package com.commercepal.apiservice.orders.core.service;

import com.commercepal.apiservice.orders.core.dto.AdminOrderPageRequestDto;
import com.commercepal.apiservice.orders.core.dto.AdminOrderResponse;
import org.springframework.data.domain.Page;

/**
 * Service interface for admin order management operations.
 */
public interface AdminOrderService {

  /**
   * Get paginated list of orders for admin with advanced filtering and sorting.
   *
   * @param requestDto the page request containing pagination, sorting, and filter criteria
   * @return paginated list of admin order responses
   */
  Page<AdminOrderResponse> getOrders(AdminOrderPageRequestDto requestDto);

  /**
   * Get order details by order ID for admin.
   *
   * @param orderId the order ID
   * @return admin order response
   */
  AdminOrderResponse getOrderById(Long orderId);

  /**
   * Get order details by order number for admin.
   *
   * @param orderNumber the order number
   * @return admin order response
   */
  AdminOrderResponse getOrderByOrderNumber(String orderNumber);
}
