package com.commercepal.apiservice.users.role;

import com.commercepal.apiservice.users.role.dto.RoleInitResponse;
import com.commercepal.apiservice.users.role.dto.RoleResponse;
import java.util.List;
import java.util.Set;

/**
 * Service for role definitions. Roles are predefined; this service provides listing and lookup by
 * code for use in registration and RBAC.
 */
public interface RoleDefinitionService {

  /**
   * Returns all role definitions in the system.
   */
  List<RoleResponse> findAllRoles();

  /**
   * Get role definitions by role codes. Throws IllegalArgumentException if any role code is not
   * found.
   */
  Set<RoleDefinition> getRoleDefinitions(Set<RoleCode> roleCodes);

  /**
   * Ensures a role definition exists for every predefined role. Creates missing roles and skips
   * those that already exist. Returns counts of created and existing roles.
   */
  RoleInitResponse ensureRolesFromRoleCodes();
}
