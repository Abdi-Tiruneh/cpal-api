package com.commercepal.apiservice.users.staff.dto;

import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.staff.enums.EmploymentType;
import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;

/**
 * Full staff profile (detail view).
 */
@Schema(description = "Full staff profile: identity, employment, contact, roles, audit")
@Builder
public record StaffResponse(

    @Schema(description = "Employee identifier", example = "EMP001")
    String employeeId,

    @Schema(description = "First name", example = "John")
    String firstName,

    @Schema(description = "Last name", example = "Doe")
    String lastName,

    @Schema(description = "Middle name", example = "Michael")
    String middleName,

    @Schema(description = "Full name", example = "John Michael Doe")
    String fullName,

    @Schema(description = "Email address", example = "john.doe@company.com")
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

    @Schema(description = "Account (login) status", example = "ACTIVE")
    UserStatus accountStatus,

    @Schema(description = "Last sign-in / access time", example = "2024-01-15T14:30:00")
    LocalDateTime lastAccessAt,

    @Schema(description = "Hire date", example = "2024-01-01")
    LocalDate hireDate,

    @Schema(description = "Termination date", example = "2024-12-31")
    LocalDate terminationDate,

    @Schema(description = "Date of birth", example = "1990-05-15")
    LocalDate dateOfBirth,

    @Schema(description = "Nationality (ISO 3166-1 alpha-3)", example = "ETH")
    String nationality,

    @Schema(description = "National ID or passport", example = "ID123456789")
    String nationalId,

    @Schema(description = "Street address", example = "123 Main St, Apt 4B")
    String address,

    @Schema(description = "City", example = "Addis Ababa")
    String city,

    @Schema(description = "State or province", example = "Addis Ababa")
    String stateProvince,

    @Schema(description = "Country (ISO 3166-1 alpha-3)", example = "ETH")
    String country,

    @Schema(description = "Emergency contact name", example = "Jane Doe")
    String emergencyContactName,

    @Schema(description = "Emergency contact phone", example = "+251911000002")
    String emergencyContactPhone,

    @Schema(description = "Emergency contact relationship", example = "Spouse")
    String emergencyContactRelationship,

    @Schema(description = "Assigned role codes", example = "[\"ROLE_WAREHOUSE_MANAGER\"]")
    Set<RoleCode> roles,

    @Schema(description = "Created at", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "Updated at", example = "2024-01-15T14:30:00")
    LocalDateTime updatedAt,

    @Schema(description = "Created by", example = "admin")
    String createdBy,

    @Schema(description = "Updated by", example = "hr_manager")
    String updatedBy
) {

}
