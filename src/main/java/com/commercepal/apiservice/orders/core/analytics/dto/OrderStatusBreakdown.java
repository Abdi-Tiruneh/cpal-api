package com.commercepal.apiservice.orders.core.analytics.dto;

import com.commercepal.apiservice.orders.enums.OrderStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusBreakdown {

  private OrderStage stage;
  private Long count;
  private Double percentage;
}
