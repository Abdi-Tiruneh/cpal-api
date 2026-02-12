package com.commercepal.apiservice.users.customer.address.migration;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

  Optional<Country> findByName(String name);

  Optional<Country> findByCountryCode(String countryCode);

  List<Country> findAllByDeliveryAllowed(Boolean deliveryAllowed);

  Optional<Country> findByNameAndIdNot(String name, Long id);

  Optional<Country> findByCountryCodeAndIdNot(String countryCode, Long id);

  List<Country> findAllByDeliveryAllowed(Boolean deliveryAllowed, Sort sort);

  // New method with sorting
  List<Country> findAll(Sort sort);
}