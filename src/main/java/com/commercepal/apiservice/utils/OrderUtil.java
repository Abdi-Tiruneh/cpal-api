package com.commercepal.apiservice.utils;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.model.OrderItem;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for order-related calculations and operations.
 * <p>
 * Provides static helper methods for order calculations that don't require instance state.
 */
@Slf4j
public final class OrderUtil {

  // Order number generation constants
  private static final String ORDER_NUMBER_PREFIX = "ORD";
  // Alphanumeric characters excluding confusing ones: 0, O, I, L, 1
  private static final String UNAMBIGUOUS_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
  private static final int ORDER_ID_LENGTH = 8;
  private static final int MAX_ORDER_NUMBER_GENERATION_ATTEMPTS = 5;

  private OrderUtil() {
    // Prevent instantiation
  }

  /**
   * Calculates the total amount for an order based on its components. Formula: subtotal - discount
   * + tax + delivery + additional charges
   *
   * @param subtotal          the order subtotal
   * @param discountAmount    the discount amount
   * @param taxAmount         the tax amount
   * @param deliveryFee       the delivery fee
   * @param additionalCharges the additional charges
   * @return the calculated total amount
   */
  public static BigDecimal calculateTotalAmount(
      BigDecimal subtotal,
      BigDecimal discountAmount,
      BigDecimal taxAmount,
      BigDecimal deliveryFee,
      BigDecimal additionalCharges) {
    if (subtotal == null) {
      subtotal = BigDecimal.ZERO;
    }
    if (discountAmount == null) {
      discountAmount = BigDecimal.ZERO;
    }
    if (taxAmount == null) {
      taxAmount = BigDecimal.ZERO;
    }
    if (deliveryFee == null) {
      deliveryFee = BigDecimal.ZERO;
    }
    if (additionalCharges == null) {
      additionalCharges = BigDecimal.ZERO;
    }

    return subtotal
        .subtract(discountAmount)
        .add(taxAmount)
        .add(deliveryFee)
        .add(additionalCharges);
  }

  /**
   * Recalculates and updates the total amount for an order. Uses the order's current subtotal,
   * discount, tax, delivery fee, and additional charges.
   *
   * @param order the order to recalculate
   */
  public static void recalculateTotalAmount(Order order) {
    BigDecimal totalAmount = calculateTotalAmount(
        order.getSubtotal(),
        order.getDiscountAmount(),
        order.getTaxAmount(),
        order.getDeliveryFee(),
        order.getAdditionalCharges());

    order.setTotalAmount(totalAmount);
  }

  /**
   * Calculates order totals from order items and updates the order.
   * <p>
   * Calculates: - Subtotal: sum of all item subtotals - Tax Amount: sum of all item tax amounts -
   * Delivery Fee: sum of all item delivery fees - Total Amount: sum of all item total amounts
   *
   * @param order the order to calculate totals for
   */
  public static void calculateOrderTotals(Order order) {
    log.debug("Calculating order totals for order: {}", order.getOrderNumber());

    BigDecimal subtotal = order.getOrderItems().stream()
        .map(OrderItem::getSubtotal)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal taxAmount = order.getOrderItems().stream()
        .map(OrderItem::getTaxAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal deliveryFee = order.getOrderItems().stream()
        .map(OrderItem::getDeliveryFee)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalAmount = order.getOrderItems().stream()
        .map(OrderItem::getTotalAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    order.setSubtotal(subtotal);
    order.setTaxAmount(taxAmount);
    order.setDeliveryFee(deliveryFee);
    order.setTotalAmount(totalAmount);

    log.info("Order totals calculated - Subtotal: {}, Tax: {}, Delivery: {}, Total: {}",
        subtotal, taxAmount, deliveryFee, totalAmount);
  }

  /**
   * Generates a unique order number without confusing characters. Format: ORD-YYYYMMDD-XXXXXXXX
   * Where XXXXXXXX is 8 unambiguous alphanumeric characters.
   *
   * @param orderNumberExists predicate to check if an order number already exists
   * @return Unique order number
   */
  public static String generateOrderNumber(Predicate<String> orderNumberExists) {
    String orderNumber = null;
    int attempts = 0;

    while (attempts < MAX_ORDER_NUMBER_GENERATION_ATTEMPTS) {
      // Generate candidate order number
      String datePart = java.time.LocalDate.now().format(
          java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
      String randomPart = generateUnambiguousId(ORDER_ID_LENGTH);
      orderNumber = ORDER_NUMBER_PREFIX + "-" + datePart + "-" + randomPart;

      // Check uniqueness
      if (!orderNumberExists.test(orderNumber)) {
        log.debug("Generated unique order number: {} (attempt {})", orderNumber, attempts + 1);
        return orderNumber;
      }

      attempts++;
      log.warn("Order number collision detected, retrying... (attempt {})", attempts);
    }

    // Fallback with timestamp if max attempts reached
    log.error("Could not generate unique order number after {} attempts, using timestamp fallback",
        MAX_ORDER_NUMBER_GENERATION_ATTEMPTS);
    return ORDER_NUMBER_PREFIX + "-" + System.currentTimeMillis() + "-" +
        generateUnambiguousId(ORDER_ID_LENGTH);
  }

  /**
   * Generates a random string using unambiguous characters. Excludes: 0, O, I, L, 1 to avoid
   * confusion.
   *
   * @param length Length of the ID to generate
   * @return Random unambiguous ID
   */
  public static String generateUnambiguousId(int length) {
    java.util.Random random = new java.util.Random();
    StringBuilder id = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(UNAMBIGUOUS_CHARS.length());
      id.append(UNAMBIGUOUS_CHARS.charAt(index));
    }

    return id.toString();
  }

  /**
   * Generates a sub-order number in the format: orderNumber_NN
   *
   * @param orderNumber Parent order number
   * @param itemNumber  Sequential item number (1-based)
   * @return Sub-order number
   */
  public static String generateSubOrderNumber(String orderNumber, int itemNumber) {
    String subOrderNumber = String.format("%s_%02d", orderNumber, itemNumber);
    log.debug("Generated sub-order number: {}", subOrderNumber);
    return subOrderNumber;
  }
}
