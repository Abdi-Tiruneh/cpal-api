package com.commercepal.apiservice.payments.core;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.model.OrderItem;
import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.oderPayment.OrderPayment;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for processing successful payments.
 * Updates order and order item payment statuses after successful payment confirmation.
 * Each operation runs in its own database transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSuccessPayment {

  private final OrderRepository orderRepository;

  /**
   * Process a successful payment by updating the order and its items.
   * Runs in a new transaction independent of the caller's transaction.
   *
   * @param payment The successful payment to process
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void pickAndProcess(OrderPayment payment) {
    Long orderId = payment.getOrder().getId();
    String reference = payment.getReference();

    log.info("Processing successful payment | ProcessSuccessPayment | pickAndProcess | reference={}, orderId={}",
        reference, orderId);

    try {
      // Fetch fresh order within this transaction
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

      // Update order payment status
      order.markAsPaid(reference, LocalDateTime.now());

      // Transition order to next stage
      if (order.getCurrentStage() == OrderStage.PENDING) {
        order.setCurrentStage(OrderStage.PAYMENT_CONFIRMED);
      }

      orderRepository.save(order);

      log.info("Order updated successfully | ProcessSuccessPayment | pickAndProcess | reference={}, orderId={}, orderNumber={}, newStage={}",
          reference, order.getId(), order.getOrderNumber(), order.getCurrentStage());

      // Update order items in separate transaction
      updateOrderItemsAsPaid(orderId, reference);

    } catch (Exception e) {
      log.error("Failed to process successful payment | ProcessSuccessPayment | pickAndProcess | reference={}, orderId={}, error={}",
          reference, orderId, e.getMessage(), e);
      throw new RuntimeException("Failed to process successful payment: " + e.getMessage(), e);
    }
  }

  /**
   * Update all order items as paid in a separate transaction.
   *
   * @param orderId   The order ID
   * @param reference The payment reference for logging
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateOrderItemsAsPaid(Long orderId, String reference) {
    log.info("Updating order items as paid | ProcessSuccessPayment | updateOrderItemsAsPaid | orderId={}, reference={}",
        orderId, reference);

    try {
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

      int itemCount = 0;
      for (OrderItem item : order.getOrderItems()) {
        item.markAsPaid();
        itemCount++;
      }

      orderRepository.save(order);

      log.info("Order items updated successfully | ProcessSuccessPayment | updateOrderItemsAsPaid | orderId={}, reference={}, itemCount={}",
          orderId, reference, itemCount);

    } catch (Exception e) {
      log.error("Failed to update order items | ProcessSuccessPayment | updateOrderItemsAsPaid | orderId={}, reference={}, error={}",
          orderId, reference, e.getMessage(), e);
      throw new RuntimeException("Failed to update order items: " + e.getMessage(), e);
    }
  }

  /**
   * Process a failed payment by updating the order status.
   * Runs in a new transaction independent of the caller's transaction.
   *
   * @param payment The failed payment to process
   * @param reason  The reason for failure
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processFailedPayment(OrderPayment payment, String reason) {
    Long orderId = payment.getOrder().getId();
    String reference = payment.getReference();

    log.info("Processing failed payment | ProcessSuccessPayment | processFailedPayment | reference={}, orderId={}, reason={}",
        reference, orderId, reason);

    try {
      // Fetch fresh order within this transaction
      Order order = orderRepository.findById(orderId)
          .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

      // Update order payment status
      order.setPaymentStatus(PaymentStatus.FAILED);

      orderRepository.save(order);

      log.info("Failed payment recorded | ProcessSuccessPayment | processFailedPayment | reference={}, orderId={}",
          reference, order.getId());

    } catch (Exception e) {
      log.error("Error recording failed payment | ProcessSuccessPayment | processFailedPayment | reference={}, orderId={}, error={}",
          reference, orderId, e.getMessage(), e);
      throw new RuntimeException("Failed to record failed payment: " + e.getMessage(), e);
    }
  }
}
