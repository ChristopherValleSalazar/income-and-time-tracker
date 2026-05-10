package com.chrisV.hoursBackend.service;

import com.chrisV.hoursBackend.dto.WeeklyReportGeneral;
import com.chrisV.hoursBackend.dto.WeeklyReportPerPerson;
import com.chrisV.hoursBackend.model.AmazonNames;
import com.chrisV.hoursBackend.model.AmazonTransaction;
import com.chrisV.hoursBackend.repo.AmazonTransactionRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public void deleteAmzRowById(Long id) {
        repo.deleteById(id);
    }

    //passing a list of objects to the frontend instead of the old method where I send a map<String, String>
        public Page<AmazonTransaction> loadAmzRowsNew(int page, int size) {
                                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateOfWork"));
                return repo.findAll(pageable);
    }

    public AmazonNames[] getAllWorkerName() {
        return AmazonNames.values();
    }

   public Page<WeeklyReportGeneral> loadWeeklyTotal(int page, int size) {
        List<AmazonTransaction> transactions = repo.findAll();
        Map<LocalDate, List<AmazonTransaction>> weeklyMap = getFullWeeks(transactions);
                List<WeeklyReportGeneral> report = convertWeeksToDto(weeklyMap);
                Collections.reverse(report);
                return paginateList(report, page, size);
   }

        public Page<WeeklyReportPerPerson> loadWeeklyTotalPerPerson(int page, int size) {
        List<AmazonTransaction> transaction = repo.findAll();
                List<WeeklyReportPerPerson> report = generateWeeklyTotalsPerPerson(transaction);
                Collections.reverse(report);
                return paginateList(report, page, size);
    }

        private <T> Page<T> paginateList(List<T> source, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                int start = (int) pageable.getOffset();

                if (start >= source.size()) {
                        return new PageImpl<>(List.of(), pageable, source.size());
                }

                int end = Math.min(start + pageable.getPageSize(), source.size());
                List<T> pageContent = source.subList(start, end);
                return new PageImpl<>(pageContent, pageable, source.size());
        }

    private Map<LocalDate, List<AmazonTransaction>> getFullWeeks(List<AmazonTransaction> transactions) {
        //group by week with starting day MONDAY
        Map<LocalDate, List<AmazonTransaction>> weeklyMap = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDateOfWork().with(DayOfWeek.MONDAY),
                        Collectors.toList()
                ));

        return weeklyMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() == 6)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
                                .toList(),
                        (a, b) -> a,
                        TreeMap::new
                ));

    }

    private List<WeeklyReportGeneral> convertWeeksToDto(Map<LocalDate, List<AmazonTransaction>> weeklyMap) {
        return weeklyMap.entrySet().stream()
                .map(entry -> {
                    LocalDate weekStart = entry.getKey();
                    LocalDate weekEnd = weekStart.plusDays(5);

                    List<AmazonTransaction> weekTransactions = entry.getValue();

                    int totalPackages = weekTransactions.stream()
                            .mapToInt(AmazonTransaction::getPackageNum)
                            .sum();

                    BigDecimal totalAmount = weekTransactions.stream()
                            .map(AmazonTransaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    WeeklyReportGeneral report = new WeeklyReportGeneral();
                    report.setWeekRange(weekStart + " - " + weekEnd);
                    report.setWeeklyPackageNum(totalPackages);
                    report.setWeeklyAmount(totalAmount);

                    return report;
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

    private Map<AmazonNames, List<AmazonTransaction>> mapTransactionsByPerson(List<AmazonTransaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(AmazonTransaction::getDateOfWork))
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
                        entry -> generateAnyWeekDateRange(entry.getValue()),
                        (a, b) -> a,
                        TreeMap::new
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
                dto.setWeekRange(weekStart + " - " + weekEnd);
                dto.setWeeklyPackageNumPerPerson(totalPackages);
                dto.setWeeklyAmountPerPerson(totalAmount);
                completeWeeklyReportPerPerson.add(dto);
            });
        });
        return completeWeeklyReportPerPerson;
    }
}
