package com.commercepal.apiservice.utils;

import com.commercepal.apiservice.shared.enums.SupportedCountry;

/**
 * Utility class for phone number validation operations.
 * <p>
 * Provides reusable methods for validating phone numbers according to country-specific rules and
 * identifier requirements. Supports international phone number formats with or without country code
 * prefixes.
 * <p>
 * Usage examples:
 * <pre>
 * // Validate phone number length
 * PhoneValidationUtil.validatePhoneNumberLength("912345678", SupportedCountry.ETHIOPIA);
 *
 * // Normalize phone number
 * String normalized = PhoneValidationUtil.normalizePhoneNumber("912345678", SupportedCountry.ETHIOPIA);
 * // Result: "+251912345678"
 *
 * // Extract phone without code
 * String phoneOnly = PhoneValidationUtil.extractPhoneNumberWithoutCode("+251912345678", SupportedCountry.ETHIOPIA);
 * // Result: "912345678"
 *
 * // Validate country-specific identifiers
 * PhoneValidationUtil.validateCountrySpecificIdentifiers("user@email.com", "912345678", SupportedCountry.KENYA);
 *
 * // Check validity without throwing exceptions
 * boolean isValid = PhoneValidationUtil.isValidPhoneNumber("912345678", SupportedCountry.ETHIOPIA);
 * </pre>
 */
public class PhoneValidationUtil {

  /**
   * Validates phone number length according to country rules. Handles phone numbers with or without
   * country code prefix.
   *
   * @param phoneNumber the phone number to validate
   * @param country     the country to validate against
   * @throws IllegalArgumentException if phone number length is invalid
   */
  public static void validatePhoneNumberLength(String phoneNumber, SupportedCountry country) {
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("Phone number cannot be null or empty");
    }

    if (country == null) {
      throw new IllegalArgumentException("Country is required for phone validation");
    }

    String normalizedPhone = phoneNumber.trim();

    // If it does not contain +, add it
    if (!normalizedPhone.contains("+")) {
      normalizedPhone = "+" + normalizedPhone;
    }

    String countryPhoneCode = country.getPhoneCode();
    String phoneWithoutCountryCode;

    // Check if phone number starts with country code
    if (normalizedPhone.startsWith(countryPhoneCode)) {
      // Remove country code prefix
      phoneWithoutCountryCode = normalizedPhone.substring(countryPhoneCode.length());
    } else {
      // Phone number doesn't start with country code, use as is
      phoneWithoutCountryCode = normalizedPhone;
    }

    // Validate length
    int expectedLength = country.getPhoneNumberLength();
    if (phoneWithoutCountryCode.length() != expectedLength) {
      throw new IllegalArgumentException(
          String.format("Phone number must be %d digits for %s", expectedLength,
              country.getDisplayName()));
    }
  }

  /**
   * Normalizes a phone number by ensuring it has the country code prefix. If the phone number
   * doesn't start with +, it adds the country's phone code.
   *
   * @param phoneNumber the phone number to normalize
   * @param country     the country for phone code reference
   * @return normalized phone number with country code
   */
  public static String normalizePhoneNumber(String phoneNumber, SupportedCountry country) {
    if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
      return phoneNumber;
    }

    if (country == null) {
      return phoneNumber;
    }

    String normalizedPhone = phoneNumber.trim();

    // If it does not contain +, add country code
    if (!normalizedPhone.contains("+")) {
      normalizedPhone = country.getPhoneCode() + normalizedPhone;
    }

    return normalizedPhone;
  }

  /**
   * Extracts the phone number without country code prefix. If the phone number starts with the
   * country's phone code, it removes the prefix.
   *
   * @param phoneNumber the phone number to extract from
   * @param country     the country for phone code reference
   * @return phone number without country code prefix
   */
  public static String extractPhoneNumberWithoutCode(String phoneNumber, SupportedCountry country) {
    if (phoneNumber == null || phoneNumber.trim().isEmpty() || country == null) {
      return phoneNumber;
    }

    String normalizedPhone = phoneNumber.trim();

    // If it does not contain +, add it first
    if (!normalizedPhone.contains("+")) {
      normalizedPhone = "+" + normalizedPhone;
    }

    String countryPhoneCode = country.getPhoneCode();

    // Check if phone number starts with country code
    if (normalizedPhone.startsWith(countryPhoneCode)) {
      // Remove country code prefix
      return normalizedPhone.substring(countryPhoneCode.length());
    }

    // Return as is if it doesn't start with country code
    return normalizedPhone;
  }

  /**
   * Validates country-specific identifier requirements.
   * <p>
   * - Ethiopia (ET): phone is mandatory, email is optional - Other countries: email is mandatory,
   * phone is optional
   *
   * @param emailAddress the email address
   * @param phoneNumber  the phone number
   * @param country      the country
   * @throws IllegalArgumentException if identifier requirements are not met
   */
  public static void validateCountrySpecificIdentifiers(String emailAddress, String phoneNumber,
      SupportedCountry country) {
    if (country == null) {
      throw new IllegalArgumentException("Country is required");
    }

    boolean hasEmail = emailAddress != null && !emailAddress.trim().isBlank();
    boolean hasPhone = phoneNumber != null && !phoneNumber.trim().isBlank();

    // Ethiopia: phone is mandatory, email is optional
    if (country == SupportedCountry.ETHIOPIA) {
      if (!hasPhone) {
        throw new IllegalArgumentException("Phone number is required for Ethiopian customers");
      }
    } else {
      // Other countries: email is mandatory, phone is optional
      if (!hasEmail) {
        throw new IllegalArgumentException(
            "Email address is required for customers from " + country.getDisplayName());
      }
    }
  }

  /**
   * Checks if a phone number is valid for the given country. This is a non-throwing version that
   * returns boolean instead of throwing exceptions.
   *
   * @param phoneNumber the phone number to validate
   * @param country     the country to validate against
   * @return true if phone number is valid, false otherwise
   */
  public static boolean isValidPhoneNumber(String phoneNumber, SupportedCountry country) {
    try {
      validatePhoneNumberLength(phoneNumber, country);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Checks if identifiers meet country-specific requirements. This is a non-throwing version that
   * returns boolean instead of throwing exceptions.
   *
   * @param emailAddress the email address
   * @param phoneNumber  the phone number
   * @param country      the country
   * @return true if identifiers are valid, false otherwise
   */
  public static boolean areIdentifiersValid(String emailAddress, String phoneNumber,
      SupportedCountry country) {
    try {
      validateCountrySpecificIdentifiers(emailAddress, phoneNumber, country);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
