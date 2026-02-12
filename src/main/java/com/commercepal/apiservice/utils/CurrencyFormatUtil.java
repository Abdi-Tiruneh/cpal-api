package com.commercepal.apiservice.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Lightweight currency formatting helper. Static utility to avoid bean wiring where formatting is
 * stateless.
 */
public final class CurrencyFormatUtil {

  private CurrencyFormatUtil() {
    // utility class
  }

  public static String format(BigDecimal price, String symbol) {
    // Dummy formatter: use your actual locale-aware formatter
    return String.format("%s %,.2f", symbol, price.setScale(2, RoundingMode.HALF_UP));
  }
}
