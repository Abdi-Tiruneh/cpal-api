package com.commercepal.apiservice.cart.model;

/**
 * Cart status enum
 */
public enum CartStatus {
  /**
   * Active cart - customer is actively shopping
   */
  ACTIVE,

  /**
   * Abandoned - no activity for configured time period
   */
  ABANDONED,

  /**
   * Converted - cart was successfully converted to order
   */
  CONVERTED,

  /**
   * Expired - old cart marked for cleanup
   */
  EXPIRED,

  /**
   * Merged - guest cart merged into authenticated cart
   */
  MERGED
}