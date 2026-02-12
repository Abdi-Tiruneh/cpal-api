package com.commercepal.apiservice.orders.tracking.enums;

import com.commercepal.apiservice.orders.enums.OrderStage;
import java.util.Set;
import lombok.Getter;

/**
 * OrderStageCategory Enum
 * <p>
 * High-level categories for grouping order stages in the customer UI. Matches
 * AliExpress tab
 * structure: "To pay", "To ship", "Shipped", "Processed"
 * <p>
 * Used for filtering orders in the order list view.
 */
@Getter
public enum OrderStageCategory {

  /**
   * All orders regardless of stage
   */
  ALL("All", "View all orders"),

  /**
   * Orders awaiting payment Includes: PENDING
   */
  TO_PAY("To pay", "Orders awaiting payment"),

  /**
   * Orders paid but not yet shipped Includes: PAYMENT_CONFIRMED, PROCESSING,
   * ORDERED_ON_PROVIDER,
   * PROVIDER_CONFIRMED
   */
  TO_SHIP("To ship", "Orders preparing for shipment"),

  /**
   * Orders currently in transit Includes: SHIPPED_FROM_PROVIDER through
   * OUT_FOR_LOCAL_DELIVERY
   */
  SHIPPED("Shipped", "Orders in transit"),

  /**
   * Completed orders (delivered or refunded) Includes: DELIVERED, REFUNDED,
   * PARTIALLY_DELIVERED
   */
  PROCESSED("Processed", "Completed orders"),

  /**
   * Cancelled or failed orders Includes: CANCELLED, FAILED
   */
  CANCELLED("Cancelled", "Cancelled orders");

  private final String label;
  private final String description;

  OrderStageCategory(String label, String description) {
    this.label = label;
    this.description = description;
  }

  /**
   * Get the category for a given OrderStage
   */
  public static OrderStageCategory fromOrderStage(OrderStage stage) {
    return switch (stage) {
      case PENDING -> TO_PAY;

      case PAYMENT_CONFIRMED, PROCESSING, ORDERED_ON_PROVIDER, PROVIDER_CONFIRMED -> TO_SHIP;

      case SHIPPED_FROM_PROVIDER, IN_INTERNATIONAL_TRANSIT, ARRIVED_IN_ETHIOPIA,
          IN_ETHIOPIAN_CUSTOMS, CUSTOMS_CLEARED, CUSTOMS_HELD, AT_LOCAL_HUB,
          OUT_FOR_LOCAL_DELIVERY, RETURN_IN_TRANSIT ->
        SHIPPED;

      case DELIVERED, REFUNDED, PARTIALLY_DELIVERED, RETURNED, REFUND_INITIATED -> PROCESSED;

      case CANCELLED, FAILED -> CANCELLED;

      case RETURN_REQUESTED, ON_HOLD -> SHIPPED; // Keep in shipped while being processed
    };
  }

  /**
   * Parse from string (case-insensitive)
   */
  public static OrderStageCategory fromString(String name) {
    try {
      return OrderStageCategory.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      return ALL; // Default to ALL if invalid
    }
  }

  /**
   * Get all OrderStages that belong to this category
   */
  public Set<OrderStage> getIncludedStages() {
    return switch (this) {
      case ALL -> Set.of(OrderStage.values());

      case TO_PAY -> Set.of(OrderStage.PENDING);

      case TO_SHIP -> Set.of(
          OrderStage.PAYMENT_CONFIRMED,
          OrderStage.PROCESSING,
          OrderStage.ORDERED_ON_PROVIDER,
          OrderStage.PROVIDER_CONFIRMED);

      case SHIPPED -> Set.of(
          OrderStage.SHIPPED_FROM_PROVIDER,
          OrderStage.IN_INTERNATIONAL_TRANSIT,
          OrderStage.ARRIVED_IN_ETHIOPIA,
          OrderStage.IN_ETHIOPIAN_CUSTOMS,
          OrderStage.CUSTOMS_CLEARED,
          OrderStage.CUSTOMS_HELD,
          OrderStage.AT_LOCAL_HUB,
          OrderStage.OUT_FOR_LOCAL_DELIVERY,
          OrderStage.RETURN_REQUESTED,
          OrderStage.RETURN_IN_TRANSIT,
          OrderStage.ON_HOLD);

      case PROCESSED -> Set.of(
          OrderStage.DELIVERED,
          OrderStage.PARTIALLY_DELIVERED,
          OrderStage.RETURNED,
          OrderStage.REFUND_INITIATED,
          OrderStage.REFUNDED);

      case CANCELLED -> Set.of(
          OrderStage.CANCELLED,
          OrderStage.FAILED);
    };
  }
}
