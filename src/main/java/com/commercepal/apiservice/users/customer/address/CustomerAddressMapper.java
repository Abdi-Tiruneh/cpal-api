package com.commercepal.apiservice.users.customer.address;

import com.commercepal.apiservice.users.customer.address.dto.AddressRequest;
import com.commercepal.apiservice.users.customer.address.dto.AddressResponse;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.MapperUtils;

/**
 * Dedicated mapper for converting between Address DTOs and CustomerAddress entities.
 */
public final class CustomerAddressMapper {

  private CustomerAddressMapper() {
    // Utility class
  }

  public static CustomerAddress toEntity(AddressRequest request, Customer customer) {
    if (request == null || customer == null) {
      return null;
    }

    return CustomerAddress.builder()
        .customer(customer)
        .receiverName(request.receiverName())
        .phoneNumber(request.phoneNumber())
        .country(request.countryCode())
        .state(request.state())
        .city(request.city())
        .district(request.district())
        .street(request.street())
        .houseNumber(request.houseNumber())
        .landmark(request.landmark())
        .addressLine1(request.addressLine1())
        .addressLine2(request.addressLine2())
        .latitude(request.latitude())
        .longitude(request.longitude())
        .addressSource(request.addressSource())
        .isDefault(Boolean.TRUE.equals(request.isDefault()))
        .build();
  }

  public static void applyRequest(CustomerAddress address, AddressRequest request) {
    if (address == null || request == null) {
      return;
    }

    MapperUtils.applyIfNotBlank(request.receiverName(), address::setReceiverName);
    MapperUtils.applyIfNotBlank(request.phoneNumber(), address::setPhoneNumber);
    MapperUtils.applyIfNotBlank(request.countryCode(), address::setCountry);
    MapperUtils.applyIfNotBlank(request.state(), address::setState);
    MapperUtils.applyIfNotBlank(request.city(), address::setCity);
    MapperUtils.applyIfNotBlank(request.district(), address::setDistrict);
    MapperUtils.applyIfNotBlank(request.street(), address::setStreet);
    MapperUtils.applyIfNotBlank(request.houseNumber(), address::setHouseNumber);
    MapperUtils.applyIfNotBlank(request.landmark(), address::setLandmark);
    MapperUtils.applyIfNotBlank(request.addressLine1(), address::setAddressLine1);
    MapperUtils.applyIfNotBlank(request.addressLine2(), address::setAddressLine2);
    MapperUtils.applyIfNotBlank(request.latitude(), address::setLatitude);
    MapperUtils.applyIfNotBlank(request.longitude(), address::setLongitude);
    MapperUtils.applyIfNotNull(request.addressSource(), address::setAddressSource);
  }

  public static AddressResponse toResponse(CustomerAddress address, boolean canEdit,
      boolean canDelete) {
    if (address == null) {
      return null;
    }

    return AddressResponse.builder()
        .id(address.getId())
        .isDefault(Boolean.TRUE.equals(address.getIsDefault()))
        .canEdit(canEdit)
        .canDelete(canDelete)
        .country(address.getCountry())
        .receiverName(address.getReceiverName())
        .phoneNumber(address.getPhoneNumber())
        .state(address.getState())
        .city(address.getCity())
        .district(address.getDistrict())
        .street(address.getStreet())
        .houseNumber(address.getHouseNumber())
        .landmark(address.getLandmark())
        .addressLine1(address.getAddressLine1())
        .addressLine2(address.getAddressLine2())
        .latitude(address.getLatitude())
        .longitude(address.getLongitude())
        .addressSource(address.getAddressSource())
        .createdAt(address.getCreatedAt())
        .updatedAt(address.getUpdatedAt())
        .build();
  }

  public static AddressResponse toResponse(CustomerAddress address) {
    return toResponse(address, true,
        true); // Default to editable/deletable if not specified (backward compatibility)
  }
}
