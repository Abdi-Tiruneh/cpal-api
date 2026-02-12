package com.commercepal.apiservice.promotions.affiliate.user.dto;

import com.commercepal.apiservice.promotions.affiliate.commission.Commission;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.Channel;
import jakarta.validation.constraints.*;

/**
 * DTO for registering a **new affiliate without an existing customer profile**.
 * Includes personal details, security info, and affiliate program details.
 */
public record AffiliateAddRequest(

        // User Personal Details
        @NotBlank(message = "First name is required") String firstName,

        @NotBlank(message = "Last name is required") String lastName,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Phone number is required") @Size(min = 5, max = 13, message = "Phone number must be between 5 and 13 digits") String phoneNumber,

        @NotBlank(message = "Country code is required") @Pattern(regexp = "\\d{1,4}", message = "Invalid country code") String countryCode,

        @NotNull(message = "Country is required") SupportedCountry country,

        // Security Details
        @NotBlank(message = "Password is required") @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters") String password,

        @NotBlank(message = "Confirm password is required") @Size(min = 6, max = 50, message = "Confirm password must be between 6 and 50 characters") String confirmPassword,

        // Affiliate Program Details
        @NotNull(message = "Commission type is required") Commission commissionType,

        @Size(min = 4, max = 8, message = "Referral code must be between 4 and 8 characters") @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Referral code must be alphanumeric only") String referralCode,

        @NotNull(message = "Registration channel is required") Channel registrationChannel,

        // Optional Fields
        String deviceId)
{
}
