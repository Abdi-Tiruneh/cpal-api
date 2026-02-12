package com.commercepal.apiservice.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for common BigDecimal operations.
 */
public final class BigDecimalUtils {

  private BigDecimalUtils() {
    // Prevent instantiation
  }

  /**
   * Multiplies two BigDecimal numbers and rounds the result to 2 decimal places using HALF_UP.
   *
   * @param a the first value
   * @param b the second value
   * @return the result of a * b, rounded to 2 decimal places
   */
  public static BigDecimal multiplyAndRound(BigDecimal a, BigDecimal b) {
    if (a == null || b == null) {
      return BigDecimal.ZERO;
    }
    return a.multiply(b).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Multiplies two BigDecimal numbers and rounds the result to 2 decimal places using HALF_UP.
   *
   * @param a the first value
   * @param b the second value
   * @return the result of a * b, rounded to 2 decimal places
   */
  public static BigDecimal multiplyAndRound(BigDecimal a, int b) {
    if (a == null) {
      return BigDecimal.ZERO;
    }
    return a.multiply(BigDecimal.valueOf(b)).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Divides one BigDecimal by another and rounds to 2 decimal places using HALF_UP.
   *
   * @param numerator   the numerator
   * @param denominator the denominator
   * @return the result of division, or ZERO if denominator is zero or either is null
   */
  public static BigDecimal divideAndRound(BigDecimal numerator, BigDecimal denominator) {
    if (numerator == null || denominator == null || BigDecimal.ZERO.compareTo(denominator) == 0) {
      return BigDecimal.ZERO;
    }
    return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
  }

  /**
   * Rounds a BigDecimal to 2 decimal places using HALF_UP.
   *
   * @param value the value to round
   * @return the rounded value
   */
  public static BigDecimal round(BigDecimal value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    return value.setScale(2, RoundingMode.HALF_UP);
  }
}
