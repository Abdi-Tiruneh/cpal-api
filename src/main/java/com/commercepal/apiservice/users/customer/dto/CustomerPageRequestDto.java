package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.users.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * DTO for advanced pagination, multi-level sorting, and comprehensive filtering of Customer
 * entities. Supports filtering and sorting on both Customer and related Credential entity
 * fields.
 */
@ParameterObject
@Schema(description = "Advanced pagination, filtering, and multi-level sorting request for customers with dual-entity support")
public record CustomerPageRequestDto(

    // ========== PAGINATION ==========
    @Schema(description = "Page number (0-based index)", example = "0")
    @Min(0)
    Integer page,

    @Schema(description = "Number of records per page", example = "20")
    @Min(1)
    Integer size,

    // ========== MULTI-LEVEL SORTING ==========
    @Schema(
        description = """
            Primary sort field. Allowed:
            Customer fields: firstName, lastName, accountNumber, country, city, registrationChannel, createdAt, updatedAt
            Credential fields: status, emailAddress, phoneNumber, lastSignedInAt
            """,
        example = "createdAt"
    )
    @Pattern(
        regexp = "^(firstName|lastName|country|city|stateProvince|registrationChannel|"
            + "preferredLanguage|preferredCurrency|createdAt|updatedAt|"
            + "status|emailAddress|phoneNumber|lastSignedInAt|isEmailVerified|isPhoneVerified)$",
        message = "Invalid sort field. Must be a valid Customer or Credential field."
    )
    String sortBy1,

    @Schema(description = "Primary sort direction (ASC or DESC)", example = "DESC")
    Sort.Direction direction1,

    @Schema(description = "Secondary sort field", example = "lastName")
    String sortBy2,

    @Schema(description = "Secondary sort direction (ASC or DESC)", example = "ASC")
    Sort.Direction direction2,

    @Schema(description = "Tertiary sort field", example = "id")
    String sortBy3,

    @Schema(description = "Tertiary sort direction (ASC or DESC)", example = "DESC")
    Sort.Direction direction3,

    // ========== FREE-TEXT SEARCH ==========
    @Schema(
        description = "Free-text keyword search across firstName, lastName, email, phone",
        example = "John"
    )
    String keyword,

    // ========== CUSTOMER-SPECIFIC FILTERS ==========
    @Schema(description = "Filter by country (ISO 3166-1 alpha-2 code)", example = "ET")
    SupportedCountry country,

    @Schema(description = "Filter by city", example = "Addis Ababa")
    String city,

    @Schema(description = "Filter by state/province", example = "Addis Ababa")
    String stateProvince,

    @Schema(description = "Filter by registration channel", example = "WEB")
    Channel registrationChannel,

    // ========== ACCOUNT CREDENTIAL FILTERS ==========
    @Schema(description = "Filter by account status", example = "ACTIVE")
    UserStatus status,

    @Schema(description = "Filter by email verified status", example = "true")
    Boolean isEmailVerified,

    @Schema(description = "Filter by phone verified status", example = "true")
    Boolean isPhoneVerified,

    @Schema(description = "Filter by MFA enabled status", example = "false")
    Boolean mfaEnabled,

    @Schema(description = "Filter by locked accounts (has lockedUntil set)", example = "false")
    Boolean isLocked,

    // ========== DATE RANGE FILTERS ==========
    @Schema(description = "Filter customers created after this date", example = "2025-01-01T00:00:00")
    LocalDateTime createdAfter,

    @Schema(description = "Filter customers created before this date", example = "2025-12-31T23:59:59")
    LocalDateTime createdBefore,

    @Schema(description = "Filter customers updated after this date", example = "2025-01-01T00:00:00")
    LocalDateTime updatedAfter,

    @Schema(description = "Filter customers updated before this date", example = "2025-12-31T23:59:59")
    LocalDateTime updatedBefore,

    @Schema(description = "Filter by last sign-in after this date", example = "2025-01-01T00:00:00")
    LocalDateTime lastSignedInAfter,

    @Schema(description = "Filter by last sign-in before this date", example = "2025-12-31T23:59:59")
    LocalDateTime lastSignedInBefore,

    // ========== AUDIT & COMPLIANCE FILTERS ==========
    @Schema(description = "Filter by deletion status", example = "false")
    Boolean isDeleted

) {

  // COMPACT CONSTRUCTOR - Set defaults and validate
  public CustomerPageRequestDto {
    // Pagination defaults
    if (page == null || page < 0) {
      page = 0;
    }
    if (size == null || size <= 0) {
      size = 20;
    }

    // Sorting defaults
    if (sortBy1 == null) {
      sortBy1 = "createdAt";
    }
    if (direction1 == null) {
      direction1 = Sort.Direction.DESC;
    }

    if (sortBy2 == null) {
      sortBy2 = "lastName";
    }
    if (direction2 == null) {
      direction2 = Sort.Direction.ASC;
    }

    if (sortBy3 == null) {
      sortBy3 = "id";
    }
    if (direction3 == null) {
      direction3 = Sort.Direction.DESC;
    }

    // Compliance defaults
    if (isDeleted == null) {
      isDeleted = false;
    }
  }

  // ========== SORTING LOGIC ==========

  public Sort toSort() {
    List<Sort.Order> orders = new ArrayList<>();

    addOrder(orders, sortBy1, direction1);
    addOrder(orders, sortBy2, direction2);
    addOrder(orders, sortBy3, direction3);

    return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
  }

  private void addOrder(List<Sort.Order> orders, String sortBy, Sort.Direction direction) {
    if (sortBy != null && !sortBy.isBlank()) {
      Sort.Direction resolvedDirection = resolveDirection(sortBy, direction);
      orders.add(new Sort.Order(resolvedDirection, sortBy));
    }
  }

  private Sort.Direction resolveDirection(String sortBy, Sort.Direction direction) {
    if (direction != null) {
      return direction;
    }

    // Default DESC for audit and date fields
    String field = sortBy.toLowerCase();
    if (field.contains("date") || field.contains("time") || field.contains("created")
        || field.contains("updated") || field.contains("signedin") || field.contains("at")) {
      return Sort.Direction.DESC;
    }

    return Sort.Direction.ASC;
  }

  // ========== PAGEABLE CONVERSION ==========

  public Pageable toPageable() {
    return PageRequest.of(page, size, toSort());
  }

  // ========== SEARCH NORMALIZATION ==========

  public String normalizedKeyword() {
    return (keyword == null || keyword.isBlank()) ? null : keyword.trim().toLowerCase();
  }

  // ========== FILTER DETECTION ==========

  public boolean hasFilters() {
    return keyword != null
        || country != null
        || city != null
        || stateProvince != null
        || registrationChannel != null
        || status != null
        || isEmailVerified != null
        || isPhoneVerified != null
        || mfaEnabled != null
        || isLocked != null
        || createdAfter != null
        || createdBefore != null
        || updatedAfter != null
        || updatedBefore != null
        || lastSignedInAfter != null
        || lastSignedInBefore != null
        || isDeleted != null;
  }

  // ========== BUSINESS LOGIC HELPERS ==========

  /**
   * Check if this request filters for active customers only.
   */
  public boolean isActiveCustomersFilter() {
    return status == UserStatus.ACTIVE && Boolean.FALSE.equals(isDeleted);
  }

  /**
   * Check if this request filters for verified customers.
   */
  public boolean isVerifiedCustomersFilter() {
    return Boolean.TRUE.equals(isEmailVerified) || Boolean.TRUE.equals(isPhoneVerified);
  }

  /**
   * Get all active filter field names for metadata.
   */
  public Set<String> getActiveFilterFields() {
    Set<String> fields = new HashSet<>();
    if (keyword != null) {
      fields.add("keyword");
    }
    if (country != null) {
      fields.add("country");
    }
    if (city != null) {
      fields.add("city");
    }
    if (stateProvince != null) {
      fields.add("stateProvince");
    }
    if (registrationChannel != null) {
      fields.add("registrationChannel");
    }
    if (status != null) {
      fields.add("status");
    }
    if (isEmailVerified != null) {
      fields.add("isEmailVerified");
    }
    if (isPhoneVerified != null) {
      fields.add("isPhoneVerified");
    }
    if (mfaEnabled != null) {
      fields.add("mfaEnabled");
    }
    if (isLocked != null) {
      fields.add("isLocked");
    }
    if (createdAfter != null) {
      fields.add("createdAfter");
    }
    if (createdBefore != null) {
      fields.add("createdBefore");
    }
    if (updatedAfter != null) {
      fields.add("updatedAfter");
    }
    if (updatedBefore != null) {
      fields.add("updatedBefore");
    }
    if (lastSignedInAfter != null) {
      fields.add("lastSignedInAfter");
    }
    if (lastSignedInBefore != null) {
      fields.add("lastSignedInBefore");
    }
    if (isDeleted != null) {
      fields.add("isDeleted");
    }
    return fields;
  }

}
