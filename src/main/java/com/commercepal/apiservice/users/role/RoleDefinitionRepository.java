package com.commercepal.apiservice.users.role;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for role definition persistence. Provides data access methods for role management.
 */
@Repository
public interface RoleDefinitionRepository extends JpaRepository<RoleDefinition, Long> {

  /**
   * Find role definition by role code. Used to map between UserRole enum and RoleDefinition
   * entity.
   *
   * @param code the role code to search for
   * @return Optional containing the RoleDefinition if found
   */
  Optional<RoleDefinition> findByCode(RoleCode code);

  /**
   * Check if a role definition exists by code.
   *
   * @param code the role code to check
   * @return true if the role exists, false otherwise
   */
  boolean existsByCode(RoleCode code);

  /**
   * Find role definition by code name (string). Useful for migration from enum-based roles.
   *
   * @param codeName the role code name as string (e.g., "ROLE_CUSTOMER")
   * @return Optional containing the RoleDefinition if found
   */
  default Optional<RoleDefinition> findByCodeName(String codeName) {
    try {
      RoleCode roleCode = RoleCode.valueOf(codeName);
      return findByCode(roleCode);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
