package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.users.credential.Credential;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for customer persistence.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>,
    JpaSpecificationExecutor<Customer> {

  Optional<Customer> findByAccountNumber(String accountNumber);

  Optional<Customer> findByCommissionAccount(String commissionAccount);

  Optional<Customer> findByReferralCode(String referralCode);

  Optional<Customer> findByCredential(Credential credential);

  Optional<Customer> findByCredential_Id(Long credentialId);

  /**
   * Find customer by credential ID with credential eagerly loaded.
   * This prevents LazyInitializationException when accessing credential properties.
   *
   * @param credentialId the credential ID
   * @return Optional Customer with credential loaded
   */
  @EntityGraph(attributePaths = {"credential"})
  @Query("SELECT c FROM Customer c WHERE c.credential.id = :credentialId")
  Optional<Customer> findByCredentialIdWithCredential(@Param("credentialId") Long credentialId);

  /**
   * Find customer by ID with credential eagerly loaded.
   * This prevents LazyInitializationException when accessing credential properties.
   *
   * @param id the customer ID
   * @return Optional Customer with credential loaded
   */
  @EntityGraph(attributePaths = {"credential"})
  @Query("SELECT c FROM Customer c WHERE c.id = :id")
  Optional<Customer> findByIdWithCredential(@Param("id") Long id);

  Optional<Customer> findByOldCustomerId(Long domainUserId);

  boolean existsByReferralCode(String referralCode);
}
