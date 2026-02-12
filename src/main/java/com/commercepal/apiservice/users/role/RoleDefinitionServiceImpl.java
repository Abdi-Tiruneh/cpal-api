package com.commercepal.apiservice.users.role;

import com.commercepal.apiservice.users.role.dto.RoleInitResponse;
import com.commercepal.apiservice.users.role.dto.RoleResponse;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleDefinitionServiceImpl implements RoleDefinitionService {

  private final RoleDefinitionRepository roleRepository;

  private static final RoleSeed[] ROLE_SEEDS = {
      // Backend staff roles
      new RoleSeed(RoleCode.ROLE_SUPER_ADMIN, "Super Administrator",
          "Full system access with all permissions"),
      new RoleSeed(RoleCode.ROLE_ADMIN, "Administrator",
          "Administrative access to manage system"),
      new RoleSeed(RoleCode.ROLE_CEO, "Chief Executive Officer",
          "Executive dashboard and analytics access"),
      new RoleSeed(RoleCode.ROLE_FINANCE, "Finance Officer",
          "Financial operations and reporting"),
      new RoleSeed(RoleCode.ROLE_FINANCE_MANAGER, "Finance Manager",
          "Finance team management and oversight"),
      new RoleSeed(RoleCode.ROLE_WAREHOUSE, "Warehouse Staff",
          "Warehouse operations and inventory handling"),
      new RoleSeed(RoleCode.ROLE_WAREHOUSE_MANAGER, "Warehouse Manager",
          "Warehouse management and logistics oversight"),
      new RoleSeed(RoleCode.ROLE_CALL_CENTER, "Call Center Agent",
          "Customer support and service operations"),
      new RoleSeed(RoleCode.ROLE_CALL_CENTER_MANAGER, "Call Center Manager",
          "Support team management and quality assurance"),
      new RoleSeed(RoleCode.ROLE_INVENTORY_MANAGER, "Inventory Manager",
          "Inventory control and stock management"),
      new RoleSeed(RoleCode.ROLE_ORDER_MANAGER, "Order Manager",
          "Order fulfillment and processing management"),
      new RoleSeed(RoleCode.ROLE_MARKETING, "Marketing Officer",
          "Marketing campaigns and promotions"),
      new RoleSeed(RoleCode.ROLE_SALES, "Sales Officer",
          "Sales operations and customer acquisition"),
      new RoleSeed(RoleCode.ROLE_HR, "Human Resources",
          "HR management and employee operations"),
      new RoleSeed(RoleCode.ROLE_SUPPORT, "Support Staff",
          "General support and assistance"),
      new RoleSeed(RoleCode.ROLE_MANAGER, "Manager",
          "General management responsibilities"),
      // Customer-facing roles
      new RoleSeed(RoleCode.ROLE_CUSTOMER, "Customer",
          "Regular customer account"),
      new RoleSeed(RoleCode.ROLE_MERCHANT, "Merchant",
          "Merchant/seller account with ability to list and sell products"),
      // Agent/Partner roles
      new RoleSeed(RoleCode.ROLE_AGENT, "Agent",
          "General sales agent with commission-based compensation"),
      new RoleSeed(RoleCode.ROLE_FINANCIAL_INSTITUTION_AGENT, "Financial Institution Agent",
          "Banking or financial institution partner agent with special privileges"),
      new RoleSeed(RoleCode.ROLE_AFFILIATE, "Affiliate Partner",
          "Affiliate/referral partner earning commission on referred sales")
  };

  @Override
  public List<RoleResponse> findAllRoles() {
    return roleRepository.findAll().stream()
        .filter(r -> r.getCode() != RoleCode.ROLE_CUSTOMER
        && r.getCode() != RoleCode.ROLE_MERCHANT
        && r.getCode() != RoleCode.ROLE_ADMIN
        && r.getCode() != RoleCode.ROLE_SUPER_ADMIN
        && r.getCode() != RoleCode.ROLE_AGENT)
        .map(RoleResponse::from)
        .toList();
  }

  @Override
  public Set<RoleDefinition> getRoleDefinitions(Set<RoleCode> roleCodes) {
    Set<RoleDefinition> roles = new HashSet<>();
    for (RoleCode roleCode : roleCodes) {
      RoleDefinition role = roleRepository.findByCode(roleCode)
          .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));
      roles.add(role);
    }
    return roles;
  }

  @Override
  public RoleInitResponse ensureRolesFromRoleCodes() {
    int createdCount = 0;
    int existingCount = 0;

    for (RoleSeed seed : ROLE_SEEDS) {
      if (roleRepository.existsByCode(seed.code)) {
        existingCount++;
        continue;
      }
      RoleDefinition role = RoleDefinition.create(seed.code, seed.name, seed.description);
      roleRepository.save(role);
      log.info("Role created | name={} | code={}", seed.name, seed.code);
      createdCount++;
    }

    log.info("Roles ensure complete: {} created, {} already existing", createdCount, existingCount);
    return new RoleInitResponse(createdCount, existingCount);
  }

  private record RoleSeed(RoleCode code, String name, String description) {
  }
}
