package com.commercepal.apiservice.users.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for the role initialization endpoint. Indicates how many roles were created vs
 * already existing.
 */
@Schema(description = "Result of role initialization: counts of created and existing roles")
public record RoleInitResponse(
    @Schema(description = "Number of roles created", example = "5")
    int createdCount,
    @Schema(description = "Number of roles that already existed", example = "15")
    int existingCount
) {

  @Schema(description = "Total roles considered (created + existing)", example = "20")
  public int totalCount() {
    return createdCount + existingCount;
  }
}
