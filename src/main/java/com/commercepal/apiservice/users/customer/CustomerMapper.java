package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.users.migration.OldCustomer;
import com.commercepal.apiservice.utils.MapperUtils;

/**
 * Mapper utility for converting OldCustomer to Customer entity.
 */
public final class CustomerMapper {

  private CustomerMapper() {
    // Utility class - prevent instantiation
  }

  /**
   * Maps OldCustomer to Customer entity. Note: The credential relationship must be set separately
   * using linkCredential().
   *
   * @param oldCustomer the source OldCustomer entity
   * @return a new Customer entity with mapped fields, or null if oldCustomer is null
   * @throws IllegalArgumentException if required fields (accountNumber, commissionAccount,
   *                                  firstName) are missing
   */
  public static Customer fromOldCustomer(OldCustomer oldCustomer) {
    if (oldCustomer == null) {
      return null;
    }

    Customer customer = new Customer();

    if (MapperUtils.isBlank(oldCustomer.getAccountNumber())) {
      throw new IllegalArgumentException("AccountNumber is required but was null or blank");
    }
    customer.setAccountNumber(oldCustomer.getAccountNumber());

    if (MapperUtils.isBlank(oldCustomer.getCommissionAccount())) {
      throw new IllegalArgumentException("CommissionAccount is required but was null or blank");
    }
    customer.setCommissionAccount(oldCustomer.getCommissionAccount());

    if (MapperUtils.isBlank(oldCustomer.getFirstName())) {
      throw new IllegalArgumentException("FirstName is required but was null or blank");
    }
    customer.setFirstName(oldCustomer.getFirstName());
    customer.setLastName(oldCustomer.getLastName());

    customer.setCity(oldCustomer.getCity());
    customer.setStateProvince(oldCustomer.getDistrict());

    if (oldCustomer.getCountryIso() != null) {
      customer.setCountry(oldCustomer.getCountryIso().getCode());
    } else if (!MapperUtils.isBlank(oldCustomer.getCountry())) {
      try {
        customer.setCountry(oldCustomer.getCountry());
      } catch (IllegalArgumentException e) {
      }
    }

    if (!MapperUtils.isBlank(oldCustomer.getLanguage())) {
      customer.setPreferredLanguage(oldCustomer.getLanguage());
    } else {
      customer.setPreferredLanguage("en");
    }

    customer.setReferralCode(oldCustomer.getReferralCode());
    customer.setRegistrationChannel(Channel.WEB);

    return customer;
  }

  /**
   * Maps OldCustomer to Customer with additional context for registration channel.
   *
   * @param oldCustomer         the source OldCustomer entity
   * @param registrationChannel the registration channel to use (if null, defaults to WEB)
   * @return a new Customer entity with mapped fields
   */
  public static Customer fromOldCustomer(OldCustomer oldCustomer, Channel registrationChannel) {
    Customer customer = fromOldCustomer(oldCustomer);
    if (customer != null && registrationChannel != null) {
      customer.setRegistrationChannel(registrationChannel);
    }
    return customer;
  }

  /**
   * Updates an existing Customer entity with values from OldCustomer. Only updates
   * non-null/non-blank values from OldCustomer.
   *
   * @param customer    the target Customer entity to update
   * @param oldCustomer the source OldCustomer entity
   */
  public static void updateCustomerFromOld(Customer customer, OldCustomer oldCustomer) {
    if (customer == null || oldCustomer == null) {
      return;
    }

    MapperUtils.applyIfNotBlank(oldCustomer.getAccountNumber(), customer::setAccountNumber);
    MapperUtils.applyIfNotBlank(oldCustomer.getCommissionAccount(), customer::setCommissionAccount);
    MapperUtils.applyIfNotBlank(oldCustomer.getFirstName(), customer::setFirstName);
    MapperUtils.applyIfNotBlank(oldCustomer.getLastName(), customer::setLastName);
    MapperUtils.applyIfNotBlank(oldCustomer.getCity(), customer::setCity);
    MapperUtils.applyIfNotBlank(oldCustomer.getDistrict(), customer::setStateProvince);

    if (oldCustomer.getCountryIso() != null) {
      customer.setCountry(oldCustomer.getCountryIso().getCode());
    } else if (!MapperUtils.isBlank(oldCustomer.getCountry())) {
      try {
        customer.setCountry(oldCustomer.getCountry().toUpperCase());
      } catch (IllegalArgumentException e) {
      }
    }

    MapperUtils.applyIfNotBlank(oldCustomer.getLanguage(), customer::setPreferredLanguage);
    MapperUtils.applyIfNotBlank(oldCustomer.getReferralCode(), customer::setReferralCode);
  }
}

