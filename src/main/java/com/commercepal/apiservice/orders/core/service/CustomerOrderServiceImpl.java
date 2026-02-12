package com.commercepal.apiservice.orders.core.service;

import com.commercepal.apiservice.orders.core.customer.CustomerOrderStatusMapper;
import com.commercepal.apiservice.orders.core.customer.dto.*;
import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.repository.OrderItemRepository;
import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import com.commercepal.apiservice.users.customer.address.CustomerAddress;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOrderServiceImpl implements CustomerOrderService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<CustomerOrderListDto> getMyOrders(Long customerId, Pageable pageable) {

    return orderRepository
        .findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
        .map(order -> CustomerOrderListDto.builder()
            .orderNumber(order.getOrderNumber())
            .orderedAt(order.getOrderedAt())
            .status(CustomerOrderStatusMapper.map(order))
            .totalAmount(order.getTotalAmount())
            .currency(order.getOrderCurrency().name())
            .items(
                order.getOrderItems().stream()
                    .limit(1)
                    .map(i -> ItemPreviewDto.builder()
                        .name(i.getProductName())
                        .image(i.getProductImageUrl())
                        .quantity(i.getQuantity())
                        .status(i.getCurrentStage().name())
                        .build())
                    .collect(Collectors.toList()))
            .actions(buildActions(order))
            .build());
  }

  @Override
  @Transactional(readOnly = true)
  public CustomerOrderDetailsDto getOrderDetails(Long customerId, String orderNumber) {

    Order order = orderRepository.findByOrderNumberAndCustomerId(orderNumber, customerId)
        .orElseThrow(() -> new RuntimeException("Order not found"));

    var items = orderItemRepository.findByOrderId(order.getId()).stream()
        .map(i -> CustomerOrderItemDto.builder()
            .subOrderNumber(i.getSubOrderNumber())
            .name(i.getProductName())
            .image(i.getProductImageUrl())
            .provider(i.getProvider().name())
            .status(i.getCurrentStage().name())
            .quantity(i.getQuantity())
            .price(i.getTotalAmount())
            .trackingNumber(i.getShipmentTrackingNumber())
            .actions(buildItemActions(i))
            .build())
        .toList();

    return CustomerOrderDetailsDto.builder()
        .orderNumber(order.getOrderNumber())
        .status(CustomerOrderStatusMapper.map(order))
        .orderedAt(order.getOrderedAt())
        .payment(
            PaymentDto.builder()
                .status(order.getPaymentStatus().name())
                .paidAt(order.getPaymentConfirmedAt())
                .build())
        .deliveryAddress(
            DeliveryAddressDto.builder()
                .fullName(buildFullName(order.getCustomer().getFirstName(), order.getCustomer().getLastName()))
                .phone(
                    order.getCustomer().getCredential() != null ? order.getCustomer().getCredential().getPhoneNumber()
                        : null)
                .city(order.getDeliveryAddress().getCity())
                .fullAddress(formatFullAddress(order.getDeliveryAddress(), order.getCustomer()))
                .build())
        .items(items)
        .summary(
            OrderSummaryDto.builder()
                .subtotal(order.getSubtotal())
                .delivery(order.getDeliveryFee())
                .discount(order.getDiscountAmount())
                .total(order.getTotalAmount())
                .build())
        .build();
  }

  private OrderActionsDto buildActions(Order order) {
    return OrderActionsDto.builder()
        .canPay(order.getPaymentStatus().name().equals("PENDING"))
        .canTrack(order.getOrderItems().stream().anyMatch(i -> i.getProviderTrackingNumber() != null))
        .build();
  }

  private OrderActionsDto buildItemActions(Object i) {
    return OrderActionsDto.builder()
        .canPay(false)
        .canTrack(true)
        .build();
  }

  private String buildFullName(String firstName, String lastName) {
    if (firstName == null && lastName == null) {
      return null;
    }
    if (firstName == null) {
      return lastName;
    }
    if (lastName == null) {
      return firstName;
    }
    return firstName + " " + lastName;
  }

  private String formatFullAddress(CustomerAddress address, com.commercepal.apiservice.users.customer.Customer customer) {
    if (address == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    // Street address
    if (address.getStreet() != null) {
      sb.append(address.getStreet());
    }

    // District/Subcity (if available)
    if (address.getDistrict() != null) {
      if (!sb.isEmpty()) {
        sb.append(" - ");
      }
      sb.append(address.getDistrict());
    }

    // City, State, Country format (repeated as in example: City,City,State,Country)
    String city = address.getCity() != null ? address.getCity() : "";
    String state = address.getState() != null ? address.getState() : "";
    String country = address.getCountry() != null ? address.getCountry() : "";
    
    if (!city.isEmpty() || !state.isEmpty() || !country.isEmpty()) {
      if (!sb.isEmpty()) {
        sb.append(" - ");
      }
      // Format: City,City,State,Country (repeating city as in example)
      sb.append(String.join(",", city, city, state, country));
    }

    // Postal code placeholder (if available in future)
    // sb.append(" 1000");

    // Masked name
    String maskedName = maskName(buildFullName(customer.getFirstName(), customer.getLastName()));
    if (maskedName != null) {
      if (!sb.isEmpty()) {
        sb.append(" ");
      }
      sb.append(maskedName);
    }

    // Masked phone
    String phoneNumber = address.getPhoneNumber();
    if (phoneNumber != null) {
      if (!sb.isEmpty()) {
        sb.append(" ");
      }
      sb.append(maskPhoneNumber(phoneNumber));
    }

    return sb.toString();
  }

  private String maskName(String fullName) {
    if (fullName == null || fullName.length() < 2) {
      return fullName;
    }
    if (fullName.length() <= 3) {
      return fullName.charAt(0) + "*".repeat(fullName.length() - 1);
    }
    // Show first and last character, mask the middle
    char first = fullName.charAt(0);
    char last = fullName.charAt(fullName.length() - 1);
    return first + "*".repeat(fullName.length() - 2) + last;
  }

  private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 8) {
      return phoneNumber;
    }
    // Mask middle digits: +251 91*****901
    int visibleStart = Math.min(7, phoneNumber.length() - 3);
    int visibleEnd = Math.max(visibleStart + 1, phoneNumber.length() - 3);

    String start = phoneNumber.substring(0, visibleStart);
    String end = phoneNumber.substring(visibleEnd);
    String masked = "*".repeat(visibleEnd - visibleStart);

    return start + masked + end;
  }
}
