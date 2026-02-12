package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.customer.Customer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Response DTO representing a customer entity.
 * <p>
 * This record contains comprehensive customer information including account details, personal
 * information, preferences, and contact information.
 */
@Schema(
    name = "CustomerResponse",
    description = """
        Response payload representing customer information.
        Contains complete customer profile including account details, personal information,
        preferences, and contact information.
        """
)
@Builder
public record CustomerResponse(
    @Schema(
        description = "Customer's first name",
        example = "John"
    )
    String firstName,

    @Schema(
        description = "Customer's last name",
        example = "Doe"
    )
    String lastName,

    @Schema(
        description = """
            Email address associated with the customer account.
            May be null for Ethiopian customers if only phone number was provided.
            """,
        example = "john.doe@example.com"
    )
    String emailAddress,

    @Schema(
        description = """
            Phone number associated with the customer account.
            Required for Ethiopian customers, optional for others.
            """,
        example = "+251912345678"
    )
    String phoneNumber,

    @Schema(
        description = """
            Country of the customer.
            ISO 3166-1 alpha-2 country code.
            """,
        example = "ET"
    )
    String country,

    @Schema(
        description = "City where the customer is located",
        example = "Addis Ababa"
    )
    String city,

    @Schema(
        description = "State or province where the customer is located",
        example = "Addis Ababa"
    )
    String stateProvince,

    @Schema(
        description = """
            Customer's preferred language for communication.
            ISO 639-1 language code (e.g., 'en', 'am').
            """,
        example = "en"
    )
    String preferredLanguage,

    @Schema(
        description = """
            Customer's preferred currency for transactions.
            Supported currency enum.
            """,
        example = "ETB"
    )
    SupportedCurrency preferredCurrency,

    @Schema(
        description = """
            Unique referral code assigned to the customer.
            Generated automatically during registration.
            Can be used for referral programs.
            """,
        example = "REF123456"
    )
    String referralCode
) {

  /**
   * Creates a CustomerResponse from a Customer entity.
   *
   * @param customer the customer entity
   * @return the customer response DTO
   */
  public static CustomerResponse from(Customer customer) {
    if (customer == null) {
      return null;
    }

    return CustomerResponse.builder()
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .emailAddress(
            customer.getCredential() != null ? customer.getCredential().getEmailAddress() : null)
        .phoneNumber(
            customer.getCredential() != null ? customer.getCredential().getPhoneNumber() : null)
        .country(customer.getCountry())
        .city(customer.getCity())
        .stateProvince(customer.getStateProvince())
        .preferredLanguage(customer.getPreferredLanguage())
        .preferredCurrency(customer.getPreferredCurrency())
        .referralCode(customer.getReferralCode())
        .build();
  }

}

