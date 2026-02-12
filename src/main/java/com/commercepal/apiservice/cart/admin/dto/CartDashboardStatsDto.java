package com.commercepal.apiservice.cart.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "High-level cart analytics dashboard statistics")
public class CartDashboardStatsDto {

  @Schema(description = "Total number of currently active carts")
  private long activeCartsCount;

  @Schema(description = "Total number of items in all active carts")
  private long totalActiveItems;

  @Schema(description = "Total estimated value of all active carts")
  private BigDecimal totalActiveValue;

  @Schema(description = "Average value of an active cart")
  private BigDecimal averageCartValue;

  @Schema(description = "Purchase probability based on historical conversion")
  private Double conversionRate;

  @Schema(description = "Percentage of carts abandoned")
  private Double abandonmentRate;
}
