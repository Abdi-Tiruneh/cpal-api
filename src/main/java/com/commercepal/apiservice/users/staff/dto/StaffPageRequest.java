package com.commercepal.apiservice.users.staff.dto;

import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pagination and filter request for staff listing.
 */
@ParameterObject
@Schema(description = "Pagination, search, and filter for staff listing")
public record StaffPageRequest(
    @Schema(description = "Zero-based page index", example = "0")
    @Min(0)
    Integer page,

    @Schema(description = "Page size", example = "20")
    @Min(1)
    Integer size,

    @Schema(description = "Search by name or employee ID (case-insensitive)", example = "John")
    String search,

    @Schema(description = "Filter by department", example = "SOFTWARE_ENGINEERS")
    StaffDepartment department,

    @Schema(description = "Filter by employment status", example = "ACTIVE")
    StaffStatus status,

    @Schema(description = "Sort property: createdAt, firstName, lastName, employeeId, department, status", example = "createdAt")
    String sortBy,

    @Schema(description = "Sort direction", example = "DESC")
    Sort.Direction direction
) {

  public StaffPageRequest {
    if (page == null || page < 0) {
      page = 0;
    }
    if (size == null || size <= 0) {
      size = 20;
    }
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = "createdAt";
    }
    if (direction == null) {
      direction = Sort.Direction.DESC;
    }
  }

  public Pageable toPageable() {
    Sort sort = Sort.by(direction, sortBy);
    return PageRequest.of(page, size, sort);
  }
}
