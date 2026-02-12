package com.commercepal.apiservice.users.role;

/**
 * Role code enumeration representing different roles in the system. Backend staff roles are used
 * for internal system operations and management. Customer-facing roles are used for external
 * users.
 */
public enum RoleCode {
  // Customer-facing roles
  ROLE_CUSTOMER,
  ROLE_MERCHANT,

  // Agent roles - different types of agents/partners
  ROLE_AGENT,                          // General sales agent
  ROLE_FINANCIAL_INSTITUTION_AGENT,    // Financial institution partner/agent
  ROLE_AFFILIATE,                      // Affiliate/referral partner

  // Backend staff roles
  ROLE_SUPER_ADMIN,           // Full system access
  ROLE_ADMIN,                 // Administrative access
  ROLE_CEO,                   // Executive dashboard access
  ROLE_FINANCE,               // Financial operations and reporting
  ROLE_FINANCE_MANAGER,       // Finance team management
  ROLE_WAREHOUSE,             // Warehouse operations
  ROLE_WAREHOUSE_MANAGER,     // Warehouse management
  ROLE_CALL_CENTER,           // Customer support operations
  ROLE_CALL_CENTER_MANAGER,   // Support team management
  ROLE_INVENTORY_MANAGER,     // Inventory management
  ROLE_ORDER_MANAGER,         // Order fulfillment management
  ROLE_MARKETING,             // Marketing operations
  ROLE_SALES,                 // Sales operations
  ROLE_HR,                    // Human resources
  ROLE_SUPPORT,               // General support
  ROLE_MANAGER                // General manager
}

