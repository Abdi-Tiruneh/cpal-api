package com.commercepal.apiservice.users.credential.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for initiating forgot password flow.
 */
@Schema(description = "Request DTO for initiating forgot password flow")
public record ForgotPasswordRequest(

    @NotBlank(message = "Email or phone number is required")
    @Pattern(
        regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\+?[1-9]\\d{1,14})$",
        message = "Must be a valid email address or phone number (E.164 format)"
    )
    @Schema(description = "Email address or phone number associated with the account",
        example = "user@example.com or +1234567890")
    String emailOrPhone,

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

