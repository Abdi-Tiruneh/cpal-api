package com.commercepal.apiservice.promotions.affiliate.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyClick {
    private LocalDate date;
    private long clicks;
}
