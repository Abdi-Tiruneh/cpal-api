package com.commercepal.apiservice.users.auth.dto;

import com.commercepal.apiservice.users.auth.ProfileCompletionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for OAuth2 login operations.
 */
@Schema(description = "Response payload for OAuth2 login")
public record OAuth2LoginResponse(
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
    long expiresIn,

    @Schema(description = "True if a new user was created during this login", example = "true") boolean isNewUser,

    @Schema(description = "True if the user needs to complete their profile (missing phone/email)", example = "true") boolean requiresProfileCompletion,

    @Schema(description = "True if the user has a password set", example = "false") boolean hasPassword,

    @Schema(description = "Status indicating what is missing from profile", example = "MISSING_PHONE") ProfileCompletionStatus profileStatus) {

}
