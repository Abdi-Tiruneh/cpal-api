package com.commercepal.apiservice.orders.core.customer;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.enums.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CustomerOrderStatusMapper {

  public CustomerOrderStatus map(Order order) {

    // =========================================================================
    // 1. HARD TERMINAL STATES (Order-level truth)
    // =========================================================================

    if (order.getCurrentStage() == OrderStage.CANCELLED) {
      return CustomerOrderStatus.CANCELLED;
    }

    if (order.getCurrentStage() == OrderStage.FAILED) {
      return CustomerOrderStatus.CANCELLED; // customer-safe abstraction
    }

    // Full order refund completed
    if (order.getRefundStatus() == RefundStatus.FULL
        || order.getCurrentStage() == OrderStage.REFUNDED) {
      return CustomerOrderStatus.REFUNDED;
    }

    // =========================================================================
    // 2. PAYMENT STATE
    // =========================================================================

    if (order.getPaymentStatus() == PaymentStatus.PENDING) {
      return CustomerOrderStatus.UNPAID;
    }

    // =========================================================================
    // 3. ITEM-LEVEL AGGREGATION
    // =========================================================================

    var items = order.getOrderItems();

    if (items == null || items.isEmpty()) {
      // Defensive fallback (should never happen in practice)
      return CustomerOrderStatus.PROCESSING;
    }

    // All items delivered → DELIVERED
    boolean allDelivered =
        items.stream()
            .allMatch(i -> i.getCurrentStage() == OrderItemStage.DELIVERED);

    if (allDelivered) {
      return CustomerOrderStatus.DELIVERED;
    }

    // Any item physically shipped / moving → SHIPPED
    boolean anyShipped =
        items.stream()
            .anyMatch(i ->
                i.getCurrentStage() == OrderItemStage.SHIPPED_FROM_SOURCE ||
                i.getCurrentStage() == OrderItemStage.IN_CUSTOMS ||
                i.getCurrentStage() == OrderItemStage.RECEIVED_LOCALLY ||
                i.getCurrentStage() == OrderItemStage.OUT_FOR_DELIVERY
            );

    if (anyShipped) {
      return CustomerOrderStatus.SHIPPED;
    }

    // =========================================================================
    // 4. EVERYTHING ELSE
    // =========================================================================

    return CustomerOrderStatus.PROCESSING;
  }
}
