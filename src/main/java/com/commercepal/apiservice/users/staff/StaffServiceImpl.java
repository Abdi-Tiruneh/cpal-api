package com.commercepal.apiservice.users.staff;

import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import com.commercepal.apiservice.users.role.RoleDefinitionService;
import com.commercepal.apiservice.users.staff.dto.StaffListResponse;
import com.commercepal.apiservice.users.staff.dto.StaffPageRequest;
import com.commercepal.apiservice.users.staff.dto.StaffRegistrationRequest;
import com.commercepal.apiservice.users.staff.dto.StaffResponse;
import com.commercepal.apiservice.users.staff.dto.StaffStatusUpdateRequest;
import com.commercepal.apiservice.users.staff.dto.StaffUpdateRequest;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import com.commercepal.apiservice.utils.CurrentUserService;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link StaffService} for backend staff operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

  private final StaffRepository staffRepository;
  private final CredentialRepository credentialRepository;
  private final RoleDefinitionService roleDefinitionService;
  private final PasswordEncoder passwordEncoder;
  private final StaffMapper staffMapper;
  private final CurrentUserService currentUserService;

  @Override
  public StaffResponse registerStaff(StaffRegistrationRequest request) {
    log.info("Registering new staff member: {} {}, employee ID: {}, department: {}",
        request.firstName(), request.lastName(), request.employeeId(),
        request.department());

    if (staffRepository.existsByEmployeeIdAndIsDeletedFalse(request.employeeId())) {
      throw new IllegalArgumentException("Employee ID already exists: " + request.employeeId());
    }

    if (request.emailAddress() == null || request.emailAddress().isBlank()) {
      throw new IllegalArgumentException("Email address is required for staff registration");
    }
    if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
      throw new IllegalArgumentException("Phone number is required for staff registration");
    }

    validateUniqueIdentifiers(request.emailAddress(), request.phoneNumber());

    String encodedPassword = passwordEncoder.encode(request.password());

    Set<RoleDefinition> roles = roleDefinitionService.getRoleDefinitions(request.roles());

    LocalDateTime now = LocalDateTime.now();

    Credential credential = Credential.builder()
        .userType(UserType.STAFF)
        .emailAddress(request.emailAddress())
        .phoneNumber(request.phoneNumber())
        .passwordHash(encodedPassword)
        .status(UserStatus.ACTIVE)
        .failedSignInAttempts(0)
        .passwordResetFailedAttempts(0)
        .requiresPasswordChange(true)
        .mfaEnabled(false)
        .deleted(false)
        .version(0L)
        .createdAt(now)
        .build();

    credential.setRoles(roles);
    credential.setLastPasswordChangeAt(now);

    String currentUser = currentUserService.getCurrentUsername();
    credential.setCreatedBy(currentUser);
    credential.setUpdatedBy(currentUser);

    credential = credentialRepository.save(credential);
    log.info("Account credential created | credentialId={}", credential.getId());

    Staff staff = staffMapper.fromRegistrationRequest(request);
    staff.linkCredential(credential);
    staff = staffRepository.save(staff);
    log.info("Staff entity saved | staffId={}", staff.getId());

    log.info("Staff member registered successfully | firstName={} | lastName={} | staffId={}",
        staff.getFirstName(), staff.getLastName(), staff.getId());

    return staffMapper.toResponse(staff);
  }

  @Override
  public StaffResponse updateStaff(String employeeId, StaffUpdateRequest request) {
    log.info("Updating staff member with employee ID: {}", employeeId);

    Staff staff = staffRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found with employee ID: " + employeeId));

    staffMapper.updateFromRequest(staff, request);

    staff = staffRepository.save(staff);

    log.info("Staff member updated successfully | employeeId={}", employeeId);
    return staffMapper.toResponse(staff);
  }

  @Override
  @Transactional(readOnly = true)
  public StaffResponse getStaffByEmployeeId(String employeeId) {
    Staff staff = staffRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found with employee ID: " + employeeId));
    return staffMapper.toResponse(staff);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<StaffListResponse> getStaffPage(StaffPageRequest request) {
    var spec = StaffSpecification.buildSpecification(request);
    return staffRepository.findAll(spec, request.toPageable())
        .map(staffMapper::toListResponse);
  }

  @Override
  public StaffResponse updateStaffStatus(String employeeId, StaffStatusUpdateRequest request) {
    log.info("Updating status for staff employee ID: {} to {}", employeeId, request.status());

    Staff staff = staffRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found with employee ID: " + employeeId));

    staff.setStatus(request.status());

    if (request.status() == StaffStatus.TERMINATED) {
      if (request.terminationDate() != null) {
        staff.setTerminationDate(request.terminationDate());
      }
      if (request.reason() != null) {
        staff.setRemarks(request.reason());
      }
      Credential credential = staff.getCredential();
      credential.setStatus(UserStatus.SUSPENDED);
      credentialRepository.save(credential);
    } else {
      staff.setTerminationDate(null);
      if (request.status() == StaffStatus.ACTIVE) {
        Credential credential = staff.getCredential();
        credential.setStatus(UserStatus.ACTIVE);
        credentialRepository.save(credential);
      }
    }

    staff = staffRepository.save(staff);
    log.info("Staff status updated | employeeId={} | status={}", employeeId, request.status());
    return staffMapper.toResponse(staff);
  }

  @Override
  public StaffResponse updateStaffRoles(String employeeId, Set<RoleCode> roleCodes) {
    log.info("Updating roles for staff employee ID: {}", employeeId);

    Staff staff = staffRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found with employee ID: " + employeeId));

    Set<RoleDefinition> roles = roleDefinitionService.getRoleDefinitions(roleCodes);
    staff.getCredential().setRoles(roles);
    credentialRepository.save(staff.getCredential());

    log.info("Roles updated | employeeId={}", employeeId);
    return staffMapper.toResponse(staff);
  }

  private void validateUniqueIdentifiers(String email, String phoneNumber) {
    if (email != null && credentialRepository.existsByEmailAddress(email)) {
      throw new IllegalArgumentException("Email address already registered: " + email);
    }
    if (phoneNumber != null && credentialRepository.existsByPhoneNumber(phoneNumber)) {
      throw new IllegalArgumentException("Phone number already registered: " + phoneNumber);
    }
  }
}
