package com.commercepal.apiservice.users.staff;

import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.staff.dto.EnumOptionResponse;
import com.commercepal.apiservice.users.staff.dto.StaffPageRequest;
import com.commercepal.apiservice.users.staff.dto.StaffRegistrationRequest;
import com.commercepal.apiservice.users.staff.dto.StaffListResponse;
import com.commercepal.apiservice.users.staff.dto.StaffResponse;
import com.commercepal.apiservice.users.staff.dto.StaffStatusUpdateRequest;
import com.commercepal.apiservice.users.staff.dto.StaffUpdateRequest;
import com.commercepal.apiservice.users.staff.enums.EmploymentType;
import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST controller for backend staff management. Provides comprehensive administrative APIs
 * for managing internal staff members including admins, warehouse staff, call center agents,
 * finance team, etc.
 */
@RestController
@RequestMapping("/api/v1/admin/staff")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
    name = "Staff Management",
    description = """
        Comprehensive APIs for backend staff management and self-service.
        
        **Self-Service:**
        - View own profile
        
        **Staff Management:**
        - Register new staff members with roles and permissions
        - Update staff information and employment details
        - Manage staff departments and positions
        - Assign and update roles
        - Terminate or deactivate staff
        
        **Search & Filtering:**
        - GET / (StaffPageRequest): paginated list with optional search, department, status filters
        """
)
public class StaffController {

  private final StaffService staffService;
  private final CurrentUserService currentUserService;

  @GetMapping("/me")
  @Operation(
      summary = "Get my profile",
      description = "Retrieves the authenticated staff member's profile"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Not authenticated"),
      @ApiResponse(responseCode = "404", description = "Staff profile not found")
  })
  public ResponseEntity<ResponseWrapper<StaffResponse>> getMyProfile() {
    var currentStaff = currentUserService.getCurrentStaff();
    log.info("Staff member fetching own profile | employeeId={}", currentStaff.getEmployeeId());

    StaffResponse response = staffService.getStaffByEmployeeId(currentStaff.getEmployeeId());

    return ResponseWrapper.success(response);
  }

  @GetMapping("/departments")
  @Operation(
      summary = "Get staff departments",
      description = "Returns all staff department options with display name and description"
  )
  @ApiResponse(responseCode = "200", description = "Departments retrieved successfully")
  public ResponseEntity<ResponseWrapper<List<EnumOptionResponse>>> getDepartments() {
    List<EnumOptionResponse> options = Arrays.stream(StaffDepartment.values())
        .map(d -> new EnumOptionResponse(d.name(), d.getDisplayName(), d.getDescription()))
        .toList();
    return ResponseWrapper.success(options);
  }

  @GetMapping("/employment-types")
  @Operation(
      summary = "Get employment types",
      description = "Returns all employment type options with display name and description"
  )
  @ApiResponse(responseCode = "200", description = "Employment types retrieved successfully")
  public ResponseEntity<ResponseWrapper<List<EnumOptionResponse>>> getEmploymentTypes() {
    List<EnumOptionResponse> options = Arrays.stream(EmploymentType.values())
        .map(e -> new EnumOptionResponse(e.name(), e.getDisplayName(), e.getDescription()))
        .toList();
    return ResponseWrapper.success(options);
  }

  @GetMapping("/statuses")
  @Operation(
      summary = "Get staff statuses",
      description = "Returns all staff status options with display name and description"
  )
  @ApiResponse(responseCode = "200", description = "Statuses retrieved successfully")
  public ResponseEntity<ResponseWrapper<List<EnumOptionResponse>>> getStatuses() {
    List<EnumOptionResponse> options = Arrays.stream(StaffStatus.values())
        .map(s -> new EnumOptionResponse(s.name(), s.getDisplayName(), s.getDescription()))
        .toList();
    return ResponseWrapper.success(options);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'CEO')")
  @Operation(
      summary = "Register new staff member",
      description = "Creates a new backend staff member with account credentials and assigned roles"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Staff member registered successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data"),
      @ApiResponse(responseCode = "409", description = "Employee ID or email/phone already exists")
  })
  public ResponseEntity<ResponseWrapper<StaffResponse>> registerStaff(
      @Valid @RequestBody StaffRegistrationRequest request) {
    log.info("Admin registering new staff: {} {}", request.firstName(), request.lastName());

    StaffResponse response = staffService.registerStaff(request);

    return ResponseWrapper.created("Staff member registered successfully", response);
  }

  @GetMapping("/{employeeId}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'CEO')")
  @Operation(
      summary = "Get staff by employee ID",
      description = "Retrieves detailed information about a staff member"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Staff found"),
      @ApiResponse(responseCode = "404", description = "Staff not found")
  })
  public ResponseEntity<ResponseWrapper<StaffResponse>> getStaffByEmployeeId(
      @PathVariable String employeeId) {
    log.info("Fetching staff by employee ID: {}", employeeId);

    StaffResponse response = staffService.getStaffByEmployeeId(employeeId);

    return ResponseWrapper.success(response);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'CEO')")
  @Operation(
      summary = "Get staff (paginated with optional filters)",
      description = "Retrieves paginated staff list. Supports search (name/employee ID), department and status filters."
  )
  @ApiResponse(responseCode = "200", description = "Staff list retrieved successfully")
  public ResponseEntity<ResponseWrapper<PagedResponse<StaffListResponse>>> getStaffPage(
      @Valid StaffPageRequest request) {
    log.info("Fetching staff - page: {}, size: {}, search: {}, department: {}, status: {}",
        request.page(), request.size(), request.search(), request.department(), request.status());

    Page<StaffListResponse> response = staffService.getStaffPage(request);

    return ResponseWrapper.success(response);
  }

  @PutMapping("/{employeeId}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'CEO')")
  @Operation(
      summary = "Update staff information",
      description = "Updates staff member information"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Staff updated successfully"),
      @ApiResponse(responseCode = "404", description = "Staff not found")
  })
  public ResponseEntity<ResponseWrapper<StaffResponse>> updateStaff(
      @PathVariable String employeeId,
      @Valid @RequestBody StaffUpdateRequest request) {
    log.info("Updating staff employee ID: {}", employeeId);

    StaffResponse response = staffService.updateStaff(employeeId, request);

    return ResponseWrapper.success("Staff updated successfully", response);
  }

  @PatchMapping("/{employeeId}/roles")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'CEO')")
  @Operation(
      summary = "Update staff roles",
      description = "Updates the roles assigned to a staff member (Super Admin only)"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Roles updated successfully"),
      @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
      @ApiResponse(responseCode = "404", description = "Staff not found")
  })
  public ResponseEntity<ResponseWrapper<StaffResponse>> updateStaffRoles(
      @PathVariable String employeeId,
      @RequestBody Set<RoleCode> roles) {
    log.info("Updating roles for staff employee ID: {}", employeeId);

    StaffResponse response = staffService.updateStaffRoles(employeeId, roles);

    return ResponseWrapper.success("Roles updated successfully", response);
  }

  @PatchMapping("/{employeeId}/status")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'CEO')")
  @Operation(
      summary = "Update staff status",
      description = "Updates only the staff employment status. Use TERMINATED with optional terminationDate and reason; use ACTIVE to reactivate (clears termination and unsuspends account)."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Status updated successfully"),
      @ApiResponse(responseCode = "404", description = "Staff not found")
  })
  public ResponseEntity<ResponseWrapper<StaffResponse>> updateStaffStatus(
      @PathVariable String employeeId,
      @Valid @RequestBody StaffStatusUpdateRequest request) {
    log.info("Updating status for staff employee ID: {} to {}", employeeId, request.status());

    StaffResponse response = staffService.updateStaffStatus(employeeId, request);

    return ResponseWrapper.success("Staff status updated successfully", response);
  }
}

