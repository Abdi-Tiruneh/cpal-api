package com.commercepal.apiservice.users.credential.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for password reset using verification token (forgot password flow).
 */
@Schema(description = "Request DTO for resetting password using verification token")
public record PasswordResetRequest(

    @NotBlank(message = "Target (email or phone) is required")
    @Pattern(
        regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\+?[1-9]\\d{1,14})$",
        message = "Target must be a valid email address or phone number (E.164 format)"
    )
    @Schema(description = "Email address or phone number associated with the account",
        example = "user@example.com or +1234567890")
    String target,

    @NotBlank(message = "Verification token is required")
    @Pattern(regexp = "^\\d{6}$", message = "Verification token must be a 6-digit number")
    @Schema(description = "6-digit verification token received via email or SMS",
        example = "123456")
    String verificationToken,

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    @Schema(description = "New password (6-128 characters)",
        example = "NewSecurePass123!")
    String newPassword,

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm password (must match new password)",
        example = "NewSecurePass123!")
    String confirmPassword,

    @Schema(
        description = """
            Channel through which the user is accessing the application.
            Examples: WEB, MOBILE_APP_ANDROID, MOBILE_APP_IOS, MOBILE_APP_WEBVIEW, API, ADMIN_PORTAL
            """,
        example = "WEB"
    )
    @NotNull(message = "Channel is required.")
    Channel channel
) {

}

