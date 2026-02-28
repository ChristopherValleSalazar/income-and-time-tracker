package com.chrisV.hoursBackend.service;

import com.chrisV.hoursBackend.model.ProgrammingHours;
import com.chrisV.hoursBackend.repo.ProgrammingHoursRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProgrammingHoursService {

  @Autowired
    ProgrammingHoursRepo repo;

    public void saveProgrammingHours(List<ProgrammingHours> hours) {
        repo.saveAll(hours);
    }

    public BigDecimal getTotalHours() {
        List<ProgrammingHours> hours = repo.findAll();

        BigDecimal total = hours.stream()
                .map(ProgrammingHours::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total;
    }

    public List<ProgrammingHours> loadProgrammingHours() {
        return repo.findAll();
    }


}
