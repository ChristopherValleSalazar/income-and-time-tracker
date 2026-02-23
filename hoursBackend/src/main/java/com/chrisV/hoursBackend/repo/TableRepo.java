package com.chrisV.hoursBackend.repo;

import com.chrisV.hoursBackend.model.ProgrammingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableRepo extends JpaRepository<ProgrammingHours, Long> {
}
