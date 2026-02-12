package com.commercepal.apiservice.promotions.affiliate.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyEarning {
    private LocalDate date;
    private BigDecimal amount;
}
