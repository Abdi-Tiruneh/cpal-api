package com.commercepal.apiservice.promotions.affiliate.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyEarningsDTO {
    private LocalDate date;
    private BigDecimal commissionAmount = BigDecimal.ZERO;
    private Integer clickCount = 0;
    private BigDecimal bountyAmount = BigDecimal.ZERO; // future use
}
