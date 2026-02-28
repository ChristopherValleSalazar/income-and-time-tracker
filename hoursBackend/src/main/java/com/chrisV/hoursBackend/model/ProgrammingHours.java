package com.chrisV.hoursBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "programming_hours")
public class ProgrammingHours {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDate date;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(columnDefinition = "TEXT")
    private String description;
}
