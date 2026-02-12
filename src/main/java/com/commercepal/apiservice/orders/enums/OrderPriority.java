package com.commercepal.apiservice.orders.enums;

/**
 * OrderPriority Enum
 * <p>
 * Represents the priority level of an order for processing and fulfillment.
 */
public enum OrderPriority {

  /**
   * Low priority - standard processing
   */
  LOW,

  /**
   * Normal priority - default for most orders
   */
  NORMAL,

  /**
   * High priority - expedited processing
   */
  HIGH,

  /**
   * Urgent priority - immediate attention required
   */
  URGENT,

  /**
   * Critical priority - highest priority (VIP customers, time-sensitive)
   */
  CRITICAL;

  /**
   * Parse from string (case-insensitive)
   */
  public static OrderPriority fromString(String priority) {
    if (priority == null || priority.trim().isEmpty()) {
      return NORMAL;
    }

    try {
      return OrderPriority.valueOf(priority.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return NORMAL;
    }
  }

  /**
   * Get priority level as numeric value (1-5)
   */
  public int getLevel() {
    return switch (this) {
      case LOW -> 1;
      case NORMAL -> 2;
      case HIGH -> 3;
      case URGENT -> 4;
      case CRITICAL -> 5;
    };
  }

  /**
   * Check if this is an elevated priority (above normal)
   */
  public boolean isElevated() {
    return this == HIGH || this == URGENT || this == CRITICAL;
  }
}
