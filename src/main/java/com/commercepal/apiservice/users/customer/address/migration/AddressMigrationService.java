package com.commercepal.apiservice.users.customer.address.migration;

import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.users.customer.address.AddressRepository;
import com.commercepal.apiservice.users.customer.address.AddressSourceType;
import com.commercepal.apiservice.users.customer.address.CustomerAddress;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.users.customer.CustomerRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressMigrationService {

  private final CustomerAddressRepository customerAddressOldRepository;
  private final AddressRepository addressRepository;
  private final CustomerRepository customerRepository;

  @Transactional
  public String migrateAddresses() {
    log.info("Starting address migration...");
    List<CustomerAddressOld> oldAddresses = customerAddressOldRepository.findAll();
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger unknownCustomerCount = new AtomicInteger();

    for (CustomerAddressOld oldAddress : oldAddresses) {
      try {
        // Find Customer by domainUserId (assuming customerId maps to domainUserId)
        Customer customer = customerRepository.findByOldCustomerId(oldAddress.getCustomerId())
            .orElse(null);

        if (customer == null) {
          log.warn("Customer not found for old address | oldAddressId={} | customerId={}",
              oldAddress.getId(), oldAddress.getCustomerId());
          unknownCustomerCount.getAndIncrement();
          continue;
        }

        if (addressRepository.existsByOldAddressId(oldAddress.getId())) {
          continue;
        }

        CustomerAddress newAddress = mapToNewAddress(oldAddress, customer);
        addressRepository.save(newAddress);
        successCount.getAndIncrement();

      } catch (Exception e) {
        log.error("Failed to migrate address ID: {}", oldAddress.getId(), e);
      }
    }

    return String.format("Migration completed. Migrated: %d, Unknown Customers: %d, Total Old: %d",
        successCount.get(), unknownCustomerCount.get(), oldAddresses.size());
  }

  private CustomerAddress mapToNewAddress(CustomerAddressOld old, Customer customer) {
    String cityName = resolveCityName(old);
    String regionName = resolveRegionName(old);

    return CustomerAddress.builder()
        .oldAddressId(old.getId())
        .customer(customer)
        .receiverName(
            customer.getFirstName() + " " + (customer.getLastName() != null ? customer.getLastName()
                : ""))
        .phoneNumber(
            old.getPhoneNumber() != null ? old.getPhoneNumber() : customer.getAccountNumber())
        .country(SupportedCountry.fromCode(customer.getCountry()).name())
        .state(regionName)
        .city(cityName)
        .district(old.getSubCity())
        .street(old.getPhysicalAddress())
        .addressLine1(old.getPhysicalAddress())
        .latitude(old.getLatitude())
        .longitude(old.getLongitude())
        .addressSource(old.getAddressSource() != null ? AddressSourceType.valueOf(
            old.getAddressSource().name())
            : AddressSourceType.MANUAL)
        .isDefault(old.getIsDefault() != null ? old.getIsDefault() : false)
        .build();
  }

  private String resolveCityName(CustomerAddressOld old) {
    if (old.getManualCityName() != null && !old.getManualCityName().isEmpty()) {
      return old.getManualCityName();
    }
    if (old.getCity() != null) {
      return old.getCity().getCity();
    }
    return null;
  }

  private String resolveRegionName(CustomerAddressOld old) {
    if (old.getManualRegionName() != null && !old.getManualRegionName().isEmpty()) {
      return old.getManualRegionName();
    }
    if (old.getRegion() != null) {
      return old.getRegion().getRegionName();
    }
    return null;
  }
}
