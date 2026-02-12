package com.commercepal.apiservice.users.customer.address.migration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddressOld, Long> {

}
