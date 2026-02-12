package com.commercepal.apiservice.payments.core;

import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public final class PhoneValidationUtil {

  /**
   * Accepted formats:
   *  - 2519XXXXXXXX
   *  - 2517XXXXXXXX
   *  - +2519XXXXXXXX
   *  - +2517XXXXXXXX
   */
  private static final String PHONE_PATTERN =
      "^(\\+?251)(9|7)\\d{8}$";

  private static final int PHONE_LENGTH_WITHOUT_PLUS = 12; // 251 + 9 digits
  private static final int PHONE_LENGTH_WITH_PLUS = 13;    // +251 + 9 digits

  private static final String PHONE_VALIDATION_ERROR_MESSAGE =
      "Invalid Ethiopian phone number. Valid formats: 2519XXXXXXXX, 2517XXXXXXXX, +2519XXXXXXXX, +2517XXXXXXXX";

  private PhoneValidationUtil() {
    throw new UnsupportedOperationException("Utility class - cannot be instantiated");
  }

  /**
   * Validates Ethiopian phone number.
   * Accepts both with and without '+' prefix.
   */
  public static void validate(String phoneNumber) {
    if (!StringUtils.hasText(phoneNumber)) {
      log.warn("Phone number validation failed - empty input");
      throw new BadRequestException("Phone number is required");
    }

    String trimmed = phoneNumber.trim();

    // Allow '+' only at the beginning
    if (!trimmed.matches("^\\+?\\d+$")) {
      log.warn("Phone number validation failed - non-numeric characters - phone: {}",
          maskPhoneNumber(trimmed));
      throw new BadRequestException(PHONE_VALIDATION_ERROR_MESSAGE);
    }

    int length = trimmed.length();
    if (length != PHONE_LENGTH_WITHOUT_PLUS && length != PHONE_LENGTH_WITH_PLUS) {
      log.warn("Phone number validation failed - invalid length - phone: {}, length: {}",
          maskPhoneNumber(trimmed), length);
      throw new BadRequestException(PHONE_VALIDATION_ERROR_MESSAGE);
    }

    if (!trimmed.matches(PHONE_PATTERN)) {
      log.warn("Phone number validation failed - pattern mismatch - phone: {}",
          maskPhoneNumber(trimmed));
      throw new BadRequestException(PHONE_VALIDATION_ERROR_MESSAGE);
    }

    log.debug("Phone number validation passed - phone: {}", maskPhoneNumber(trimmed));
  }

  /**
   * Returns true if phone number is valid Ethiopian mobile number.
   */
  public static boolean isValid(String phoneNumber) {
    try {
      validate(phoneNumber);
      return true;
    } catch (BadRequestException ex) {
      return false;
    }
  }

  /**
   * Validates and returns phone number in +251XXXXXXXXX format.
   */
  public static String normalizeWithPlus(String phoneNumber) {
    validate(phoneNumber);

    String trimmed = phoneNumber.trim();
    if (trimmed.startsWith("+")) {
      return trimmed;
    }
    return "+" + trimmed;
  }

  /**
   * Validates and returns phone number in 251XXXXXXXXX format (without '+').
   */
  public static String normalizeWithoutPlus(String phoneNumber) {
    validate(phoneNumber);

    String trimmed = phoneNumber.trim();
    if (trimmed.startsWith("+")) {
      return trimmed.substring(1);
    }
    return trimmed;
  }

  /**
   * Masks phone number for safe logging.
   */
  public static String maskPhoneNumber(String phoneNumber) {
    if (!StringUtils.hasText(phoneNumber) || phoneNumber.length() < 6) {
      return "****";
    }
    int length = phoneNumber.length();
    return phoneNumber.substring(0, 4)
        + "*".repeat(length - 6)
        + phoneNumber.substring(length - 2);
  }
}
