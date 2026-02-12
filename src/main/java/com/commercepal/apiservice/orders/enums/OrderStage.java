package com.commercepal.apiservice.orders.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

/**
 * OrderStage Enum
 * <p>
 * Represents the lifecycle stages of an order in an Ethiopia-based dropshipping platform. Orders
 * are placed on external providers (Amazon, AliExpress, etc.) and shipped to Ethiopia.
 * <p>
 * Dropshipping Flow (Ethiopia-based): 1. Customer places order on CommercePal platform → PENDING 2.
 * Customer pays → PAYMENT_CONFIRMED 3. CommercePal places order on provider (Amazon/AliExpress) →
 * ORDERED_ON_PROVIDER 4. Provider ships item internationally → SHIPPED_FROM_PROVIDER 5. Item in
 * international transit → IN_INTERNATIONAL_TRANSIT 6. Item arrives in Ethiopia →
 * ARRIVED_IN_ETHIOPIA 7. Ethiopian customs processing → IN_ETHIOPIAN_CUSTOMS 8. Customs cleared →
 * CUSTOMS_CLEARED 9. Item at local hub in Ethiopia → AT_LOCAL_HUB 10. Out for local delivery in
 * Ethiopia → OUT_FOR_LOCAL_DELIVERY 11. Delivered to customer → DELIVERED
 * <p>
 * Alternative Flows: - Cancellation: Any stage → CANCELLED - Failure: Any stage → FAILED - Returns:
 * DELIVERED → RETURN_REQUESTED → RETURN_IN_TRANSIT → RETURNED → REFUND_INITIATED → REFUNDED
 */
@Getter
public enum OrderStage {

  // ============================================================================
  // INITIAL STAGES (On CommercePal Platform)
  // ============================================================================

  PENDING(1, "Pending Payment", "Order created on CommercePal, awaiting customer payment.", false,
      false),

  PAYMENT_CONFIRMED(2, "Payment Confirmed", "Customer payment received and verified.", false,
      false),

  PROCESSING(3, "Processing", "Order being prepared to place on external provider.", false, false),

  // ============================================================================
  // PROVIDER ORDER STAGES (Ordering from Amazon/AliExpress/etc.)
  // ============================================================================

  ORDERED_ON_PROVIDER(4, "Ordered on Provider",
      "Order placed on external provider (Amazon, AliExpress, Alibaba, Shein, etc.).", false,
      false),

  PROVIDER_CONFIRMED(5, "Provider Confirmed",
      "External provider confirmed the order.", false, false),

  // ============================================================================
  // INTERNATIONAL SHIPPING STAGES
  // ============================================================================

  SHIPPED_FROM_PROVIDER(6, "Shipped from Provider",
      "Items shipped internationally from provider's country.", false, false),

  IN_INTERNATIONAL_TRANSIT(7, "In International Transit",
      "Items in transit to Ethiopia (air/sea freight).", false, false),

  // ============================================================================
  // ETHIOPIA ARRIVAL & CUSTOMS STAGES
  // ============================================================================

  ARRIVED_IN_ETHIOPIA(8, "Arrived in Ethiopia",
      "Items arrived in Ethiopia (airport/port).", false, false),

  IN_ETHIOPIAN_CUSTOMS(9, "In Ethiopian Customs",
      "Items undergoing Ethiopian customs clearance process.", false, false),

  CUSTOMS_CLEARED(10, "Customs Cleared",
      "Items cleared Ethiopian customs successfully.", false, false),

  // ============================================================================
  // LOCAL DELIVERY STAGES (Within Ethiopia)
  // ============================================================================

  AT_LOCAL_HUB(11, "At Local Hub",
      "Items at CommercePal's local warehouse/hub in Ethiopia.", false, false),

  OUT_FOR_LOCAL_DELIVERY(12, "Out for Local Delivery",
      "Items out with local delivery courier in Ethiopia.", false, false),

  DELIVERED(13, "Delivered",
      "Order successfully delivered to customer in Ethiopia.", true, false),

  // ============================================================================
  // RETURN & REFUND FLOW
  // ============================================================================

  RETURN_REQUESTED(14, "Return Requested",
      "Customer requested to return items.", false, false),

  RETURN_IN_TRANSIT(15, "Return In Transit",
      "Returned items being shipped back to hub.", false, false),

  RETURNED(16, "Returned",
      "Returned items received at hub.", false, false),

  REFUND_INITIATED(17, "Refund Initiated",
      "Refund process started for customer.", false, false),

  REFUNDED(18, "Refunded",
      "Full refund issued to customer.", true, false),

  // ============================================================================
  // FAILURE & CANCELLATION FLOW
  // ============================================================================

  CANCELLED(19, "Cancelled",
      "Order cancelled (before or after provider placement).", true, true),

  FAILED(20, "Failed",
      "Order failed (payment issue, provider issue, customs issue, etc.).", true, true),

  // ============================================================================
  // SPECIAL STATUSES
  // ============================================================================

  PARTIALLY_DELIVERED(21, "Partially Delivered",
      "Some items delivered, others still in transit.", false, false),

  ON_HOLD(22, "On Hold",
      "Order temporarily on hold (fraud review, customs hold, etc.).", false, false),

  CUSTOMS_HELD(23, "Customs Held",
      "Items held by Ethiopian customs (requires action).", false, false);

  /**
   * Valid stage transitions for Ethiopia-based dropshipping
   */
  private static final Map<OrderStage, Set<OrderStage>> VALID_TRANSITIONS = Map.ofEntries(
      Map.entry(PENDING, Set.of(PAYMENT_CONFIRMED, CANCELLED, FAILED)),
      Map.entry(PAYMENT_CONFIRMED, Set.of(PROCESSING, CANCELLED, FAILED)),
      Map.entry(PROCESSING, Set.of(ORDERED_ON_PROVIDER, ON_HOLD, CANCELLED, FAILED)),
      Map.entry(ORDERED_ON_PROVIDER,
          Set.of(PROVIDER_CONFIRMED, SHIPPED_FROM_PROVIDER, CANCELLED, FAILED)),
      Map.entry(PROVIDER_CONFIRMED, Set.of(SHIPPED_FROM_PROVIDER, CANCELLED, FAILED)),
      Map.entry(SHIPPED_FROM_PROVIDER,
          Set.of(IN_INTERNATIONAL_TRANSIT, ARRIVED_IN_ETHIOPIA, FAILED)),
      Map.entry(IN_INTERNATIONAL_TRANSIT, Set.of(ARRIVED_IN_ETHIOPIA, FAILED)),
      Map.entry(ARRIVED_IN_ETHIOPIA, Set.of(IN_ETHIOPIAN_CUSTOMS, AT_LOCAL_HUB, FAILED)),
      Map.entry(IN_ETHIOPIAN_CUSTOMS, Set.of(CUSTOMS_CLEARED, CUSTOMS_HELD, FAILED)),
      Map.entry(CUSTOMS_HELD, Set.of(CUSTOMS_CLEARED, IN_ETHIOPIAN_CUSTOMS, FAILED, CANCELLED)),
      Map.entry(CUSTOMS_CLEARED, Set.of(AT_LOCAL_HUB, FAILED)),
      Map.entry(AT_LOCAL_HUB, Set.of(OUT_FOR_LOCAL_DELIVERY, FAILED)),
      Map.entry(OUT_FOR_LOCAL_DELIVERY, Set.of(DELIVERED, PARTIALLY_DELIVERED, FAILED)),
      Map.entry(DELIVERED, Set.of(RETURN_REQUESTED)),
      Map.entry(PARTIALLY_DELIVERED, Set.of(DELIVERED, RETURN_REQUESTED)),
      Map.entry(RETURN_REQUESTED, Set.of(RETURN_IN_TRANSIT)),
      Map.entry(RETURN_IN_TRANSIT, Set.of(RETURNED)),
      Map.entry(RETURNED, Set.of(REFUND_INITIATED)),
      Map.entry(REFUND_INITIATED, Set.of(REFUNDED)),
      Map.entry(ON_HOLD, Set.of(PROCESSING, ORDERED_ON_PROVIDER, IN_ETHIOPIAN_CUSTOMS, CANCELLED)),
      Map.entry(REFUNDED, Set.of()),
      Map.entry(CANCELLED, Set.of()),
      Map.entry(FAILED, Set.of()));
  private final int code;
  private final String label;
  private final String description;
  private final boolean isFinalStage;
  private final boolean isFailureStage;

  OrderStage(int code, String label, String description, boolean isFinalStage,
      boolean isFailureStage) {
    this.code = code;
    this.label = label;
    this.description = description;
    this.isFinalStage = isFinalStage;
    this.isFailureStage = isFailureStage;
  }

  /**
   * Get all allowed next stages from current stage
   */
  public static Set<OrderStage> getAllowedNextStages(OrderStage currentStage) {
    return VALID_TRANSITIONS.getOrDefault(currentStage, Set.of());
  }

  /**
   * Parse from string (case-insensitive)
   */
  public static OrderStage fromString(String name) {
    try {
      return OrderStage.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid OrderStage name: " + name);
    }
  }

  /**
   * Parse from code
   */
  public static OrderStage fromCode(int code) {
    return Arrays.stream(values())
        .filter(stage -> stage.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid OrderStage code: " + code));
  }

  /**
   * Check if transition to next stage is valid
   */
  public boolean canTransitionTo(OrderStage nextStage) {
    return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(nextStage);
  }

  /**
   * Check if this is an active stage (not final)
   */
  public boolean isActive() {
    return !isFinalStage;
  }

  /**
   * Check if this is a successful completion stage
   */
  public boolean isSuccessful() {
    return isFinalStage && !isFailureStage;
  }

  /**
   * Check if order is with external provider
   */
  public boolean isWithProvider() {
    return this == ORDERED_ON_PROVIDER || this == PROVIDER_CONFIRMED ||
        this == SHIPPED_FROM_PROVIDER || this == IN_INTERNATIONAL_TRANSIT;
  }

  /**
   * Check if order is in Ethiopia
   */
  public boolean isInEthiopia() {
    return this == ARRIVED_IN_ETHIOPIA || this == IN_ETHIOPIAN_CUSTOMS ||
        this == CUSTOMS_CLEARED || this == CUSTOMS_HELD ||
        this == AT_LOCAL_HUB || this == OUT_FOR_LOCAL_DELIVERY ||
        this == DELIVERED || this == PARTIALLY_DELIVERED;
  }

  /**
   * Check if order is in customs process
   */
  public boolean isInCustoms() {
    return this == IN_ETHIOPIAN_CUSTOMS || this == CUSTOMS_HELD;
  }
}
