package com.commercepal.apiservice.users.staff;

import com.commercepal.apiservice.users.role.RoleDefinition;
import com.commercepal.apiservice.users.staff.dto.StaffListResponse;
import com.commercepal.apiservice.users.staff.dto.StaffRegistrationRequest;
import com.commercepal.apiservice.users.staff.dto.StaffResponse;
import com.commercepal.apiservice.users.staff.dto.StaffUpdateRequest;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import com.commercepal.apiservice.utils.MapperUtils;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper for Staff entity and DTOs.
 */
@Component
public class StaffMapper {

  /**
   * Create Staff entity from registration request.
   */
  public Staff fromRegistrationRequest(StaffRegistrationRequest request) {
    if (request == null) {
      return null;
    }

    Staff staff = new Staff();
    staff.setEmployeeId(request.employeeId());
    staff.setFirstName(request.firstName());
    staff.setLastName(request.lastName());
    staff.setMiddleName(request.middleName());
    staff.setDepartment(request.department());
    staff.setPosition(request.position());
    staff.setEmploymentType(request.employmentType());
    staff.setStatus(StaffStatus.ACTIVE);
    staff.setHireDate(request.hireDate());
    staff.setDateOfBirth(request.dateOfBirth());
    staff.setNationality(request.nationality());
    staff.setNationalId(request.nationalId());
    staff.setAddress(request.address());
    staff.setCity(request.city());
    staff.setStateProvince(request.stateProvince());
    staff.setCountry(request.country());
    staff.setEmergencyContactName(request.emergencyContactName());
    staff.setEmergencyContactPhone(request.emergencyContactPhone());
    staff.setEmergencyContactRelationship(request.emergencyContactRelationship());

    return staff;
  }

  /**
   * Update Staff entity from update request using MapperUtils.
   */
  public void updateFromRequest(Staff staff, StaffUpdateRequest request) {
    if (staff == null || request == null) {
      return;
    }

    MapperUtils.applyIfNotBlank(request.firstName(), staff::setFirstName);
    MapperUtils.applyIfNotBlank(request.lastName(), staff::setLastName);
    MapperUtils.applyIfNotBlank(request.middleName(), staff::setMiddleName);
    MapperUtils.applyIfNotNull(request.department(), staff::setDepartment);
    MapperUtils.applyIfNotBlank(request.position(), staff::setPosition);
    MapperUtils.applyIfNotNull(request.employmentType(), staff::setEmploymentType);
    MapperUtils.applyIfNotNull(request.dateOfBirth(), staff::setDateOfBirth);
    MapperUtils.applyIfNotNull(request.nationality(), staff::setNationality);
    MapperUtils.applyIfNotBlank(request.nationalId(), staff::setNationalId);
    MapperUtils.applyIfNotBlank(request.address(), staff::setAddress);
    MapperUtils.applyIfNotBlank(request.city(), staff::setCity);
    MapperUtils.applyIfNotBlank(request.stateProvince(), staff::setStateProvince);
    MapperUtils.applyIfNotNull(request.country(), staff::setCountry);
    MapperUtils.applyIfNotBlank(request.emergencyContactName(), staff::setEmergencyContactName);
    MapperUtils.applyIfNotBlank(request.emergencyContactPhone(), staff::setEmergencyContactPhone);
    MapperUtils.applyIfNotBlank(request.emergencyContactRelationship(),
        staff::setEmergencyContactRelationship);
  }

  /**
   * Convert Staff entity to StaffResponse record.
   */
  public StaffResponse toResponse(Staff staff) {
    if (staff == null) {
      return null;
    }

    return StaffResponse.builder()
        .employeeId(staff.getEmployeeId())
        .firstName(staff.getFirstName())
        .lastName(staff.getLastName())
        .middleName(staff.getMiddleName())
        .fullName(staff.getFullName())
        .emailAddress(staff.getCredential().getEmailAddress())
        .phoneNumber(staff.getCredential().getPhoneNumber())
        .department(staff.getDepartment())
        .position(staff.getPosition())
        .employmentType(staff.getEmploymentType())
        .status(staff.getStatus())
        .accountStatus(staff.getCredential().getStatus())
        .lastAccessAt(staff.getCredential().getLastSignedInAt())
        .hireDate(staff.getHireDate())
        .terminationDate(staff.getTerminationDate())
        .dateOfBirth(staff.getDateOfBirth())
        .nationality(staff.getNationality())
        .nationalId(staff.getNationalId())
        .address(staff.getAddress())
        .city(staff.getCity())
        .stateProvince(staff.getStateProvince())
        .country(staff.getCountry())
        .emergencyContactName(staff.getEmergencyContactName())
        .emergencyContactPhone(staff.getEmergencyContactPhone())
        .emergencyContactRelationship(staff.getEmergencyContactRelationship())
        .roles(staff.getCredential().getRoles().stream()
            .map(RoleDefinition::getCode)
            .collect(Collectors.toSet()))
        .createdAt(staff.getCreatedAt())
        .updatedAt(staff.getUpdatedAt())
        .createdBy(staff.getCreatedBy())
        .updatedBy(staff.getUpdatedBy())
        .build();
  }

  /**
   * Convert Staff entity to StaffListResponse (for list API).
   */
  public StaffListResponse toListResponse(Staff staff) {
    if (staff == null) {
      return null;
    }
    return StaffListResponse.builder()
        .employeeId(staff.getEmployeeId())
        .fullName(staff.getFullName())
        .emailAddress(staff.getCredential().getEmailAddress())
        .phoneNumber(staff.getCredential().getPhoneNumber())
        .department(staff.getDepartment())
        .position(staff.getPosition())
        .employmentType(staff.getEmploymentType())
        .status(staff.getStatus())
        .hireDate(staff.getHireDate())
        .createdAt(staff.getCreatedAt())
        .lastAccessAt(staff.getCredential().getLastSignedInAt())
        .build();
  }
}

