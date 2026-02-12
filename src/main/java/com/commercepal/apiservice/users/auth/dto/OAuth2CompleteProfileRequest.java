package com.commercepal.apiservice.users.auth.dto;

import com.commercepal.apiservice.shared.enums.SupportedCountry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Follow-up request to complete OAuth2 user profile. Used when phone number or email wasn't
 * provided during initial OAuth2 login or implied by the provider.
 */
@Schema(description = "Request payload to complete user profile with missing contact info")
public record OAuth2CompleteProfileRequest(

    @Schema(description = "Phone number (required for Ethiopia). Format: +251...", example = "+251911223344") @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format") String phoneNumber,

    @Schema(description = "Email address (required for non-Ethiopia)", example = "user@example.com") @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format") String email,

    @Schema(description = "User's country for localization and validation rules", example = "ET") @NotNull(message = "Country is required") SupportedCountry country) {

}
