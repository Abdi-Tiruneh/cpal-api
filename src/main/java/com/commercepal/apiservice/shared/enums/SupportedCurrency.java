package com.commercepal.apiservice.shared.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportedCurrency {
  USD("USD", "$"),
  ETB("ETB", "ETB"),
  AED("AED", "AED"),
  KES("KES", "KES"),
  SOS("SOS", "SOS");

  private final String code;
  private final String symbol;

  public static SupportedCurrency fromCode(String code) {
    if (code == null) {
      return null;
    }
    for (SupportedCurrency currency : values()) {
      if (currency.code.equalsIgnoreCase(code)) {
        return currency;
      }
    }
    throw new IllegalArgumentException("Unsupported currency code: " + code);
  }

  @Override
  public String toString() {
    return code;
  }
}

