package com.commercepal.apiservice.users.auth.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.users.enums.IdentityProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for OAuth2 login. Contains provider token and user info directly from client (or
 * verified ID token).
 */
@Schema(description = "Request payload for OAuth2 login")
public record OAuth2LoginRequest(
    @Schema(description = "Identity provider used for login", example = "GOOGLE") @NotNull(message = "Identity provider is required") IdentityProvider provider,

    @Schema(description = "Unique user ID from the identity provider", example = "1234567890") @NotBlank(message = "Provider user ID is required") String providerUserId,

    @Schema(description = "Email address from provider (if available)", example = "user@example.com") String email,

    @Schema(description = "First name from provider", example = "John") @NotBlank(message = "First name is required") String firstName,

    @Schema(description = "Last name from provider", example = "Doe") String lastName,

    @Schema(description = "Device ID for mobile apps", example = "device-uuid-123") String deviceId,

    @Schema(description = "Channel through which login is attempted", example = "WEB") @NotNull(message = "Channel is required") Channel channel) {

}
