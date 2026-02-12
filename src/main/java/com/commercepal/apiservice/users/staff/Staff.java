package com.commercepal.apiservice.users.staff;

import com.commercepal.apiservice.shared.BaseAuditEntity;
import com.commercepal.apiservice.users.credential.Credential;
import com.commercepal.apiservice.users.staff.enums.EmploymentType;
import com.commercepal.apiservice.users.staff.enums.StaffDepartment;
import com.commercepal.apiservice.users.staff.enums.StaffStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Staff entity representing backend users (admin, warehouse, call center, etc.) in the e-commerce
 * system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
    name = "staff",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_staff_employee_id", columnNames = "employee_id"),
        @UniqueConstraint(name = "uk_staff_credential_id", columnNames = "credential_id")
    },
    indexes = {
        @Index(name = "idx_staff_department", columnList = "department"),
        @Index(name = "idx_staff_status", columnList = "status"),
        @Index(name = "idx_staff_first_name", columnList = "first_name"),
        @Index(name = "idx_staff_last_name", columnList = "last_name"),
        @Index(name = "idx_staff_manager_id", columnList = "manager_id")
    }
)
public class Staff extends BaseAuditEntity {

  // Staff Specific Fields
  @Column(name = "employee_id", nullable = false, unique = true, length = 20)
  private String employeeId;

  @Column(name = "first_name", nullable = false, length = 120)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 120)
  private String lastName;

  @Column(name = "middle_name", length = 120)
  private String middleName;

  @Enumerated(EnumType.STRING)
  @Column(name = "department", nullable = false, length = 32)
  private StaffDepartment department;

  @Column(name = "position", nullable = false, length = 120)
  private String position;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "employment_type", nullable = false, length = 16)
  private EmploymentType employmentType = EmploymentType.FULL_TIME;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private StaffStatus status = StaffStatus.ACTIVE;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @Column(name = "termination_date")
  private LocalDate terminationDate;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "nationality", length = 3)
  private String nationality;

  @Column(name = "national_id", length = 50)
  private String nationalId;

  @Column(name = "address", length = 500)
  private String address;

  @Column(name = "city", length = 120)
  private String city;

  @Column(name = "state_province", length = 50)
  private String stateProvince;

  @Column(name = "country", length = 3)
  private String country;

  @Column(name = "emergency_contact_name", length = 120)
  private String emergencyContactName;

  @Column(name = "emergency_contact_phone", length = 32)
  private String emergencyContactPhone;

  @Column(name = "emergency_contact_relationship", length = 50)
  private String emergencyContactRelationship;

  @Column(name = "manager_id")
  private Long managerId;

  @Column(name = "admin_notes", length = 2000)
  private String adminNotes;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "credential_id", nullable = false, unique = true)
  private Credential credential;

  /**
   * Links the staff member to their account credentials.
   */
  public void linkCredential(Credential credential) {
    this.credential = credential;
  }

  /**
   * Gets the full name of the staff member.
   */
  public String getFullName() {
    if (middleName != null && !middleName.isEmpty()) {
      return firstName + " " + middleName + " " + lastName;
    }
    return firstName + " " + lastName;
  }

  /**
   * Terminates the staff member's employment.
   */
  public void terminate(LocalDate terminationDate, String reason) {
    this.status = StaffStatus.TERMINATED;
    this.terminationDate = terminationDate;
    if (reason != null) {
      this.setRemarks(reason);
    }
  }
}

