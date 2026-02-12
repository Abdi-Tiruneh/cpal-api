package com.commercepal.apiservice.users.staff.dto;

import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.staff.enums.EmploymentType;
import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

/**
 * Request to register a new staff member (profile + credentials + roles).
 */
@Schema(description = "Staff registration: profile, login credentials, and role assignments")
public record StaffRegistrationRequest(

    @Schema(description = "Unique employee identifier", example = "EMP001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    String employeeId,

    @Schema(description = "First name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(max = 120, message = "First name must not exceed 120 characters")
    String firstName,

    @Schema(description = "Last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(max = 120, message = "Last name must not exceed 120 characters")
    String lastName,

    @Schema(description = "Middle name", example = "Michael")
    @Size(max = 120, message = "Middle name must not exceed 120 characters")
    String middleName,

    @Schema(description = "Login email", example = "john.doe@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email address")
    @Size(max = 254, message = "Email must not exceed 254 characters")
    String emailAddress,

    @Schema(description = "Login phone (E.164)", example = "+251911000001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    String phoneNumber,

    @Schema(description = "Password (stored encrypted)", example = "SecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password,

    @Schema(description = "Department", example = "SOFTWARE_ENGINEERS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Department is required")
    StaffDepartment department,

    @Schema(description = "Job title", example = "Senior Engineer", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Position is required")
    @Size(max = 120, message = "Position must not exceed 120 characters")
    String position,

    @Schema(description = "Employment type", example = "FULL_TIME", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Employment type is required")
    EmploymentType employmentType,

    @Schema(description = "Hire date", example = "2024-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Hire date is required")
    @Past(message = "Hire date must be in the past")
    LocalDate hireDate,

    @Schema(description = "Date of birth", example = "1990-05-15")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @Schema(description = "Nationality (ISO 3166-1 alpha-3)", example = "ETH")
    String nationality,

    @Schema(description = "National ID or passport number", example = "ID123456789")
    @Size(max = 50, message = "National ID must not exceed 50 characters")
    String nationalId,

    @Schema(description = "Street address", example = "123 Main St, Apt 4B")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address,

    @Schema(description = "City", example = "Addis Ababa")
    @Size(max = 120, message = "City must not exceed 120 characters")
    String city,

    @Schema(description = "State or province", example = "Addis Ababa")
    @Size(max = 50, message = "State/Province must not exceed 50 characters")
    String stateProvince,

    @Schema(description = "Country (ISO 3166-1 alpha-3)", example = "ETH")
    String country,

    @Schema(description = "Emergency contact name", example = "Jane Doe")
    @Size(max = 120, message = "Emergency contact name must not exceed 120 characters")
    String emergencyContactName,

    @Schema(description = "Emergency contact phone", example = "+251911000002")
    @Size(max = 32, message = "Emergency contact phone must not exceed 32 characters")
    String emergencyContactPhone,

    @Schema(description = "Emergency contact relationship", example = "Spouse")
    @Size(max = 50, message = "Emergency contact relationship must not exceed 50 characters")
    String emergencyContactRelationship,

    @Schema(description = "Assigned role codes (at least one required)", example = "[\"ROLE_WAREHOUSE_MANAGER\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one role is required")
    Set<RoleCode> roles
) {

}
