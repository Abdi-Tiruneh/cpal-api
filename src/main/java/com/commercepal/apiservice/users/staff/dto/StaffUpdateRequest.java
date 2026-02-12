package com.commercepal.apiservice.users.staff.dto;

import com.commercepal.apiservice.users.staff.enums.EmploymentType;
import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Partial update for staff profile. All fields optional; only provided fields are updated.
 */
@Schema(description = "Staff profile update. All fields optional; only provided fields are updated.")
public record StaffUpdateRequest(

    @Schema(description = "First name", example = "John")
    @Size(max = 120, message = "First name must not exceed 120 characters")
    String firstName,

    @Schema(description = "Last name", example = "Doe")
    @Size(max = 120, message = "Last name must not exceed 120 characters")
    String lastName,

    @Schema(description = "Middle name", example = "Michael")
    @Size(max = 120, message = "Middle name must not exceed 120 characters")
    String middleName,

    @Schema(description = "Department", example = "SOFTWARE_ENGINEERS")
    StaffDepartment department,

    @Schema(description = "Job title", example = "Senior Engineer")
    @Size(max = 120, message = "Position must not exceed 120 characters")
    String position,

    @Schema(description = "Employment type", example = "FULL_TIME")
    EmploymentType employmentType,

    @Schema(description = "Date of birth", example = "1990-05-15")
    LocalDate dateOfBirth,

    @Schema(description = "Nationality (ISO 3166-1 alpha-3)", example = "ETH")
    String nationality,

    @Schema(description = "National ID or passport", example = "ID123456789")
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
    String emergencyContactRelationship
) {

}
