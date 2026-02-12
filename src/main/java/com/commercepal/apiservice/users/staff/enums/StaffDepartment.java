package com.commercepal.apiservice.users.staff.enums;

import lombok.Getter;

/**
 * Department enumeration for backend staff members (engineers).
 */
@Getter
public enum StaffDepartment {
  EXECUTIVE("Executive Leadership", "Executive and senior leadership"),
  ADMINISTRATION("Administration", "Administrative and office support"),
  FINANCE("Finance & Accounting", "Finance, accounting and treasury"),
  WAREHOUSE("Warehouse & Logistics", "Warehouse and logistics operations"),
  CALL_CENTER("Call Center & Support", "Customer support and call center"),
  INVENTORY("Inventory Management", "Inventory and stock management"),
  SALES("Sales", "Sales and business development"),
  MARKETING("Marketing", "Marketing and communications"),
  HUMAN_RESOURCES("Human Resources", "HR and people operations"),
  SOFTWARE_ENGINEERS("Software Engineers", "Software development and engineering"),
  OPERATIONS("Operations", "General operations"),
  ORDER_FULFILLMENT("Order Fulfillment", "Order processing and fulfillment");

  private final String displayName;
  private final String description;

  StaffDepartment(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }
}

