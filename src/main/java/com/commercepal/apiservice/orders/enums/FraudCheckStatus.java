package com.commercepal.apiservice.orders.enums;

/**
 * FraudCheckStatus Enum
 * <p>
 * Represents the fraud detection/verification status of an order.
 */
public enum FraudCheckStatus {

  /**
   * Fraud check pending/not yet performed
   */
  PENDING,

  /**
   * Fraud check in progress
   */
  IN_REVIEW,

  /**
   * Order approved - no fraud detected
   */
  APPROVED,

  /**
   * Order flagged as suspicious - requires manual review
   */
  FLAGGED,

  /**
   * Order rejected due to fraud
   */
  REJECTED,

  /**
   * Fraud check bypassed/not required
   */
  BYPASSED;

  /**
   * Parse from string (case-insensitive)
   */
  public static FraudCheckStatus fromString(String status) {
    if (status == null || status.trim().isEmpty()) {
      return PENDING;
    }

    try {
      return FraudCheckStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return PENDING;
    }
  }

  /**
   * Check if order can proceed (approved or bypassed)
   */
  public boolean canProceed() {
    return this == APPROVED || this == BYPASSED;
  }

  /**
   * Check if manual review is required
   */
  public boolean requiresReview() {
    return this == FLAGGED || this == IN_REVIEW;
  }

  /**
   * Check if order is blocked
   */
  public boolean isBlocked() {
    return this == REJECTED;
  }
}
