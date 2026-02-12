package com.commercepal.apiservice.users.customer.address.migration;

import com.commercepal.apiservice.users.customer.address.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAddressServiceOLd {

  private final CustomerAddressRepository customerAddressRepository;
  private final RegionRepository regionRepository;
  private final CityRepository cityRepository;
  private final CountryRepository countryRepository;
  private final AddressRepository addressRepository;


}