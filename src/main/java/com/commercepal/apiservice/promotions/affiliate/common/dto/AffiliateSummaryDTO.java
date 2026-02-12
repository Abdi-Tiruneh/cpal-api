package com.commercepal.apiservice.promotions.affiliate.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AffiliateSummaryDTO {
    private BigDecimal totalEarnings;
    private int totalOrderedItems;
    private int totalShippedItems;
    private int totalClicks;
    private BigDecimal conversionRate; // (converted referrals / total referrals) * 100
}
