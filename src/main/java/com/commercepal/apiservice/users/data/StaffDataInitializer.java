package com.commercepal.apiservice.users.data;

import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.credential.CredentialRepository;
import com.commercepal.apiservice.users.enums.UserStatus;
import com.commercepal.apiservice.users.enums.UserType;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import com.commercepal.apiservice.users.role.RoleDefinitionRepository;
import com.commercepal.apiservice.users.staff.Staff;
import com.commercepal.apiservice.users.staff.StaffRepository;
import com.commercepal.apiservice.users.staff.enums.EmploymentType;
import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import com.commercepal.apiservice.config.AppInitProperties;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data initialization service for staff module. Creates default super admin user on application
 * startup. Runs AFTER RoleDataInitializer (Order = 2).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class StaffDataInitializer implements CommandLineRunner {

  private final RoleDefinitionRepository roleDefinitionRepository;
  private final StaffRepository staffRepository;
  private final CredentialRepository credentialRepository;
  private final PasswordEncoder passwordEncoder;
  private final EntityManager entityManager;
  private final AppInitProperties appInit;

  @Override
  @Transactional
  public void run(String... args) {
    if (!appInit.isEnabled()) {
      log.info("Data initialization is disabled");
      return;
    }

    log.info("==========================================================");
    log.info("Starting Staff Data Initialization...");
    log.info("==========================================================");

    try {
      if (appInit.isCreateAllRoles()) {
        createAllBackendStaffUsers();
      } else {
        createDefaultSuperAdmin();
      }

      log.info("==========================================================");
      log.info("Staff Data Initialization Completed Successfully!");
      log.info("==========================================================");
    } catch (Exception e) {
      log.error("Error during staff data initialization", e);
      throw new RuntimeException("Failed to initialize staff data", e);
    }
  }

  /**
   * Create staff users for all backend roles.
   */
  private void createAllBackendStaffUsers() {
    log.info("Creating staff users for all backend roles...");

    // Check if any staff users already exist - if so, skip entire initialization
    if (staffRepository.count() > 0) {
      log.info("Staff users already exist in database - skipping staff initialization");
      return;
    }

    // Define all backend staff to create
    StaffUserData[] staffUsers = {
        new StaffUserData("EMP000001", "Super", "Admin", "superadmin@commercepal.com",
            "+251911000001", StaffDepartment.EXECUTIVE, "Super Administrator",
            RoleCode.ROLE_SUPER_ADMIN),
        new StaffUserData("EMP000002", "System", "Admin", "admin@commercepal.com",
            "+251911000002", StaffDepartment.ADMINISTRATION, "System Administrator",
            RoleCode.ROLE_ADMIN),
        new StaffUserData("EMP000003", "Chief", "Executive", "ceo@commercepal.com",
            "+251911000003", StaffDepartment.EXECUTIVE, "Chief Executive Officer",
            RoleCode.ROLE_CEO),
        new StaffUserData("EMP000004", "Finance", "Officer", "finance@commercepal.com",
            "+251911000004", StaffDepartment.FINANCE, "Finance Officer",
            RoleCode.ROLE_FINANCE),
        new StaffUserData("EMP000005", "Finance", "Manager", "finance.manager@commercepal.com",
            "+251911000005", StaffDepartment.FINANCE, "Finance Manager",
            RoleCode.ROLE_FINANCE_MANAGER),
        new StaffUserData("EMP000006", "Warehouse", "Staff", "warehouse@commercepal.com",
            "+251911000006", StaffDepartment.WAREHOUSE, "Warehouse Staff",
            RoleCode.ROLE_WAREHOUSE),
        new StaffUserData("EMP000007", "Warehouse", "Manager", "warehouse.manager@commercepal.com",
            "+251911000007", StaffDepartment.WAREHOUSE, "Warehouse Manager",
            RoleCode.ROLE_WAREHOUSE_MANAGER),
        new StaffUserData("EMP000008", "Call Center", "Agent", "callcenter@commercepal.com",
            "+251911000008", StaffDepartment.CALL_CENTER, "Call Center Agent",
            RoleCode.ROLE_CALL_CENTER),
        new StaffUserData("EMP000009", "Call Center", "Manager",
            "callcenter.manager@commercepal.com",
            "+251911000009", StaffDepartment.CALL_CENTER, "Call Center Manager",
            RoleCode.ROLE_CALL_CENTER_MANAGER),
        new StaffUserData("EMP000010", "Inventory", "Manager", "inventory@commercepal.com",
            "+251911000010", StaffDepartment.INVENTORY, "Inventory Manager",
            RoleCode.ROLE_INVENTORY_MANAGER),
        new StaffUserData("EMP000011", "Order", "Manager", "orders@commercepal.com",
            "+251911000011", StaffDepartment.ORDER_FULFILLMENT, "Order Manager",
            RoleCode.ROLE_ORDER_MANAGER),
        new StaffUserData("EMP000012", "Marketing", "Officer", "marketing@commercepal.com",
            "+251911000012", StaffDepartment.MARKETING, "Marketing Officer",
            RoleCode.ROLE_MARKETING),
        new StaffUserData("EMP000013", "Sales", "Officer", "sales@commercepal.com",
            "+251911000013", StaffDepartment.SALES, "Sales Officer",
            RoleCode.ROLE_SALES),
        new StaffUserData("EMP000014", "HR", "Manager", "hr@commercepal.com",
            "+251911000014", StaffDepartment.HUMAN_RESOURCES, "HR Manager",
            RoleCode.ROLE_HR),
        new StaffUserData("EMP000015", "Support", "Staff", "support@commercepal.com",
            "+251911000015", StaffDepartment.OPERATIONS, "Support Staff",
            RoleCode.ROLE_SUPPORT),
        new StaffUserData("EMP000016", "General", "Manager", "manager@commercepal.com",
            "+251911000016", StaffDepartment.OPERATIONS, "General Manager",
            RoleCode.ROLE_MANAGER)
    };

    int createdCount = 0;
    int existingCount = 0;

    for (StaffUserData userData : staffUsers) {
      try {
        if (createStaffUser(userData)) {
          createdCount++;
        } else {
          existingCount++;
        }
      } catch (Exception e) {
        log.error("Failed to create staff user: {}", userData.employeeId, e);
        // Clear the persistence context to prevent contamination from failed entities
        entityManager.clear();
      }
    }

    log.info("==========================================================");
    log.info("Staff users creation complete: {} created, {} existing", createdCount, existingCount);
    log.info("==========================================================");
    log.info("Common Password: {} (CHANGE IMMEDIATELY!)", appInit.getSuperAdmin().getDefaultPassword());
    log.info("==========================================================");
    log.warn("SECURITY WARNING: All users have the same password!");
    log.warn("Change passwords immediately after first login!");
    log.info("==========================================================");
  }

  /**
   * Create a single staff user. Returns true if created, false if already exists.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean createStaffUser(StaffUserData userData) {
    if (staffRepository.existsByEmployeeIdAndIsDeletedFalse(userData.employeeId)) {
      log.debug("Staff already exists | employeeId={}", userData.employeeId);
      return false;
    }

    if (credentialRepository.existsByEmailAddressAndDeletedFalse(userData.email)) {
      log.debug("Email already registered | email={}", userData.email);
      return false;
    }

    RoleDefinition role = roleDefinitionRepository.findByCode(userData.roleCode)
        .orElseThrow(() -> new IllegalStateException(
            userData.roleCode + " not found. Roles must be initialized first."));

    Set<RoleDefinition> roles = new HashSet<>();
    roles.add(role);

    String encodedPassword = passwordEncoder.encode(appInit.getSuperAdmin().getDefaultPassword());
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    // First, create and save the Credential (without domainUserId initially)
    Credential credential = Credential.builder()
        .userType(UserType.STAFF)
        .emailAddress(userData.email)
        .phoneNumber(userData.phone)
        .passwordHash(encodedPassword)
        .status(UserStatus.ACTIVE)
        .failedSignInAttempts(0)
        .passwordResetFailedAttempts(0)
        .requiresPasswordChange(false)
        .mfaEnabled(false)
        .deleted(false)
        .version(0L)
        .createdAt(now)
        .createdBy("SYSTEM")
        .roles(roles)
        .build();

    credential.setEmailVerified(true);
    credential.setPhoneVerified(true);
    credential.setEmailVerifiedAt(now);
    credential.setPhoneVerifiedAt(now);
    credential.setLastPasswordChangeAt(now);

    credential = credentialRepository.save(credential);

    // Now create Staff with the credential (to satisfy NOT NULL constraint)
    Staff staff = Staff.builder()
        .employeeId(userData.employeeId)
        .firstName(userData.firstName)
        .lastName(userData.lastName)
        .department(userData.department)
        .position(userData.position)
        .employmentType(EmploymentType.FULL_TIME)
        .status(StaffStatus.ACTIVE)
        .hireDate(LocalDate.now())
        .adminNotes("Test user created during initialization")
        .credential(credential)
        .build();

    staff = staffRepository.save(staff);

    credentialRepository.save(credential);

    log.info("Staff user created | employeeId={} | position={} | email={}", userData.employeeId, userData.position, userData.email);
    return true;
  }

  /**
   * Create default super admin user if it doesn't exist.
   */
  private void createDefaultSuperAdmin() {
    log.info("Checking for default super admin user...");

    String superAdminEmployeeId = appInit.getSuperAdmin().getEmployeeId();
    String superAdminEmail = appInit.getSuperAdmin().getEmail();
    String superAdminPhone = appInit.getSuperAdmin().getPhone();

    if (staffRepository.existsByEmployeeIdAndIsDeletedFalse(superAdminEmployeeId)) {
      log.info("Super admin user already exists | employeeId={}", superAdminEmployeeId);
      return;
    }

    if (credentialRepository.existsByEmailAddressAndDeletedFalse(superAdminEmail)) {
      log.info("Email already registered | email={}", superAdminEmail);
      return;
    }

    log.info("Creating default super admin user...");

    try {
      RoleDefinition superAdminRole = roleDefinitionRepository.findByCode(RoleCode.ROLE_SUPER_ADMIN)
          .orElseThrow(() -> new IllegalStateException(
              "ROLE_SUPER_ADMIN not found. Roles must be initialized first."));

      Set<RoleDefinition> roles = new HashSet<>();
      roles.add(superAdminRole);

      String encodedPassword = passwordEncoder.encode(appInit.getSuperAdmin().getDefaultPassword());
      java.time.LocalDateTime now = java.time.LocalDateTime.now();

      // First, create and save the Credential (without domainUserId initially)
      Credential credential = Credential.builder()
          .userType(UserType.STAFF)
          .emailAddress(superAdminEmail)
          .phoneNumber(superAdminPhone)
          .passwordHash(encodedPassword)
          .status(UserStatus.ACTIVE)
          .failedSignInAttempts(0)
          .passwordResetFailedAttempts(0)
          .requiresPasswordChange(false)
          .mfaEnabled(false)
          .deleted(false)
          .version(0L)
          .createdAt(now)
          .createdBy("SYSTEM")
          .roles(roles)
          .build();

      credential.setEmailVerified(true);
      credential.setPhoneVerified(true);
      credential.setEmailVerifiedAt(now);
      credential.setPhoneVerifiedAt(now);
      credential.setLastPasswordChangeAt(now);

      credential = credentialRepository.save(credential);
      log.info("Account credential created | credentialId={}", credential.getId());

      // Now create Staff with the credential (to satisfy NOT NULL constraint)
      Staff staff = Staff.builder()
          .employeeId(superAdminEmployeeId)
          .firstName("System")
          .lastName("Administrator")
          .department(StaffDepartment.EXECUTIVE)
          .position("Super Administrator")
          .employmentType(EmploymentType.FULL_TIME)
          .status(StaffStatus.ACTIVE)
          .hireDate(LocalDate.now())
          .adminNotes("Default super admin created during initialization")
          .credential(credential)
          .build();

      staff = staffRepository.save(staff);
      log.info("Staff entity created | staffId={}", staff.getId());

      // Update credential with the domainUserId
      credentialRepository.save(credential);

      log.info("Default super admin user created successfully | employeeId={} | email={} | phone={}", 
          superAdminEmployeeId, superAdminEmail, superAdminPhone);
      log.warn("SECURITY WARNING: Please change the default password immediately!");

    } catch (Exception e) {
      log.error("Failed to create default super admin user", e);
      throw e;
    }
  }

  /**
   * Helper record to hold staff user data.
   */
  public record StaffUserData(
      String employeeId,
      String firstName,
      String lastName,
      String email,
      String phone,
      StaffDepartment department,
      String position,
      RoleCode roleCode
  ) {

  }
}
