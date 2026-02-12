package com.commercepal.apiservice.users.staff.enums;

import lombok.Getter;

/**
 * Employment type enumeration for staff members.
 */
@Getter
public enum EmploymentType {
  FULL_TIME("Full Time", "Permanent full-time employment"),
  PART_TIME("Part Time", "Part-time employment with reduced hours"),
  CONTRACT("Contract", "Fixed-term contract engagement"),
  TEMPORARY("Temporary", "Temporary or seasonal employment"),
  INTERN("Intern", "Internship or trainee position");

  private final String displayName;
  private final String description;

  EmploymentType(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }
}
