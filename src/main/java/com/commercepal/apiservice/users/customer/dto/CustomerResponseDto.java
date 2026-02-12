package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

/**
 * Customer-facing profile response delivering a clear, secure, and comprehensive snapshot of the
 * customer's account. This DTO is specifically designed for self-service experiences where
 * customers view their own information. Sensitive operational fields (admin notes, security flags,
 * raw identifiers) are intentionally excluded or masked.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "CustomerProfileResponse", description = "Customer-facing profile view with personal, contact, and verification details.")
@Builder
public record CustomerResponseDto(

    @Schema(description = "Primary personal profile information") PersonalInfo personal,

    @Schema(description = "Primary and secondary contact channels") ContactInfo contact,

    @Schema(description = "Residential address synced with the user profile") AddressInfo residentialAddress,

    @Schema(description = "Customer language, time, and notification preferences") PreferenceInfo preferences,

    @Schema(description = "Customer-facing notes or reminders", example = "Remember to update proof of address before 30 June.") String customerNotes) {

  @Schema(description = "Personal profile information block")
  @Builder
  public record PersonalInfo(

      @Schema(description = "First name", example = "Liya") String firstName,

      @Schema(description = "Middle name", example = "Worku") String middleName,

      @Schema(description = "Last name", example = "Bekele") String lastName,

      @Schema(description = "Date of birth", example = "1994-05-12") LocalDate dateOfBirth,

      @Schema(description = "Nationality", example = "Ethiopian") String nationality,

      @Schema(description = "Profile image URL", example = "https://cdn.fastpay.com/profile/liya.png") String profileImageUrl) {

  }

  @Schema(description = "Contact methods for the customer")
  @Builder
  public record ContactInfo(
      @Schema(description = "Primary email address", example = "liya@example.com") String email,

      @Schema(description = "Email verification status", example = "true") Boolean emailVerified,

      @Schema(description = "Primary phone number in E.164 format", example = "+251911223344") String phonePrimary,

      @Schema(description = "Primary phone verification status", example = "true") Boolean phonePrimaryVerified,

      @Schema(description = "Backup phone number", example = "+251922334455") String phoneSecondary,

      @Schema(description = "Secondary phone verification status", example = "false") Boolean phoneSecondaryVerified) {

  }

  @Schema(description = "Structured address information")
  @Builder
  public record AddressInfo(
      @Schema(description = "Address line 1", example = "123 Bole Road") String line1,

      @Schema(description = "Address line 2", example = "Apartment 5B") String line2,

      @Schema(description = "City", example = "Addis Ababa") String city,

      @Schema(description = "State or province", example = "Addis Ababa") String stateProvince,

      @Schema(description = "Postal or ZIP code", example = "1000") String postalCode,

      @Schema(description = "Country", example = "Ethiopia") String country,

      @Schema(description = "Formatted display address", example = "123 Bole Road, Apartment 5B, Addis Ababa 1000, Ethiopia") String formatted) {

  }

  @Schema(description = "Customer notification and localisation preferences")
  @Builder
  public record PreferenceInfo(

      @Schema(description = "Preferred currency code", example = "ETB") SupportedCurrency currency,

      @Schema(description = "Receive email notifications", example = "true") Boolean notificationEmail,

      @Schema(description = "Receive SMS notifications", example = "false") Boolean notificationSms,

      @Schema(description = "Receive push notifications", example = "true") Boolean notificationPush) {

  }
}
