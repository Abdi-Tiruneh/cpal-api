package com.commercepal.apiservice.users.role;

import com.commercepal.apiservice.users.role.dto.RoleInitResponse;
import com.commercepal.apiservice.users.role.dto.RoleResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RoleDefinitionController
 * <p>
 * REST API controller for listing role definitions. Roles are predefined in the system;
 * this API returns all roles for role-based access control (RBAC).
 */
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Role Management",
    description = "APIs for listing role definitions used in role-based access control (RBAC)."
)
public class RoleDefinitionController {

  private final RoleDefinitionService roleDefinitionService;

  @Operation(
      summary = "Get all roles",
      description = "Retrieves a list of all role definitions in the system.",
      tags = {"Role Management"}
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "List of roles retrieved successfully",
          content = @Content(schema = @Schema(implementation = RoleResponse.class))
      )
  })
  @GetMapping
  public ResponseEntity<ResponseWrapper<List<RoleResponse>>> getAllRoles() {
    return ResponseWrapper.success(roleDefinitionService.findAllRoles());
  }

  @Operation(
      summary = "Initialize roles",
      description = "Ensures a role definition exists for every predefined role code. "
          + "Creates any missing roles; existing roles are left unchanged. Idempotent.",
      tags = {"Role Management"}
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Roles initialized successfully",
          content = @Content(schema = @Schema(implementation = RoleInitResponse.class))
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - admin authority required")
  })
  @PostMapping("/init")
  public ResponseEntity<ResponseWrapper<RoleInitResponse>> initRoles() {
    return ResponseWrapper.success("Roles initialized", roleDefinitionService.ensureRolesFromRoleCodes());
  }
}
