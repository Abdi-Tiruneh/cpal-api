package com.commercepal.apiservice.users.staff.enums;

import lombok.Getter;

/**
 * Status enumeration for staff members.
 */
@Getter
public enum StaffStatus {
  ACTIVE("Active", "Currently employed and active"),
  ON_LEAVE("On Leave", "Temporarily on leave (e.g. vacation, sick leave)"),
  SUSPENDED("Suspended", "Employment suspended pending review"),
  TERMINATED("Terminated", "Employment ended by the organization"),
  RESIGNED("Resigned", "Voluntarily left the organization");

  private final String displayName;
  private final String description;

  StaffStatus(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }
}
