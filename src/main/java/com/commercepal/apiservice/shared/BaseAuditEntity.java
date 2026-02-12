package com.commercepal.apiservice.shared;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base audit entity that provides comprehensive auditing fields. All domain entities should extend
 * this class to inherit audit capabilities.
 * <p>
 * This entity includes: - Creation and modification timestamps - User tracking for creation and
 * updates - Soft delete functionality - Version control for optimistic locking - IP address
 * tracking for security - Status tracking for workflow management
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  @Version
  @Column(name = "version", nullable = false)
  private final Long version = 0L;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  @Column(name = "created_ip", length = 45)
  private String createdIp;

  @Column(name = "updated_ip", length = 45)
  private String updatedIp;

  @Column(name = "remarks", length = 500)
  private String remarks;


  //Safe boolean getters (primitive + null-safe)
  public boolean isDeleted() {
    return Boolean.TRUE.equals(isDeleted);
  }

  // Utility methods for soft delete
  public void softDelete(String deletedBy) {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }

  public void restore() {
    this.isDeleted = false;
    this.deletedAt = null;
    this.deletedBy = null;
  }

  // Equals and HashCode
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BaseAuditEntity that = (BaseAuditEntity) o;

    // For entities with null IDs (not yet persisted), compare by reference
    if (id == null || that.id == null) {
      return false;
    }

    // For persisted entities, compare by ID
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  // ToString
  @Override
  public String toString() {
    return "BaseAuditEntity{" + "id=" + id + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
        + ", createdBy='" + createdBy + '\'' + ", updatedBy='" + updatedBy + '\'' + ", version="
        + version + ", isDeleted=" + isDeleted + ", remarks='" + remarks + '\'' + '}';
  }
}
