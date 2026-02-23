package com.chrisV.hoursBackend.service;

import com.chrisV.hoursBackend.model.AmazonNames;
import com.chrisV.hoursBackend.model.AmazonTransaction;
import com.chrisV.hoursBackend.repo.AmazonTransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AmazonServices {

    @Autowired
    AmazonTransactionRepo repo;

    final

    public void saveAmzRows(List<Map<String, String>> transactions) {
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
    }

    public List<Map<String, String>> loadAllAmzRows() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                .map(entity -> {
                    Map<String, String> entities = new HashMap<>();
                    entities.put("dates", String.valueOf(entity.getDateOfWork()));
                    entities.put("amount", String.valueOf(entity.getAmount()));
                    entities.put("person", String.valueOf(entity.getPerson()));
                    entities.put("package", String.valueOf(entity.getPackageNum()));
                    return entities;
                }).toList();
    }

    public List<AmazonTransaction> testingObjLoading() {
        List<AmazonTransaction> transactions = repo.findAll()
                        .stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                .toList();

        System.out.println(transactions);

        return transactions;

    }

    public AmazonNames[] getAllWorkerName() {
        return AmazonNames.values();
    }

    public List<Map<String, String>> loadWeeklyTotal() {
        List<AmazonTransaction> transactions =  repo.findAll();
        return generateTotalsForAllWeeks(transactions);
   }

    public List<Map<String, String>> loadWeeklyTotalPerPerson() {
        List<AmazonTransaction> transaction = repo.findAll();
        return generateWeeklyTotalsPerPerson(transaction);
    }

    private List<Map<String, String>> generateTotalsForAllWeeks(List<AmazonTransaction> transactions) {

        Map<LocalDate, List<AmazonTransaction>> weeklyDateRange = generateFullWeekDateRange(transactions);

        List<String> totalWeeklyPackages = generateTotalWeeklyPackages(weeklyDateRange);
        List<String> totalWeeklyAmounts = generateTotalWeeklyAmounts(weeklyDateRange);
        return createCompleteWeekReport(weeklyDateRange, totalWeeklyAmounts, totalWeeklyPackages);
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
                .filter(entry -> entry.getValue().size() == 6) // 6 representing the quantity of days per week

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

    private List<Map<String, String>> createCompleteWeekReport(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange, List<String> totalWeeklyAmounts, List<String> totalWeeklyPackages) {
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
        return weeklyDateRange.values().stream()
                .map(weekTransactions -> {

                    int totalPackages = weekTransactions.stream()
                            .mapToInt(AmazonTransaction::getPackageNum)
                            .sum();
                    return String.valueOf(totalPackages);
                }).toList();
    }

    private List<String> generateTotalWeeklyAmounts(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange) {
        return weeklyDateRange.values().stream()
                .map(weekTransactions -> {

                    BigDecimal totalAmounts = weekTransactions.stream()
                            .map(AmazonTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return String.valueOf(totalAmounts);
                }).toList();
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

        return completeWeeklyReportPerPerson;
    }
}
