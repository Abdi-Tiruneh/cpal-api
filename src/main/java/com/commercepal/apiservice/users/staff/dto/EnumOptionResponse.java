package com.commercepal.apiservice.users.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum option for dropdowns and filters (code, label, description).
 */
@Schema(description = "Enum option: code, display label, and description")
public record EnumOptionResponse(
    @Schema(description = "Enum constant name (API value)")
    String name,
    @Schema(description = "Display label for UI")
    String displayName,
    @Schema(description = "Human-readable description")
    String description
) {}
