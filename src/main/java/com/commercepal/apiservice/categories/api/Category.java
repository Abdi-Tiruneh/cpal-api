package com.commercepal.apiservice.categories.api;

import com.commercepal.apiservice.categories.enums.CategoryStatus;
import com.commercepal.apiservice.shared.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Category Entity
 * <p>
 * Represents a product category in the system. Categories can have multiple subcategories.
 * Supports status management for enabling/disabling categories.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name", columnNames = "name"),
        @UniqueConstraint(name = "uk_category_code", columnNames = "code")
    },
    indexes = {
        @Index(name = "idx_category_status", columnList = "status"),
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_category_slug", columnList = "slug"),
        @Index(name = "idx_category_code", columnList = "code"),
        @Index(name = "idx_category_provider_id", columnList = "provider_id")
    }
)
public class Category extends BaseAuditEntity {

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "slug", nullable = false, unique = true, length = 255)
  private String slug;

  @Column(name = "code", nullable = false, unique = true, length = 50)
  private String code;

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

  @OneToMany(mappedBy = "category")
  @Builder.Default
  private List<SubCategory> subCategories = new ArrayList<>();
}
