package com.commercepal.apiservice.users.auth.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Represents a login request for all user types.
 * <p>
 * This record supports authentication for all users (OldCustomer, Admin, Merchant, etc.) using
 * either an email address or phone number, along with a secure password.
 */
@Schema(
    name = "LoginRequest",
    description = "Request payload for authenticating users (OldCustomer, Admin, Merchant, etc.) using email or phone number and password."
)
public record LoginRequest(

    @Schema(
        description = """
            Login identifier used for authentication.
            Must be either a valid email address or a phone number (E.164 format recommended).
            Example formats:
            - "user@example.com"
            - "+14155552671"
            """,
        example = "user@example.com"
    )
    @NotBlank(message = "Login identifier cannot be blank.")
    @Pattern(
        regexp = "^(\\+?[0-9]{7,15}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})$",
        message = "Login identifier must be a valid email address or phone number."
    )
    String loginIdentifier,

    @Schema(
        description = """
            Password used for authentication.
            Must meet the application's password security requirements.
            """,
        example = "SecurePassword123!"
    )
    @NotBlank(message = "Password cannot be blank.")
    String password,

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
