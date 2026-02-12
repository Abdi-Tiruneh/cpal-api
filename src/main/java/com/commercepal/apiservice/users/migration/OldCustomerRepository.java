package com.commercepal.apiservice.users.migration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for OldCustomer persistence.
 */
@Repository
public interface OldCustomerRepository extends JpaRepository<OldCustomer, Long> {

}

