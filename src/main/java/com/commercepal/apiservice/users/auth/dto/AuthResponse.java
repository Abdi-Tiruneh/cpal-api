package com.commercepal.apiservice.users.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authentication response with access token and refresh token.
 * <p>
 * This response is returned after successful authentication or token refresh operations. Contains
 * the access token for API authorization and a refresh token for obtaining new access tokens.
 */
@Schema(
    name = "AuthResponse",
    description = "Authentication response containing access token, refresh token, token type, and expiration information."
)
public record AuthResponse(
    @Schema(
        description = "JWT access token used for authenticating API requests. Include this token in the Authorization header as 'Bearer {accessToken}'.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ..."
    )
    String accessToken,

    @Schema(
        description = "JWT refresh token used to obtain a new access token when the current one expires. Store this securely and use it with the refresh token endpoint.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ..."
    )
    String refreshToken,

    @Schema(
        description = "Type of token. Always 'Bearer' for JWT tokens.",
        example = "Bearer"
    )
    String tokenType,

    @Schema(
        description = "Access token expiration time in seconds. After this duration, the access token will expire and a new one must be obtained using the refresh token.",
        example = "1800"
    )
    long expiresIn
) {

}
