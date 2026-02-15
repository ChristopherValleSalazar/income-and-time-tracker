package com.chrisV.hoursBackend.controller;

import com.chrisV.hoursBackend.model.AmazonNames;
import com.chrisV.hoursBackend.model.AmazonTransaction;
import com.chrisV.hoursBackend.repo.AmazonTransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/amzTransaction")
@CrossOrigin("http://127.0.0.1:5500/")
public class AmazonTransactionController {

    @Autowired
    AmazonTransactionRepo repo;

    @GetMapping("/test")
    public void testingAmazon() {
        System.out.println("Endpoint hit for amazon hehe");
    }

    @PostMapping("/saveTable")
    public ResponseEntity<Void> saveAmzRows(@RequestBody List<Map<String, String>> transactions) {
        System.out.println(transactions.toString());
        List<AmazonTransaction> entities = transactions
                .stream()
                .map(entity -> {
                    AmazonTransaction row = new AmazonTransaction();
                    row.setAmount(new BigDecimal(entity.get("amount")));
                    row.setDateOfWork(LocalDate.parse(entity.get("date")));
                    row.setPerson(AmazonNames.valueOf(entity.get("person")));
                    row.setPackageNum(Byte.valueOf(entity.get("package")));
                    return row;
                }).toList();
        repo.saveAll(entities);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAllRows")
    public ResponseEntity<List<Map<String, String>>> loadAllAmzRows() {
        return new ResponseEntity<>(repo.findAll().stream()
                .map(entity -> {
                    Map<String, String> entities = new HashMap<>();
                    entities.put("date", String.valueOf(entity.getDateOfWork()));
                    entities.put("amount", String.valueOf(entity.getAmount()));
                    entities.put("person", String.valueOf(entity.getPerson()));
                    entities.put("package", String.valueOf(entity.getPackageNum()));
                    return entities;
                }).toList(), HttpStatus.OK);
    }

    @GetMapping("/getAllWorkerNames")
    public ResponseEntity<List<AmazonNames>> getAllWorkerName() {
        System.out.println("access heheh");
        return new ResponseEntity<>(repo.findAmazonTransactionNames(), HttpStatus.OK);
    }

    @GetMapping("/getAllTotalPerWeek")
    public ResponseEntity<List<Map<String, String>>> loadTotalPerWeek() {

        List<AmazonTransaction> transactions = repo.findAll();
        return new ResponseEntity<>(generateTotalsForAllWeeks(transactions), HttpStatus.OK);
    }

    private List<Map<String, String>> generateTotalsForAllWeeks(List<AmazonTransaction> transactions) {

        Map<LocalDate, List<AmazonTransaction>> weeklyDateRange = generateFullWeekDateRange(transactions);

        List<String> totalWeeklyPackages = generateTotalWeeklyPackages(weeklyDateRange);
        List<String> totalWeeklyAmounts = generateTotalWeeklyAmounts(weeklyDateRange);
        List<Map<String, String>> completeWeeklyReport = getMaps(weeklyDateRange, totalWeeklyAmounts, totalWeeklyPackages);

        System.out.println(completeWeeklyReport);

        return completeWeeklyReport;
    }

    private List<Map<String, String>> getMaps(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange, List<String> totalWeeklyAmounts, List<String> totalWeeklyPackages) {
        List<String> totalWeeklyDates = convertWeeklyDateRangeIntoString(weeklyDateRange);

        List<Map<String, String>> completeWeeklyReport = new ArrayList<>();

        for(int i = 0; i < totalWeeklyAmounts.size(); i++) {
            Map<String,String> weeklyReport = new LinkedHashMap<>();
            weeklyReport.put("Range Of Dates", totalWeeklyDates.get(i));
            weeklyReport.put("Total Packages", totalWeeklyPackages.get(i));
            weeklyReport.put("Total Amount", totalWeeklyAmounts.get(i));
            completeWeeklyReport.add(weeklyReport);
        }
        return completeWeeklyReport;
    }

    private List<String> convertWeeklyDateRangeIntoString(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange) {
        return weeklyDateRange.keySet().stream()
                .map(weekStart -> {
                    LocalDate weekEnd = weekStart.plusDays(5);
                    return weekStart + " - " + weekEnd;
                }).toList();
    }

    private List<String> generateTotalWeeklyPackages(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange) {
        return weeklyDateRange.entrySet().stream()
                .map(entry -> {
                    List<AmazonTransaction> weekTransactions = entry.getValue();

                    int totalPackages = weekTransactions.stream()
                            .mapToInt(AmazonTransaction::getPackageNum)
                            .sum();
                    return String.valueOf(totalPackages);
                }).toList();
    }

    private List<String> generateTotalWeeklyAmounts(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange) {
        return weeklyDateRange.entrySet().stream()
                .map(entry -> {
                    List<AmazonTransaction> weekTransactions = entry.getValue();

                    BigDecimal totalAmounts = weekTransactions.stream()
                            .map(AmazonTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return String.valueOf(totalAmounts);
                }).toList();
    }

    private Map<LocalDate, List<AmazonTransaction>> generateFullWeekDateRange(List<AmazonTransaction> transactions) {
        Map<LocalDate, List<AmazonTransaction>> grouped = transactions.stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                .collect(Collectors.groupingBy(
                        t -> t.getDateOfWork().with(DayOfWeek.MONDAY),
                        TreeMap::new,
                        Collectors.toList()
                ))

                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() == 6)

                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                                .toList(),
                        (a, b) -> a,
                        TreeMap::new
                ));

        return grouped;
    }

    private Map<LocalDate, List<AmazonTransaction>> generateAnyWeekDateRange(List<AmazonTransaction> transactions) {
        Map<LocalDate, List<AmazonTransaction>> grouped = transactions.stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                .collect(Collectors.groupingBy(
                        t -> t.getDateOfWork().with(DayOfWeek.MONDAY),
                        TreeMap::new,
                        Collectors.toList()
                ));

        return grouped;
    }

    private List<Map<String, String>> generateWeeklyTotalsPerPerson(List<AmazonTransaction> transactions) {
        Map<LocalDate, List<AmazonTransaction>> weeklyDateRange = generateFullWeekDateRange(transactions);

        Map<AmazonNames, List<AmazonTransaction>> byPerson =
                transactions.stream()
                        .collect(Collectors.groupingBy(
                                AmazonTransaction::getPerson,
                                TreeMap::new,
                                Collectors.toList()
                        ));

        Map<AmazonNames, Map<LocalDate, List<AmazonTransaction>>> completeTransactionsPerPersonAndWeek =
                byPerson.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> generateAnyWeekDateRange(entry.getValue()),
                                (a, b) -> a,
                                TreeMap::new
                        ));

        List<Map<String, String>> completeWeeklyReportPerPerson = new ArrayList<>();

        completeTransactionsPerPersonAndWeek.forEach((person, weeks) -> {

                    weeks.forEach((weekStart, weekTransactions) -> {

                        LocalDate weekEnd = weekStart.plusDays(5);

                        BigDecimal totalAmount = weekTransactions.stream()
                                .map(AmazonTransaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        int totalPackages = weekTransactions.stream()
                                .mapToInt(AmazonTransaction::getPackageNum)
                                .sum();

                        Map<String, String> personReport = new HashMap<>();
                        personReport.put("Person", String.valueOf(person));
                        personReport.put("WeekRange", (weekStart + " - " + weekEnd));
                        personReport.put("TotalPackages", String.valueOf(totalPackages));
                        personReport.put("TotalAmount", String.valueOf(totalAmount));

                        completeWeeklyReportPerPerson.add(personReport);

                    });
                });

        /*
        using this list will allow me to generate a total per person, I will make individual methods afterward
        essentially this logic will be done in 2 methods, one for complete total and the other one will be for
        filter out totals per person
         */

        //need to filter this string response for packages then use same logic for the rest of them
        //instead of getting the string version of the list I need to filter out the original obj
        //then I need to filter and passed that into the converter to String list
        //So separating concerns for these conversions.
        return completeWeeklyReportPerPerson;
    }

    @GetMapping("/getWeeklyTotalsPerPerson")
    public ResponseEntity<List<Map<String, String>>> loadWeeklyTotalPerPerson() {
        List<AmazonTransaction> transactions = repo.findAll();

        return new ResponseEntity<>(generateWeeklyTotalsPerPerson(transactions), HttpStatus.OK);
    }
}




