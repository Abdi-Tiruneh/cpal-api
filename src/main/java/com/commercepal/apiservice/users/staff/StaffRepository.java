package com.commercepal.apiservice.users.staff;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Staff entity.
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long>,
    JpaSpecificationExecutor<Staff> {

  /**
   * Find staff member by credential ID.
   */
  Optional<Staff> findByCredentialIdAndIsDeletedFalse(Long credentialId);

  /**
   * Find staff member by employee ID.
   */
  Optional<Staff> findByEmployeeIdAndIsDeletedFalse(String employeeId);

  /**
   * Check if employee ID exists.
   */
  boolean existsByEmployeeIdAndIsDeletedFalse(String employeeId);
}

