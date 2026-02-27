package com.chrisV.hoursBackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "amazon_transaction")
public class AmazonTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NonNull
    private LocalDate dateOfWork;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private Byte packageNum;

    @Enumerated(EnumType.STRING)
    AmazonNames person;
}
