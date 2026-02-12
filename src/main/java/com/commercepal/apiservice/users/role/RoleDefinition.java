package com.commercepal.apiservice.users.role;

import com.commercepal.apiservice.shared.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Role definition entity representing a role in the system. Used in ManyToMany relationship with
 * Credential.
 */
@Setter
@Getter
@Entity
@Table(name = "role_definition", uniqueConstraints = {
    @UniqueConstraint(name = "uk_role_definition_code", columnNames = "code")
}, indexes = {
    @Index(name = "idx_role_definition_code", columnList = "code")
})
public class RoleDefinition extends BaseAuditEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "code", nullable = false, unique = true, length = 64)
  private RoleCode code;

  @Column(name = "name", nullable = false, length = 128)
  private String name;

  @Column(name = "description", length = 512)
  private String description;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  protected RoleDefinition() {
  }

  public RoleDefinition(RoleCode code, String name, String description) {
    this.code = code;
    this.name = name;
    this.description = description;
  }

  public static RoleDefinition create(RoleCode code, String name, String description) {
    return new RoleDefinition(code, name, description);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoleDefinition that)) {
      return false;
    }
    return code == that.code;
  }

  @Override
  public int hashCode() {
    return parseCodeHashCode();
  }

  private int parseCodeHashCode() {
    return code != null ? code.hashCode() : 0;
  }
}
