package com.commercepal.apiservice.orders.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderStageStatistics
 * <p>
 * Statistics showing count of orders in each stage category. Used for displaying badge counts on
 * tabs (like AliExpress).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStageStatistics {

  /**
   * Count of orders awaiting payment
   */
  @Builder.Default
  private final Long toPay = 0L;

  /**
   * Count of orders paid but not shipped
   */
  @Builder.Default
  private final Long toShip = 0L;

  /**
   * Count of orders currently in transit
   */
  @Builder.Default
  private final Long shipped = 0L;

  /**
   * Count of completed orders (delivered/refunded)
   */
  @Builder.Default
  private final Long processed = 0L;

  /**
   * Count of cancelled/failed orders
   */
  @Builder.Default
  private final Long cancelled = 0L;

  /**
   * Total count of all orders
   */
  @Builder.Default
  private final Long total = 0L;
}
