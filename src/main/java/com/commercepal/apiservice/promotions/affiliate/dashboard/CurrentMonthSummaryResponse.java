package com.commercepal.apiservice.promotions.affiliate.dashboard;

import com.commercepal.apiservice.utils.CurrencyFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CurrentMonthSummaryResponse {
    private String totalEarnings;
    private long totalOrders;
    private long totalClicks;
    private String conversionRate;

    public CurrentMonthSummaryResponse(BigDecimal totalEarnings, long totalOrders, long totalClicks, String conversionRate) {
        this.totalEarnings = CurrencyFormatUtil.format(totalEarnings, "ETB");
        this.totalOrders = totalOrders;
        this.totalClicks = totalClicks;
        this.conversionRate = conversionRate;
    }
}
