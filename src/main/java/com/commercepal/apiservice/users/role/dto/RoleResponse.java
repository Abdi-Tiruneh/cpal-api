package com.commercepal.apiservice.users.role.dto;

import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * RoleResponse
 * <p>
 * Response DTO representing a role definition with all its properties including audit metadata.
 * Used to return role information to clients.
 */
@Schema(description = "Response DTO containing role definition details with audit information")
@Builder
public record RoleResponse(
    @Schema(
        description = "Role code from the RoleCode enumeration",
        example = "ROLE_ADMIN"
    )
    RoleCode code,

    @Schema(
        description = "Human-readable display name for the role",
        example = "Administrator"
    )
    String name,

    @Schema(
        description = "Detailed description of the role's purpose and permissions",
        example = "Administrative access to manage system settings and configurations"
    )
    String description
) {

  public static RoleResponse from(RoleDefinition role) {
    return new RoleResponse(
        role.getCode(),
        role.getName(),
        role.getDescription()
    );
  }
}

