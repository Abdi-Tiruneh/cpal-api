package com.commercepal.apiservice.orders.core.analytics.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalyticsSummary {

  private BigDecimal totalRevenue;
  private Long totalOrders;
  private BigDecimal averageOrderValue;
  private Long pendingOrders;
  private Long completedOrders;
  private Long cancelledOrders;
}
