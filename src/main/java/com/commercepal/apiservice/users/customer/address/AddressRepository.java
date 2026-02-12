package com.commercepal.apiservice.users.customer.address;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<CustomerAddress, Long> {

  List<CustomerAddress> findByCustomer_IdAndIsDeletedFalse(Long customerId);

  Optional<CustomerAddress> findByIdAndCustomer_IdAndIsDeletedFalse(Long id, Long customerId);

  boolean existsByCustomer_IdAndIsDefaultTrueAndIsDeletedFalse(Long customerId);

  Optional<CustomerAddress> findFirstByCustomer_IdAndIsDefaultTrueAndIsDeletedFalse(
      Long customerId);

  Optional<CustomerAddress> findByOldAddressId(Long oldAddressId);

  boolean existsByOldAddressId(Long oldAddressId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update CustomerAddress ca set ca.isDefault = false where ca.customer.id = :customerId and ca.id <> :addressId and ca.isDeleted = false")
  void clearDefaultForCustomerExcept(Long customerId, Long addressId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update CustomerAddress ca set ca.isDefault = false where ca.customer.id = :customerId and ca.isDeleted = false")
  void clearDefaultForCustomer(Long customerId);
}
