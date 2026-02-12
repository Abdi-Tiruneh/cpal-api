package com.commercepal.apiservice.payments.oderPayment;

import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting OrderPayment entities to DTOs.
 */
@Component
public class OrderPaymentMapper {

  /**
   * Maps OrderPayment entity to OrderPaymentResponse DTO.
   */
  public OrderPaymentResponse toResponse(OrderPayment payment) {
    var order = payment.getOrder();
    var customer = payment.getCustomer();
    var credential = customer != null ? customer.getCredential() : null;
    String fullName = customer != null
        ? (customer.getFirstName() + " " + (customer.getLastName() != null ? customer.getLastName()
        : "")).trim()
        : null;
    return OrderPaymentResponse.builder()
        .reference(payment.getReference())
        .gatewayReference(payment.getGatewayReference())
        .gateway(payment.getGateway())
        .accountNumber(payment.getAccountNumber())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .status(payment.getStatus())
        .orderNumber(order != null ? order.getOrderNumber() : null)
        .customerFullName(fullName)
        .customerEmail(credential != null ? credential.getEmailAddress() : null)
        .customerPhone(credential != null ? credential.getPhoneNumber() : null)
        .createdAt(payment.getCreatedAt())
        .initRequestedAt(payment.getInitRequestedAt())
        .resolvedAt(payment.getResolvedAt())
        .build();
  }
}
