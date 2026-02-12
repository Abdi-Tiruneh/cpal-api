package com.commercepal.apiservice.users.customer.address.migration;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

  List<City> findByRegionId(Integer id);

  boolean existsByCityIgnoreCaseAndRegionId(String city, Integer regionId);

  boolean existsByCityIgnoreCaseAndCityIdNot(String s, Long id);
}
