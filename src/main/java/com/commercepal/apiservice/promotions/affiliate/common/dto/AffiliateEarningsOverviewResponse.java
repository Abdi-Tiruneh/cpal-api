package com.commercepal.apiservice.promotions.affiliate.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AffiliateEarningsOverviewResponse {
    private List<DailyEarningsDTO> dailyStats;
}
