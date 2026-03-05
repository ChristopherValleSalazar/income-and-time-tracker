package com.chrisV.hoursBackend.dto;

import com.chrisV.hoursBackend.model.AmazonNames;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class WeeklyReportPerPerson {

    private String weekRange;
    private Integer weeklyPackageNumPerPerson;
    private BigDecimal weeklyAmountPerPerson;
    private AmazonNames worker;
}
