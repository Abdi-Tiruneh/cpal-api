package com.commercepal.apiservice.orders.enums;

/**
 * RefundStatus Enum
 * <p>
 * Represents the refund status of an order or order item.
 */
public enum RefundStatus {

  /**
   * No refund requested or applicable
   */
  NONE,

  /**
   * Refund requested but not yet processed
   */
  REQUESTED,

  /**
   * Refund is being processed
   */
  PROCESSING,

  /**
   * Refund approved and pending execution
   */
  APPROVED,

  /**
   * Partial refund completed
   */
  PARTIAL,

  /**
   * Full refund completed
   */
  FULL,

  /**
   * Refund rejected/denied
   */
  REJECTED,

  /**
   * Refund failed during processing
   */
  FAILED;

  /**
   * Parse from string (case-insensitive)
   */
  public static RefundStatus fromString(String status) {
    if (status == null || status.trim().isEmpty()) {
      return NONE;
    }

    try {
      return RefundStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid RefundStatus: " + status);
    }
  }

  /**
   * Check if refund is completed
   */
  public boolean isCompleted() {
    return this == PARTIAL || this == FULL;
  }

  /**
   * Check if refund is in progress
   */
  public boolean isInProgress() {
    return this == REQUESTED || this == PROCESSING || this == APPROVED;
  }

  /**
   * Check if refund is final (no further changes expected)
   */
  public boolean isFinal() {
    return this == FULL || this == REJECTED || this == FAILED;
  }
}
