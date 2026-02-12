package com.commercepal.apiservice.promotions.affiliate.dashboard;

import com.commercepal.apiservice.utils.CurrencyFormatUtil;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LifetimeSummaryResponse {
    private String totalCommissions;
    private String totalBounties;
    private long totalClicks;

    public LifetimeSummaryResponse(BigDecimal totalCommissions, BigDecimal totalBounties, long totalClicks) {
        this.totalCommissions = CurrencyFormatUtil.format(totalCommissions, "ETB");
        this.totalBounties = CurrencyFormatUtil.format(totalBounties, "ETB");
        this.totalClicks = totalClicks;
    }
}
