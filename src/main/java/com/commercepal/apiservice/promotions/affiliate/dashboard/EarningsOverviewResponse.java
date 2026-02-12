package com.commercepal.apiservice.promotions.affiliate.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EarningsOverviewResponse {
    private List<EarningsOverviewData> dailyData;
}
