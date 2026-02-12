package com.commercepal.apiservice.users.credential.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for staff to set their own initial password. Enforces strong password policy for admin-side
 * accounts. Used by the authenticated staff member (current user) to set their initial password.
 */
@Schema(description = "Request DTO for staff to set own initial password (strong password required)")
public record StaffInitialPasswordSetRequest(

    @NotBlank(message = "Password is required")
    @Size(min = 12, max = 128, message = "Password must be between 12 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_\\-+=\\[\\]{}|;:,.<>/~`]).{12,128}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&#^()_-+=[]{}|;:,.<>/~`)"
    )
    @Schema(
        description = "Strong password: min 12 chars, must include uppercase, lowercase, digit, and special character",
        example = "AdminSecureP@ss1!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String newPassword,

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirmation of new password", example = "AdminSecureP@ss1!", requiredMode = Schema.RequiredMode.REQUIRED)
    String confirmPassword
) {
}
