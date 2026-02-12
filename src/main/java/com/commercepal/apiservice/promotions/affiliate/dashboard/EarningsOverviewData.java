package com.commercepal.apiservice.promotions.affiliate.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EarningsOverviewData {
    private LocalDate date;
    private BigDecimal commissions;  // From ORDER type
    private BigDecimal bounties;     // From SIGNUP type
    private long clicks;
}
