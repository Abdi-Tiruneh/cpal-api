package com.commercepal.apiservice.orders.core.service;

import com.commercepal.apiservice.orders.core.dto.AdminOrderPageRequestDto;
import com.commercepal.apiservice.orders.core.dto.AdminOrderResponse;
import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import com.commercepal.apiservice.orders.core.specification.OrderSpecification;
import com.commercepal.apiservice.orders.tracking.dto.DeliveryAddressSummary;
import com.commercepal.apiservice.orders.tracking.dto.OrderItemSummary;
import com.commercepal.apiservice.orders.tracking.service.OrderTrackingMapper;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for admin order management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminOrderServiceImpl implements AdminOrderService {

  private final OrderRepository orderRepository;
  private final OrderTrackingMapper orderTrackingMapper;

  @Override
  public Page<AdminOrderResponse> getOrders(AdminOrderPageRequestDto requestDto) {
    log.debug("[ADMIN-ORDER] Fetching orders with filters - page={}, size={}, sortBy={}",
        requestDto.page(), requestDto.size(), requestDto.sortBy());

    Specification<Order> specification = OrderSpecification.buildSpecification(requestDto);
    Page<Order> ordersPage = orderRepository.findAll(specification, requestDto.toPageable());

    log.info("[ADMIN-ORDER] Found {} orders (page {} of {})",
        ordersPage.getTotalElements(),
        ordersPage.getNumber() + 1,
        ordersPage.getTotalPages());

    return ordersPage.map(this::mapToAdminResponse);
  }

  @Override
  public AdminOrderResponse getOrderById(Long orderId) {
    log.debug("[ADMIN-ORDER] Fetching order by ID: {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> {
          log.error("[ADMIN-ORDER] Order not found - ID: {}", orderId);
          return new ResourceNotFoundException("Order not found with ID: " + orderId);
        });

    return mapToAdminResponse(order);
  }

  @Override
  public AdminOrderResponse getOrderByOrderNumber(String orderNumber) {
    log.debug("[ADMIN-ORDER] Fetching order by order number: {}", orderNumber);

    Order order = orderRepository.findByOrderNumber(orderNumber)
        .orElseThrow(() -> {
          log.error("[ADMIN-ORDER] Order not found - Order Number: {}", orderNumber);
          return new ResourceNotFoundException("Order not found with order number: " + orderNumber);
        });

    return mapToAdminResponse(order);
  }

  /**
   * Maps Order entity to AdminOrderResponse DTO.
   */
  private AdminOrderResponse mapToAdminResponse(Order order) {
    // Get customer information
    var customer = order.getCustomer();
    String customerName = buildCustomerName(customer);
    String customerEmail = customer != null && customer.getCredential() != null
        ? customer.getCredential().getEmailAddress() : null;
    String customerPhone = customer != null && customer.getCredential() != null
        ? customer.getCredential().getPhoneNumber() : null;

    // Map order items using existing mapper
    List<OrderItemSummary> items = order.getOrderItems().stream()
        .map(orderTrackingMapper::toOrderItemSummary)
        .collect(Collectors.toList());

    // Map delivery address using existing mapper
    DeliveryAddressSummary deliveryAddress = orderTrackingMapper.mapDeliveryAddress(
        order.getDeliveryAddress());

    return AdminOrderResponse.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber())
        .customerId(customer != null ? customer.getId() : null)
        .customerAccountNumber(customer != null ? customer.getAccountNumber() : null)
        .customerName(customerName)
        .customerEmail(customerEmail)
        .customerPhone(customerPhone)
        .platform(order.getPlatform())
        .priority(order.getPriority())
        .currency(order.getOrderCurrency())
        .subtotal(order.getSubtotal())
        .taxAmount(order.getTaxAmount())
        .deliveryFee(order.getDeliveryFee())
        .discountAmount(order.getDiscountAmount())
        .additionalCharges(order.getAdditionalCharges())
        .totalAmount(order.getTotalAmount())
        .currentStage(order.getCurrentStage())
        .paymentStatus(order.getPaymentStatus())
        .refundStatus(order.getRefundStatus())
        .refundedAmount(order.getRefundedAmount())
        .totalItemsCount(order.getTotalItemsCount())
        .items(items)
        .deliveryAddress(deliveryAddress)
        .orderedAt(order.getOrderedAt())
        .completedAt(order.getCompletedAt())
        .cancelledAt(order.getCancelledAt())
        .cancellationReason(order.getCancellationReason())
        .paymentConfirmedAt(order.getPaymentConfirmedAt())
        .paymentReference(order.getPaymentReference())
        .refundInitiatedAt(order.getRefundInitiatedAt())
        .refundCompletedAt(order.getRefundCompletedAt())
        .isAgentInitiated(order.getIsAgentInitiated())
        .agentId(order.getAgentId())
        .cartId(order.getCartId())
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .build();
  }

  private String buildCustomerName(com.commercepal.apiservice.users.customer.Customer customer) {
    if (customer == null) {
      return null;
    }
    if (customer.getLastName() != null && !customer.getLastName().isBlank()) {
      return customer.getFirstName() + " " + customer.getLastName();
    }
    return customer.getFirstName();
  }
}
