package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request DTO for customer registration.
 * <p>
 * This record represents the data required to register a new customer in the system. The validation
 * rules are country-specific:
 * <ul>
 *   <li>Ethiopia (ET): Phone number is mandatory, email is optional</li>
 *   <li>Other countries: Email is mandatory, phone number is optional</li>
 * </ul>
 */
@Schema(
    name = "CustomerRegistrationRequest",
    description = """
        Request payload for registering a new customer.
        Country-specific validation applies:
        - Ethiopia (ET): Phone number is required, email is optional
        - Other countries: Email is required, phone number is optional
        """
)
@Builder
public record CustomerRegistrationRequest(

    @Schema(
        description = """
            Email address of the customer.
            Required for all countries except Ethiopia (ET).
            Must be a valid email format.
            """,
        example = "john.doe@example.com"
    )
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "Email address must be in a valid format"
    )
    String emailAddress,

    @Schema(
        description = """
            Phone number of the customer.
            Required for Ethiopia (ET), optional for other countries.
            Can include country code prefix (e.g., +251) or be without it.
            Phone number length is validated according to country-specific rules.
            """,
        example = "+251912345678"
    )
    @Pattern(
        regexp = "^\\+?[0-9]{7,15}$",
        message = "Phone number must contain only digits and optional '+' prefix"
    )
    String phoneNumber,

    @Schema(
        description = """
            Password for the customer account.
            Must meet the application's password security requirements.
            """,
        example = "SecurePassword123!"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    String password,

    @Schema(
        description = """
            Confirmation of the password.
            Must match the password field exactly.
            """,
        example = "SecurePassword123!"
    )
    @NotBlank(message = "Confirm password is required")
    String confirmPassword,

    @Schema(
        description = """
            First name of the customer.
            Required field for customer identification.
            """,
        example = "John"
    )
    @NotBlank(message = "First name is required")
    @Size(max = 120, message = "First name must not exceed 120 characters")
    String firstName,

    @Schema(
        description = """
            Last name of the customer.
            Optional field for customer identification.
            """,
        example = "Doe"
    )
    @Size(max = 120, message = "Last name must not exceed 120 characters")
    String lastName,

    @Schema(
        description = """
            Country of the customer.
            Required field that determines validation rules for email/phone.
            Must be a valid ISO 3166-1 alpha-2 country code.
            """,
        example = "ET"
    )
    @NotNull(message = "Country is required")
    SupportedCountry country,

    @Schema(
        description = """
            Channel through which the customer is registering.
            Represents the entry point or platform used for registration.
            Examples: WEB, MOBILE_APP_ANDROID, MOBILE_APP_IOS, MOBILE_APP_WEBVIEW, API, ADMIN_PORTAL
            """,
        example = "WEB"
    )
    @NotNull(message = "Registration channel is required")
    Channel registrationChannel

) {

}

