package com.commercepal.apiservice.users.customer.address;

import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.users.customer.address.dto.AddressRequest;
import com.commercepal.apiservice.users.customer.address.dto.AddressResponse;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.PhoneValidationUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class for managing Customer Addresses. Handles creation, updating, retrieval, and
 * deletion of addresses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAddressService {

  private static final List<OrderStage> ACTIVE_STAGES = Arrays.stream(OrderStage.values())
      .filter(OrderStage::isActive)
      .toList();
  private final AddressRepository addressRepository;
  private final OrderRepository orderRepository;
  private final CurrentUserService currentUserService;

  @Transactional(Transactional.TxType.SUPPORTS)
  public List<AddressResponse> getMyAddresses() {
    Customer customer = currentUserService.getCurrentCustomer();
    return addressRepository.findByCustomer_IdAndIsDeletedFalse(customer.getId())
        .stream()
        .sorted(Comparator.comparing((CustomerAddress a) -> !Boolean.TRUE.equals(a.getIsDefault()))
            .thenComparing(a -> a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt(),
                Comparator.reverseOrder()))
        .map(address -> {
          boolean hasActiveOrders = hasActiveOrders(address.getId());
          return CustomerAddressMapper.toResponse(address, !hasActiveOrders, !hasActiveOrders);
        })
        .collect(Collectors.toList());
  }

  @Transactional(Transactional.TxType.SUPPORTS)
  public AddressResponse getAddressById(Long addressId) {
    Customer customer = currentUserService.getCurrentCustomer();
    CustomerAddress address = getAddressEntity(addressId, customer.getId());
    boolean hasActiveOrders = hasActiveOrders(addressId);
    return CustomerAddressMapper.toResponse(address, !hasActiveOrders, !hasActiveOrders);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public AddressResponse createAddress(AddressRequest request) {
    Customer customer = currentUserService.getCurrentCustomer();

    PhoneValidationUtil.validatePhoneNumberLength(request.phoneNumber(),
        SupportedCountry.fromCode(request.countryCode()));

    boolean hasDefault = addressRepository.existsByCustomer_IdAndIsDefaultTrueAndIsDeletedFalse(
        customer.getId());
    boolean makeDefault = Boolean.TRUE.equals(request.isDefault()) || !hasDefault;

    CustomerAddress address = CustomerAddressMapper.toEntity(request, customer);
    address.setIsDefault(makeDefault);
    address.setOldAddressId(null); // New addresses don't have legacy IDs

    CustomerAddress saved = addressRepository.save(address);
    if (Boolean.TRUE.equals(saved.getIsDefault())) {
      addressRepository.clearDefaultForCustomerExcept(customer.getId(), saved.getId());
    }

    // New address has no orders
    return CustomerAddressMapper.toResponse(saved, true, true);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public AddressResponse updateAddress(Long addressId, AddressRequest request) {
    Customer customer = currentUserService.getCurrentCustomer();

    if (request.phoneNumber() != null && !request.phoneNumber().isEmpty()) {
      PhoneValidationUtil.validatePhoneNumberLength(request.phoneNumber(),
          SupportedCountry.fromCode(request.countryCode()));
    }

    CustomerAddress existing = getAddressEntity(addressId, customer.getId());

    if (hasActiveOrders(addressId)) {
      throw new BadRequestException("Cannot edit address associated with active orders.");
    }

    CustomerAddressMapper.applyRequest(existing, request);
    if (Boolean.TRUE.equals(request.isDefault())) {
      existing.setIsDefault(true);
    }

    CustomerAddress saved = addressRepository.save(existing);
    if (Boolean.TRUE.equals(saved.getIsDefault())) {
      addressRepository.clearDefaultForCustomerExcept(customer.getId(), saved.getId());
    }

    return CustomerAddressMapper.toResponse(saved, true, true);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public AddressResponse setDefault(Long addressId) {
    Customer customer = currentUserService.getCurrentCustomer();
    CustomerAddress address = getAddressEntity(addressId, customer.getId());

    address.setIsDefault(true);
    CustomerAddress saved = addressRepository.save(address);
    addressRepository.clearDefaultForCustomerExcept(customer.getId(), saved.getId());

    boolean hasActiveOrders = hasActiveOrders(addressId);
    return CustomerAddressMapper.toResponse(saved, !hasActiveOrders, !hasActiveOrders);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void deleteAddress(Long addressId) {
    Customer customer = currentUserService.getCurrentCustomer();
    CustomerAddress address = getAddressEntity(addressId, customer.getId());

    if (hasActiveOrders(addressId)) {
      throw new BadRequestException("Cannot delete address associated with active orders.");
    }

    boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

    // Soft delete the address
    String deletedBy = currentUserService.getCurrentUsername();
    address.softDelete(deletedBy);
    addressRepository.save(address);

    if (wasDefault) {
      promoteFallbackDefault(customer.getId());
    }
  }

  private CustomerAddress getAddressEntity(Long addressId, Long customerId) {
    return addressRepository.findByIdAndCustomer_IdAndIsDeletedFalse(addressId, customerId)
        .orElseThrow(() -> new EntityNotFoundException("Address not found for customer"));
  }

  private void promoteFallbackDefault(Long customerId) {
    Optional<CustomerAddress> next = addressRepository.findByCustomer_IdAndIsDeletedFalse(
            customerId)
        .stream()
        .findFirst();
    next.ifPresent(address -> {
      address.setIsDefault(true);
      CustomerAddress saved = addressRepository.save(address);
      addressRepository.clearDefaultForCustomerExcept(customerId, saved.getId());
    });
  }

  private boolean hasActiveOrders(Long addressId) {
    // return orderRepository.existsByDeliveryAddressIdAndCurrentStageIn(addressId,
    // ACTIVE_STAGES);
    return false;
  }
}
