package com.commercepal.apiservice.users.auth.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for refreshing authentication token.
 */
@Schema(
    name = "RefreshTokenRequest",
    description = "Request payload for refreshing access token using refresh token."
)
public record RefreshTokenRequest(
    @Schema(
        description = "Refresh token used to obtain a new access token",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    @NotBlank(message = "Refresh token is required")
    String refreshToken,

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

