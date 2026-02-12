package com.commercepal.apiservice.orders.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

/**
 * OrderItemStage Enum
 * <p>
 * Represents the lifecycle stages of individual order items. Each item can progress independently
 * through its own lifecycle.
 * <p>
 * Normal Flow: PENDING → CONFIRMED → ALLOCATED → PREPARED → SHIPPED_FROM_SOURCE → IN_CUSTOMS →
 * RECEIVED_LOCALLY → OUT_FOR_DELIVERY → DELIVERED
 * <p>
 * Alternative Flows: - Cancellation: Early stages → CANCELLED - Failure: Any stage → FAILED -
 * Returns: DELIVERED → RETURN_REQUESTED → RETURN_IN_TRANSIT → RETURNED → REFUND_INITIATED →
 * REFUNDED
 */
@Getter
public enum OrderItemStage {

  // ============================================================================
  // NORMAL ITEM FLOW
  // ============================================================================

  PENDING(1, "Pending", "Item created, awaiting payment confirmation.", false, false),

  CONFIRMED(2, "Confirmed", "Payment confirmed for this item.", false, false),

  ALLOCATED(3, "Allocated", "Inventory reserved for this item.", false, false),

  PREPARED(4, "Prepared", "Item packed and ready for shipment.", false, false),

  SHIPPED_FROM_SOURCE(5, "Shipped from Source", "Item shipped from supplier/provider.", false,
      false),

  IN_CUSTOMS(6, "In Customs", "Item in customs clearance (international).", false, false),

  RECEIVED_LOCALLY(7, "Received Locally", "Item received at local hub.", false, false),

  OUT_FOR_DELIVERY(8, "Out for Delivery", "Item with local delivery agent.", false, false),

  DELIVERED(9, "Delivered", "Item successfully delivered to customer.", true, false),

  // ============================================================================
  // RETURN & REFUND FLOW
  // ============================================================================

  RETURN_REQUESTED(10, "Return Requested", "Customer requested return for this item.", false,
      false),

  RETURN_IN_TRANSIT(11, "Return In Transit", "Item being returned by customer.", false, false),

  RETURNED(12, "Returned", "Returned item received successfully.", false, false),

  REFUND_INITIATED(13, "Refund Initiated", "Refund process started for this item.", false, false),

  REFUNDED(14, "Refunded", "Refund issued successfully.", true, false),

  // ============================================================================
  // FAILURE & CANCELLATION FLOW
  // ============================================================================

  CANCELLED(15, "Cancelled", "Item cancelled before shipment.", true, true),

  FAILED(16, "Failed", "Processing failed (stock error, provider failure, etc.).", true, true);

  /**
   * Valid stage transitions
   */
  private static final Map<OrderItemStage, Set<OrderItemStage>> VALID_TRANSITIONS = Map.ofEntries(
      Map.entry(PENDING, Set.of(CONFIRMED, CANCELLED, FAILED)),
      Map.entry(CONFIRMED, Set.of(ALLOCATED, CANCELLED, FAILED)),
      Map.entry(ALLOCATED, Set.of(PREPARED, CANCELLED, FAILED)),
      Map.entry(PREPARED, Set.of(SHIPPED_FROM_SOURCE, CANCELLED, FAILED)),
      Map.entry(SHIPPED_FROM_SOURCE, Set.of(IN_CUSTOMS, RECEIVED_LOCALLY, FAILED)),
      Map.entry(IN_CUSTOMS, Set.of(RECEIVED_LOCALLY, FAILED)),
      Map.entry(RECEIVED_LOCALLY, Set.of(OUT_FOR_DELIVERY, FAILED)),
      Map.entry(OUT_FOR_DELIVERY, Set.of(DELIVERED, FAILED)),
      Map.entry(DELIVERED, Set.of(RETURN_REQUESTED)),
      Map.entry(RETURN_REQUESTED, Set.of(RETURN_IN_TRANSIT)),
      Map.entry(RETURN_IN_TRANSIT, Set.of(RETURNED)),
      Map.entry(RETURNED, Set.of(REFUND_INITIATED)),
      Map.entry(REFUND_INITIATED, Set.of(REFUNDED)),
      Map.entry(REFUNDED, Set.of()),
      Map.entry(CANCELLED, Set.of()),
      Map.entry(FAILED, Set.of()));
  private final int code;
  private final String label;
  private final String description;
  private final boolean isFinalStage;
  private final boolean isFailureStage;

  OrderItemStage(int code, String label, String description, boolean isFinalStage,
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
  public static Set<OrderItemStage> getAllowedNextStages(OrderItemStage currentStage) {
    return VALID_TRANSITIONS.getOrDefault(currentStage, Set.of());
  }

  /**
   * Parse from string (case-insensitive)
   */
  public static OrderItemStage fromString(String name) {
    try {
      return OrderItemStage.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid OrderItemStage name: " + name);
    }
  }

  /**
   * Parse from code
   */
  public static OrderItemStage fromCode(int code) {
    return Arrays.stream(values())
        .filter(stage -> stage.code == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid OrderItemStage code: " + code));
  }

  /**
   * Check if transition to next stage is valid
   */
  public boolean canTransitionTo(OrderItemStage nextStage) {
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
}
