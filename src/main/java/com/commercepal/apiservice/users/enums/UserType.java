package com.commercepal.apiservice.users.enums;

/**
 * User type enumeration for distinguishing between different types of users in the system.
 */
public enum UserType {
  CUSTOMER,    // External customers who purchase products
  MERCHANT,    // External merchants who sell products
  AGENT,       // Sales agents/affiliates
  STAFF,       // Internal backend staff (admin, warehouse, call center, etc.)
  SYSTEM       // System-generated operations
}

