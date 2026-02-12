package com.commercepal.apiservice.users.credential.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for user password change operations.
 */
@Schema(description = "Request DTO for password change operations")
public record PasswordChangeRequest(

    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password for verification",
        example = "CurrentP@ssw0rd!")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 128, message = "New password must be between 6 and 128 characters")
    @Schema(description = "New password (6-128 characters)",
        example = "NewSecureP@ssw0rd!")
    String newPassword,

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirmation of new password",
        example = "NewSecureP@ssw0rd!")
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
