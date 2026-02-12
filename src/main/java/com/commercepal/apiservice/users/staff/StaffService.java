package com.commercepal.apiservice.users.staff;

import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.staff.dto.StaffListResponse;
import com.commercepal.apiservice.users.staff.dto.StaffPageRequest;
import com.commercepal.apiservice.users.staff.dto.StaffRegistrationRequest;
import com.commercepal.apiservice.users.staff.dto.StaffResponse;
import com.commercepal.apiservice.users.staff.dto.StaffStatusUpdateRequest;
import com.commercepal.apiservice.users.staff.dto.StaffUpdateRequest;
import java.util.Set;
import org.springframework.data.domain.Page;

/**
 * Service interface for backend staff operations.
 */
public interface StaffService {

  StaffResponse registerStaff(StaffRegistrationRequest request);

  StaffResponse updateStaff(String employeeId, StaffUpdateRequest request);

  StaffResponse getStaffByEmployeeId(String employeeId);

  Page<StaffListResponse> getStaffPage(StaffPageRequest request);

  StaffResponse updateStaffStatus(String employeeId, StaffStatusUpdateRequest request);

  StaffResponse updateStaffRoles(String employeeId, Set<RoleCode> roleCodes);
}
