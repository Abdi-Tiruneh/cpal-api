package com.commercepal.apiservice.orders.enums;

/**
 * ProductProvider Enum
 * <p>
 * Represents the source/provider of products in the order. Supports multiple e-commerce platforms
 * and marketplaces.
 */
public enum ProductProvider {

  /**
   * AliExpress (Alibaba Group's retail platform)
   */
  ALIEXPRESS,

  /**
   * Amazon (Global e-commerce platform)
   */
  AMAZON,

  /**
   * Alibaba (B2B wholesale platform)
   */
  ALIBABA,

  /**
   * Shein (Fashion and apparel platform)
   */
  SHEIN,

  /**
   * CommercePal's own inventory
   */
  COMMERCEPAL,

  /**
   * Temu (E-commerce platform)
   */
  TEMU,

  /**
   * DHgate (Wholesale marketplace)
   */
  DHGATE,

  /**
   * Banggood (Online retailer)
   */
  BANGGOOD,

  /**
   * Wish (E-commerce platform)
   */
  WISH,

  /**
   * eBay (Online marketplace)
   */
  EBAY,

  /**
   * Other/Unknown provider
   */
  OTHER,

  /**
   * Unknown provider
   */
  UNKNOWN;

  /**
   * Parse from string (case-insensitive)
   */
  public static ProductProvider fromString(String name) {
    if (name == null || name.trim().isEmpty()) {
      return UNKNOWN;
    }

    try {
      // Handle legacy variations
      String normalized = name.trim().toUpperCase();
      if (normalized.equals("ALIEXPRESSSINGAPORE")) {
        return ALIEXPRESS;
      }
      return ProductProvider.valueOf(normalized);
    } catch (IllegalArgumentException e) {
      return UNKNOWN;
    }
  }

  /**
   * Check if this is an external provider (not CommercePal)
   */
  public boolean isExternal() {
    return this != COMMERCEPAL && this != UNKNOWN;
  }

  /**
   * Check if this provider requires international shipping
   */
  public boolean requiresInternationalShipping() {
    return this == ALIEXPRESS || this == AMAZON || this == ALIBABA ||
        this == SHEIN || this == TEMU || this == DHGATE ||
        this == BANGGOOD || this == WISH;
  }
}
