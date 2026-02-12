package com.commercepal.apiservice.users.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request for OAuth2 users to optionally set a password. Enables traditional login alongside
 * OAuth2.
 */
@Schema(description = "Request payload to set a password for an OAuth2 account")
public record OAuth2SetPasswordRequest(
    @Schema(description = "New password", example = "SecurePass123!@#") @NotBlank(message = "Password is required") @Size(min = 6, max = 128, message = "Password must be at least 6 characters") String password,

    @Schema(description = "Confirm new password", example = "SecurePass123!@#") @NotBlank(message = "Confirm password is required") String confirmPassword) {

}
