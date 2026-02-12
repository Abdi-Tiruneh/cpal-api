package com.commercepal.apiservice.users.customer.address.migration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {

  boolean existsByRegionNameIgnoreCaseAndCountryId(String regionName, Long countryId);

  List<Region> findByCountryIdAndDeliveryAllowed(Long countryId, boolean deliveryAllowed);

  boolean existsByRegionNameIgnoreCaseAndIdNot(String regionName, Integer regionId);

}
