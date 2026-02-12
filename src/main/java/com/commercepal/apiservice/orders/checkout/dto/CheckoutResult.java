package com.commercepal.apiservice.orders.checkout.dto;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;

/**
 * Result of checkout containing the order and payment information.
 */
public record CheckoutResult(
    Order order,
    PaymentInitiationResponse payment) {

}
