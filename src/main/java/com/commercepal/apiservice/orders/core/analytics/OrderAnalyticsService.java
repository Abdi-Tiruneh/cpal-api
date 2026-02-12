package com.commercepal.apiservice.orders.core.analytics;

import com.commercepal.apiservice.orders.core.analytics.dto.OrderAnalyticsSummary;
import com.commercepal.apiservice.orders.core.analytics.dto.OrderStatusBreakdown;
import com.commercepal.apiservice.orders.core.analytics.dto.SalesTrendResponse;
import java.time.LocalDate;
import java.util.List;

public interface OrderAnalyticsService {

  OrderAnalyticsSummary getDashboardSummary(LocalDate startDate, LocalDate endDate);

  List<SalesTrendResponse> getSalesTrend(LocalDate startDate, LocalDate endDate);

  List<OrderStatusBreakdown> getStatusBreakdown(LocalDate startDate, LocalDate endDate);

  List<com.commercepal.apiservice.orders.core.analytics.dto.TopProductResponse> getTopSellingProducts(
      int limit);
}
