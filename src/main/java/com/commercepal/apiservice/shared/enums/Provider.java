package com.commercepal.apiservice.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of supported product providers.
 * <p>
 * Represents the various e-commerce platforms and marketplaces from which products can be sourced.
 */
@Getter
@RequiredArgsConstructor
public enum Provider {
  /**
   * Amazon - Global e-commerce platform
   */
  AMAZON("Amazon", "Amazon"),

  /**
   * Alibaba - Global B2B e-commerce platform
   */
  ALIBABA("Alibaba", "Alibaba"),

  /**
   * AliExpress - Global online retail service
   */
  ALIEXPRESS("Aliexpress", "AliExpress"),

  /**
   * AliExpress Singapore - Singapore-specific AliExpress marketplace
   */
  ALIEXPRESS_SINGAPORE("AliexpressSingapore", "AliExpress Singapore"),

  /**
   * Shein - Fast fashion e-commerce platform
   */
  SHEIN("Shein", "SHEIN");

  private final String code;
  private final String displayName;

  /**
   * Creates a Provider enum from a code string.
   * <p>
   * Supports case-insensitive matching and handles common variations: - "Aliexpress", "AliExpress",
   * "AliexpressSingapore" -> ALIEXPRESS or ALIEXPRESS_SINGAPORE
   *
   * @param code the provider code
   * @return the matching Provider enum
   * @throws IllegalArgumentException if the code is not recognized
   */
  @JsonCreator
  public static Provider fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }

    String normalizedCode = code.trim();

    // Handle common variations
    if (normalizedCode.equalsIgnoreCase("AliExpress")
        || normalizedCode.equalsIgnoreCase("Aliexpress")) {
      return ALIEXPRESS;
    }

    // Exact match
    for (Provider provider : values()) {
      if (provider.code.equalsIgnoreCase(normalizedCode)) {
        return provider;
      }
    }

    throw new IllegalArgumentException("Unsupported provider code: " + code);
  }

  /**
   * Gets the provider code used in API requests.
   *
   * @return the provider code
   */
  @JsonValue
  public String getCode() {
    return code;
  }

  /**
   * Checks if the provider is an AliExpress variant.
   *
   * @return true if the provider is ALIEXPRESS or ALIEXPRESS_SINGAPORE
   */
  public boolean isAliExpressVariant() {
    return this == ALIEXPRESS || this == ALIEXPRESS_SINGAPORE;
  }
}
