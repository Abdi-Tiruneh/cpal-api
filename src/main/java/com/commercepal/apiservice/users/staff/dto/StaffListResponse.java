package com.commercepal.apiservice.users.staff.dto;

import com.commercepal.apiservice.users.staff.enums.EmploymentType;
import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Staff summary for paginated list results.
 */
@Schema(description = "Staff summary item for list/search results")
@Builder
public record StaffListResponse(
    @Schema(description = "Employee identifier", example = "EMP001")
    String employeeId,
    @Schema(description = "Full name", example = "John Doe")
    String fullName,
    @Schema(description = "Email address", example = "john@company.com")
    String emailAddress,
    @Schema(description = "Phone number", example = "+251911000001")
    String phoneNumber,
    @Schema(description = "Department", example = "SOFTWARE_ENGINEERS")
    StaffDepartment department,
    @Schema(description = "Job title", example = "Senior Engineer")
    String position,
    @Schema(description = "Employment type", example = "FULL_TIME")
    EmploymentType employmentType,
    @Schema(description = "Employment status", example = "ACTIVE")
    StaffStatus status,
    @Schema(description = "Hire date", example = "2024-01-15")
    LocalDate hireDate,
    @Schema(description = "Record creation timestamp", example = "2024-01-15T10:00:00")
    LocalDateTime createdAt,
    @Schema(description = "Last sign-in / access time", example = "2024-01-15T14:30:00")
    LocalDateTime lastAccessAt
) {}
