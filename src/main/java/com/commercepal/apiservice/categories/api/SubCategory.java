package com.commercepal.apiservice.categories.api;

import com.commercepal.apiservice.categories.enums.CategoryStatus;
import com.commercepal.apiservice.shared.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SubCategory Entity
 * <p>
 * Represents a subcategory that belongs to a parent category.
 * Supports status management for enabling/disabling subcategories.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "sub_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_subcategory_name_category", columnNames = {"name", "category_id"})
    },
    indexes = {
        @Index(name = "idx_subcategory_status", columnList = "status"),
        @Index(name = "idx_subcategory_category_id", columnList = "category_id"),
        @Index(name = "idx_subcategory_name", columnList = "name"),
        @Index(name = "idx_subcategory_slug", columnList = "slug"),
        @Index(name = "idx_subcategory_provider_id", columnList = "provider_id")
    }
)
public class SubCategory extends BaseAuditEntity {

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "slug", nullable = false, length = 255)
  private String slug;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private CategoryStatus status = CategoryStatus.ACTIVE;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "provider_id", nullable = false, length = 255)
  private String providerId;

  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subcategory_category"))
  private Category category;
}
