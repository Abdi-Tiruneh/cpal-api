package com.commercepal.apiservice.users.data;

import com.commercepal.apiservice.config.AppInitProperties;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.users.role.RoleDefinition;
import com.commercepal.apiservice.users.role.RoleDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data initialization service for backend roles. Seeds all role definitions on application startup.
 * Runs only when app.init.enabled=true. Runs BEFORE StaffDataInitializer (Order = 1).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class RoleDataInitializer implements CommandLineRunner {

  private final RoleDefinitionRepository roleDefinitionRepository;
  private final AppInitProperties appInit;

  @Override
  @Transactional
  public void run(String... args) {
    if (!appInit.isEnabled()) {
      log.debug("Role data initialization is disabled (app.init.enabled=false)");
      return;
    }

    log.info("==========================================================");
    log.info("Starting Role Definitions Initialization...");
    log.info("==========================================================");

    try {
      initializeBackendRoles();

      log.info("==========================================================");
      log.info("Role Definitions Initialization Completed Successfully!");
      log.info("==========================================================");
    } catch (Exception e) {
      log.error("Error during role initialization", e);
      throw new RuntimeException("Failed to initialize roles", e);
    }
  }

  /**
   * Initialize all backend staff roles in the database.
   */
  private void initializeBackendRoles() {
    log.info("Initializing backend roles...");

    // Check if any roles already exist - if so, skip entire initialization
    if (roleDefinitionRepository.count() > 0) {
      log.info("Roles already exist in database - skipping role initialization");
      return;
    }

    // Define all backend roles with their descriptions
    RoleData[] roles = {
        // Backend staff roles
        new RoleData(RoleCode.ROLE_SUPER_ADMIN, "Super Administrator",
            "Full system access with all permissions"),
        new RoleData(RoleCode.ROLE_ADMIN, "Administrator",
            "Administrative access to manage system"),
        new RoleData(RoleCode.ROLE_CEO, "Chief Executive Officer",
            "Executive dashboard and analytics access"),
        new RoleData(RoleCode.ROLE_FINANCE, "Finance Officer",
            "Financial operations and reporting"),
        new RoleData(RoleCode.ROLE_FINANCE_MANAGER, "Finance Manager",
            "Finance team management and oversight"),
        new RoleData(RoleCode.ROLE_WAREHOUSE, "Warehouse Staff",
            "Warehouse operations and inventory handling"),
        new RoleData(RoleCode.ROLE_WAREHOUSE_MANAGER, "Warehouse Manager",
            "Warehouse management and logistics oversight"),
        new RoleData(RoleCode.ROLE_CALL_CENTER, "Call Center Agent",
            "Customer support and service operations"),
        new RoleData(RoleCode.ROLE_CALL_CENTER_MANAGER, "Call Center Manager",
            "Support team management and quality assurance"),
        new RoleData(RoleCode.ROLE_INVENTORY_MANAGER, "Inventory Manager",
            "Inventory control and stock management"),
        new RoleData(RoleCode.ROLE_ORDER_MANAGER, "Order Manager",
            "Order fulfillment and processing management"),
        new RoleData(RoleCode.ROLE_MARKETING, "Marketing Officer",
            "Marketing campaigns and promotions"),
        new RoleData(RoleCode.ROLE_SALES, "Sales Officer",
            "Sales operations and customer acquisition"),
        new RoleData(RoleCode.ROLE_HR, "Human Resources",
            "HR management and employee operations"),
        new RoleData(RoleCode.ROLE_SUPPORT, "Support Staff",
            "General support and assistance"),
        new RoleData(RoleCode.ROLE_MANAGER, "Manager",
            "General management responsibilities"),

        // Customer-facing roles
        new RoleData(RoleCode.ROLE_CUSTOMER, "Customer",
            "Regular customer account"),
        new RoleData(RoleCode.ROLE_MERCHANT, "Merchant",
            "Merchant/seller account with ability to list and sell products"),

        // Agent/Partner roles
        new RoleData(RoleCode.ROLE_AGENT, "Agent",
            "General sales agent with commission-based compensation"),
        new RoleData(RoleCode.ROLE_FINANCIAL_INSTITUTION_AGENT, "Financial Institution Agent",
            "Banking or financial institution partner agent with special privileges"),
        new RoleData(RoleCode.ROLE_AFFILIATE, "Affiliate Partner",
            "Affiliate/referral partner earning commission on referred sales")
    };

    int createdCount = 0;
    int existingCount = 0;

    for (RoleData roleData : roles) {
      if (!roleDefinitionRepository.existsByCode(roleData.code)) {
        RoleDefinition role = RoleDefinition.create(
            roleData.code,
            roleData.name,
            roleData.description
        );
        roleDefinitionRepository.save(role);
        log.info("Role created | name={} | code={}", roleData.name, roleData.code);
        createdCount++;
      } else {
        log.debug("Role already exists | code={}", roleData.code);
        existingCount++;
      }
    }

    log.info("Backend roles initialization complete: {} created, {} existing",
        createdCount, existingCount);
  }

  /**
   * Helper record to hold role data.
   */
  private record RoleData(RoleCode code, String name, String description) {

  }
}
