package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating customer information.
 * <p>
 * This record represents the data that can be updated for an existing customer. All fields are
 * optional, allowing partial updates. Only provided fields will be updated.
 */
@Schema(
    name = "CustomerUpdateRequest",
    description = """
        Request payload for updating customer information.
        All fields are optional, allowing partial updates.
        Only provided fields will be updated in the customer record.
        """
)
public record CustomerUpdateRequest(

    @Schema(
        description = """
            Customer's first name.
            If provided, will update the customer's first name.
            """,
        example = "John"
    )
    @Size(max = 120, message = "First name must not exceed 120 characters")
    String firstName,

    @Schema(
        description = """
            Customer's last name.
            If provided, will update the customer's last name.
            """,
        example = "Doe"
    )
    @Size(max = 120, message = "Last name must not exceed 120 characters")
    String lastName,

    @Schema(
        description = """
            Country of the customer.
            If provided, will update the customer's country.
            Must be a valid ISO 3166-1 alpha-2 country code.
            """,
        example = "ET"
    )
    SupportedCountry country,

    @Schema(
        description = """
            City where the customer is located.
            If provided, will update the customer's city.
            """,
        example = "Addis Ababa"
    )
    @Size(max = 120, message = "City must not exceed 120 characters")
    String city,

    @Schema(
        description = """
            State or province where the customer is located.
            If provided, will update the customer's state or province.
            """,
        example = "Addis Ababa"
    )
    @Size(max = 50, message = "State or province must not exceed 50 characters")
    String stateProvince,

    @Schema(
        description = """
            Customer's preferred language for communication.
            If provided, will update the customer's preferred language.
            Should be an ISO 639-1 language code (e.g., 'en', 'am').
            """,
        example = "en"
    )
    @Size(max = 8, message = "Preferred language must not exceed 8 characters")
    @Pattern(
        regexp = "^[a-z]{2}(-[A-Z]{2})?$",
        message = "Preferred language must be a valid ISO 639-1 language code"
    )
    String preferredLanguage,

    @Schema(
        description = """
            Customer's preferred currency for transactions.
            If provided, will update the customer's preferred currency.
            Must be a supported currency.
            """,
        example = "ETB"
    )
    SupportedCurrency preferredCurrency,

    @Schema(
        description = """
            Customer notes or additional information.
            If provided, will update the customer notes.
            Internal notes that can be viewed by the customer.
            """,
        example = "Preferred contact method: SMS"
    )
    @Size(max = 1000, message = "Customer notes must not exceed 1000 characters")
    String customerNotes
) {

}

