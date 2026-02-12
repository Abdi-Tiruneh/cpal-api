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
public class SalesTrendResponse {

  private String date; // YYYY-MM-DD
  private Long orderCount;
  private BigDecimal totalRevenue;
}
