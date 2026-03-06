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
        return repo.getTotalHours();
    }

    //TODO: this returns wrong unsorted data
    public List<ProgrammingHours> loadProgrammingHours() {
        return repo.findAll();
    }


}
