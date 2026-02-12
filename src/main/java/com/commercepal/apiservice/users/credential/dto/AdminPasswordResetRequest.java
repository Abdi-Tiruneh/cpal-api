package com.commercepal.apiservice.users.credential.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for admin to reset user password.
 */
@Schema(description = "Request DTO for admin to reset user password")
public record AdminPasswordResetRequest(

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user whose password needs to be reset",
        example = "123")
    Long userId,

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
    String newPassword,

    @Schema(description = "Reason for password reset",
        example = "User requested password reset")
    String reason,

    @Schema(description = "Whether to require password change on next login",
        example = "true")
    Boolean requirePasswordChange,

    @Schema(
        description = """
            Channel through which the admin is accessing the application.
            Examples: WEB, MOBILE_APP_ANDROID, MOBILE_APP_IOS, MOBILE_APP_WEBVIEW, API, ADMIN_PORTAL
            """,
        example = "ADMIN_PORTAL"
    )
    @NotNull(message = "Channel is required.")
    Channel channel
) {

}

