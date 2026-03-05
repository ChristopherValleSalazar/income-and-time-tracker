package com.chrisV.hoursBackend.service;

import com.chrisV.hoursBackend.dto.WeeklyReportGeneral;
import com.chrisV.hoursBackend.dto.WeeklyReportPerPerson;
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

    //Need validations, maybe learn again how to use @valid for a dto of these or just verified manually
    public void saveAmzRows(List<AmazonTransaction> transactions) {
        repo.saveAll(transactions);
    }

    //passing a list of objects to the frontend instead of the old method where I send a map<String, String>
    public List<AmazonTransaction> loadAmzRowsNew() {
        return repo.findAll()
                        .stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                .toList();
    }

    public AmazonNames[] getAllWorkerName() {
        return AmazonNames.values();
    }

   public List<WeeklyReportGeneral> loadWeeklyTotalTesting() {
        List<AmazonTransaction> transactions = repo.findAll();
        return generateTotalsForAllWeeks(transactions);
   }

    public List<WeeklyReportPerPerson> loadWeeklyTotalPerPerson() {
        List<AmazonTransaction> transaction = repo.findAll();
        return generateWeeklyTotalsPerPerson(transaction);
    }

    private List<WeeklyReportGeneral> generateTotalsForAllWeeks(List<AmazonTransaction> transactions) {
        Map<LocalDate, List<AmazonTransaction>> weeklyDateRange = generateFullWeekDateRange(transactions);
        List<Integer> totalWeeklyPackages = generateTotalWeeklyPackages(weeklyDateRange);
        List<BigDecimal> totalWeeklyAmounts = generateTotalWeeklyAmounts(weeklyDateRange);

        return mapDataToDTO(weeklyDateRange, totalWeeklyAmounts, totalWeeklyPackages);
    }

    private List<WeeklyReportGeneral> mapDataToDTO(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange,
                                                   List<BigDecimal> totalWeeklyAmounts,
                                                   List<Integer> totalWeeklyPackages) {

        List<String> totalWeeklyDates = convertWeeklyDateRangeIntoString(weeklyDateRange);
        List<WeeklyReportGeneral> weeklyReportGeneral = new ArrayList<>();

        for(int i = 0; i < totalWeeklyAmounts.size(); i++) {
            WeeklyReportGeneral report = new WeeklyReportGeneral();
            report.setWeekRange(totalWeeklyDates.get(i));
            report.setWeeklyPackageNum(totalWeeklyPackages.get(i));
            report.setWeeklyAmount(totalWeeklyAmounts.get(i));
            weeklyReportGeneral.add(report);
        }
        return weeklyReportGeneral;
    }

    private Map<LocalDate, List<AmazonTransaction>> generateFullWeekDateRange(List<AmazonTransaction> transactions) {
        Map<LocalDate, List<AmazonTransaction>> grouped = transactions.stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                .collect(Collectors.groupingBy(
                        t -> t.getDateOfWork().with(DayOfWeek.MONDAY),
                        Collectors.toList()
                ))

                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() == 6) // 6 representing the quantity of days per week

                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                                .toList()
                ));
        return grouped;
    }

    private List<String> convertWeeklyDateRangeIntoString(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange) {
        return weeklyDateRange.keySet().stream()
                .map(weekStart -> {
                    LocalDate weekEnd = weekStart.plusDays(5);
                    return weekStart + " - " + weekEnd;
                }).toList();
    }

    private List<Integer> generateTotalWeeklyPackages(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange) {
        return weeklyDateRange.values().stream()
                .map(weekTransactions -> {
                    int totalPackages = weekTransactions.stream()
                            .mapToInt(AmazonTransaction::getPackageNum)
                            .sum();
                    return totalPackages;
                }).toList();
    }

    private List<BigDecimal> generateTotalWeeklyAmounts(Map<LocalDate, List<AmazonTransaction>> weeklyDateRange) {
        return weeklyDateRange.values().stream()
                .map(weekTransactions -> {
                    BigDecimal totalAmounts = weekTransactions.stream()
                            .map(AmazonTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return totalAmounts;
                }).toList();
    }

    private Map<LocalDate, List<AmazonTransaction>> generateAnyWeekDateRange(List<AmazonTransaction> transactions) {
        Map<LocalDate, List<AmazonTransaction>> grouped = transactions.stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                .collect(Collectors.groupingBy(
                        t -> t.getDateOfWork().with(DayOfWeek.MONDAY),
                        Collectors.toList()
                ));

        return grouped;
    }

    private Map<AmazonNames, List<AmazonTransaction>> mapTransactionsByPerson(List<AmazonTransaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        AmazonTransaction::getPerson,
                        Collectors.toList()
                ));
    }

    private Map<AmazonNames, Map<LocalDate, List<AmazonTransaction>>> generateCompleteWeeklyTransactionsPerPerson(Map<AmazonNames, List<AmazonTransaction>> byPerson) {
        return byPerson.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> generateAnyWeekDateRange(entry.getValue())
                ));
    }

    private List<WeeklyReportPerPerson> generateWeeklyTotalsPerPerson(List<AmazonTransaction> transactions) {
        Map<AmazonNames, List<AmazonTransaction>> transactionsPerPerson = mapTransactionsByPerson(transactions);
        Map<AmazonNames, Map<LocalDate, List<AmazonTransaction>>> completeTransactionsPerPersonAndWeek = generateCompleteWeeklyTransactionsPerPerson(transactionsPerPerson);
        List<WeeklyReportPerPerson> completeWeeklyReportPerPerson = new ArrayList<>();

        completeTransactionsPerPersonAndWeek.forEach((person, weeks) -> {
            weeks.forEach((weekStart, weekTransactions) -> {

                LocalDate weekEnd = weekStart.plusDays(5);

                BigDecimal totalAmount = weekTransactions.stream()
                        .map(AmazonTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                int totalPackages = weekTransactions.stream()
                        .mapToInt(AmazonTransaction::getPackageNum)
                        .sum();

                WeeklyReportPerPerson dto = new WeeklyReportPerPerson();

                dto.setWorker(person);
                dto.setWeekRange(weekStart + " " + weekEnd);
                dto.setWeeklyPackageNumPerPerson(totalPackages);
                dto.setWeeklyAmountPerPerson(totalAmount);

                completeWeeklyReportPerPerson.add(dto);
            });
        });
        return completeWeeklyReportPerPerson;
    }
}
