package com.commercepal.apiservice.orders.tracking.service;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.tracking.dto.OrderFilterRequest;
import com.commercepal.apiservice.orders.tracking.dto.OrderListResponse;
import com.commercepal.apiservice.orders.tracking.dto.OrderStageStatistics;
import com.commercepal.apiservice.orders.tracking.dto.OrderTrackingResponse;
import com.commercepal.apiservice.orders.tracking.enums.OrderStageCategory;
import com.commercepal.apiservice.orders.tracking.model.OrderTrackingEvent;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OrderQueryService
 * <p>
 * Service for querying and filtering orders for customer order list views.
 * Provides methods for
 * retrieving orders with filtering, pagination, and statistics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

  private final OrderRepository orderRepository;
  private final OrderTrackingService trackingService;
  private final OrderTrackingMapper trackingMapper;

  /**
   * Get filtered orders for a customer
   *
   * @param customerId    Customer ID
   * @param filterRequest Filter and pagination parameters
   * @return Page of order list responses
   */
  @Transactional(readOnly = true)
  public Page<OrderListResponse> getCustomerOrders(Long customerId,
      OrderFilterRequest filterRequest) {

    // Build pageable
    Pageable pageable = buildPageable(filterRequest);

    // Get orders based on filters
    Page<Order> orders;

    if (filterRequest.getStageCategory() == null ||
        filterRequest.getStageCategory() == OrderStageCategory.ALL) {
      // Get all orders for customer
      orders = orderRepository.findByCustomerId(customerId, pageable);
    } else {
      // Filter by stage category
      Set<OrderStage> stages = filterRequest.getStageCategory().getIncludedStages();
      orders = orderRepository.findByCustomerIdAndCurrentStageIn(customerId, stages, pageable);
    }

    // Map to DTOs
    return orders.map(trackingMapper::toOrderListResponse);
  }

  /**
   * Get order details by order number
   *
   * @param customerId  Customer ID (for security validation)
   * @param orderNumber Order number
   * @return Order list response
   */
  @Transactional(readOnly = true)
  public OrderListResponse getOrderDetails(Long customerId, String orderNumber) {
    Order order = orderRepository.findByOrderNumber(orderNumber)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

    // Validate customer ownership
    if (!order.getCustomer().getId().equals(customerId)) {
      throw new IllegalArgumentException("Order does not belong to customer");
    }

    return trackingMapper.toOrderListResponse(order);
  }

  /**
   * Get order tracking timeline
   *
   * @param customerId  Customer ID (for security validation)
   * @param orderNumber Order number
   * @return Order tracking response with timeline
   */
  @Transactional(readOnly = true)
  public OrderTrackingResponse getOrderTracking(Long customerId, String orderNumber) {
    Order order = orderRepository.findByOrderNumber(orderNumber)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

    // Validate customer ownership
    if (!order.getCustomer().getId().equals(customerId)) {
      throw new IllegalArgumentException("Order does not belong to customer");
    }

    // Get customer-visible tracking events
    List<OrderTrackingEvent> trackingEvents = trackingService.getCustomerTimeline(order);

    return trackingMapper.toOrderTrackingResponse(order, trackingEvents);
  }

  /**
   * Get order statistics by stage category
   *
   * @param customerId Customer ID
   * @return Order stage statistics
   */
  @Transactional(readOnly = true)
  public OrderStageStatistics getOrderStatistics(Long customerId) {
    // Count orders in each category
    long toPay = countOrdersByCategory(customerId, OrderStageCategory.TO_PAY);
    long toShip = countOrdersByCategory(customerId, OrderStageCategory.TO_SHIP);
    long shipped = countOrdersByCategory(customerId, OrderStageCategory.SHIPPED);
    long processed = countOrdersByCategory(customerId, OrderStageCategory.PROCESSED);
    long cancelled = countOrdersByCategory(customerId, OrderStageCategory.CANCELLED);
    long total = orderRepository.countByCustomerId(customerId);

    return OrderStageStatistics.builder()
        .toPay(toPay)
        .toShip(toShip)
        .shipped(shipped)
        .processed(processed)
        .cancelled(cancelled)
        .total(total)
        .build();
  }

  /**
   * Get orders by specific stage category
   *
   * @param customerId Customer ID
   * @param category   Stage category
   * @param pageable   Pagination parameters
   * @return Page of order list responses
   */
  @Transactional(readOnly = true)
  public Page<OrderListResponse> getOrdersByCategory(
      Long customerId,
      OrderStageCategory category,
      Pageable pageable) {

    Set<OrderStage> stages = category.getIncludedStages();
    Page<Order> orders = orderRepository.findByCustomerIdAndCurrentStageIn(
        customerId, stages, pageable);

    return orders.map(trackingMapper::toOrderListResponse);
  }

  /**
   * Search orders by order number or product name
   *
   * @param customerId  Customer ID
   * @param searchQuery Search term
   * @param pageable    Pagination parameters
   * @return Page of order list responses
   */
  @Transactional(readOnly = true)
  public Page<OrderListResponse> searchOrders(
      Long customerId,
      String searchQuery,
      Pageable pageable) {

    // Search by order number or product name in items
    Page<Order> orders = orderRepository.findByCustomerIdAndOrderNumberContainingIgnoreCase(
        customerId, searchQuery, pageable);

    return orders.map(trackingMapper::toOrderListResponse);
  }

  /**
   * Confirm order received by customer
   *
   * @param customerId  Customer ID (for security validation)
   * @param orderNumber Order number
   */
  @Transactional
  public void confirmOrderReceived(Long customerId, String orderNumber) {
    Order order = orderRepository.findByOrderNumber(orderNumber)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

    // Validate customer ownership
    if (!order.getCustomer().getId().equals(customerId)) {
      throw new IllegalArgumentException("Order does not belong to customer");
    }

    // Validate can confirm received
    if (order.getCurrentStage() != OrderStage.OUT_FOR_LOCAL_DELIVERY) {
      throw new IllegalStateException(
          "Order cannot be confirmed as received in current stage: " +
              order.getCurrentStage());
    }

    // Update order stage to delivered
    order.complete();
    orderRepository.save(order);

    // Create tracking event
    trackingService.createTrackingEvent(
        order,
        com.commercepal.apiservice.orders.tracking.enums.TrackingEventType.RECEIVED_BY_CUSTOMER,
        null,
        "Customer confirmed receipt of package");

    log.info("Customer confirmed order received: orderId={}, customerId={}",
        order.getId(), customerId);
  }

  // =========================================================================
  // PRIVATE HELPER METHODS
  // =========================================================================

  private Pageable buildPageable(OrderFilterRequest filterRequest) {
    Sort.Direction direction = "asc".equalsIgnoreCase(filterRequest.getDirection())
        ? Sort.Direction.ASC
        : Sort.Direction.DESC;

    Sort sort = Sort.by(direction, filterRequest.getSort());

    return PageRequest.of(
        filterRequest.getPage(),
        filterRequest.getSize(),
        sort);
  }

  private long countOrdersByCategory(Long customerId, OrderStageCategory category) {
    if (category == OrderStageCategory.ALL) {
      return orderRepository.countByCustomerId(customerId);
    }

    Set<OrderStage> stages = category.getIncludedStages();
    return orderRepository.countByCustomerIdAndCurrentStageIn(customerId, stages);
  }
}
