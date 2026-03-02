package com.chrisV.hoursBackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class WeeklyReportGeneral {

    private String weekRange;
    private Integer weeklyPackageNum;
    private BigDecimal weeklyAmount;
}
