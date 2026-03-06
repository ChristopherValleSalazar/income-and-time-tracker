package com.chrisV.hoursBackend.repo;

import com.chrisV.hoursBackend.model.ProgrammingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProgrammingHoursRepo extends JpaRepository<ProgrammingHours, Long> {

    @Query("SELECT SUM(h.hours) FROM ProgrammingHours h")
    BigDecimal getTotalHours();
}
