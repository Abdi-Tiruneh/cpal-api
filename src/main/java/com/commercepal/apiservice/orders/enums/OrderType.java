package com.commercepal.apiservice.orders.enums;

/**
 * OrderType Enum
 * <p>
 * Represents the type/category of an order.
 */
public enum OrderType {

  /**
   * Standard/regular order
   */
  NORMAL,

  /**
   * Bulk/wholesale order (large quantity)
   */
  BULK,

  /**
   * Pre-order (product not yet available)
   */
  PREORDER,

  /**
   * Subscription order (recurring)
   */
  SUBSCRIPTION,

  /**
   * Sample order (for testing/evaluation)
   */
  SAMPLE,

  /**
   * Dropshipping order
   */
  DROPSHIP,

  /**
   * Gift order
   */
  GIFT,

  /**
   * Express/urgent order
   */
  EXPRESS;

  /**
   * Parse from string (case-insensitive)
   */
  public static OrderType fromString(String type) {
    if (type == null || type.trim().isEmpty()) {
      return NORMAL;
    }

    try {
      return OrderType.valueOf(type.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return NORMAL;
    }
  }

  /**
   * Check if this order type requires special handling
   */
  public boolean requiresSpecialHandling() {
    return this == BULK || this == PREORDER || this == SUBSCRIPTION || this == EXPRESS;
  }
}
