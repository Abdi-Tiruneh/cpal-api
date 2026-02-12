package com.commercepal.apiservice.shared.exceptions.service;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when payment gateway operations fail. Examples: Gateway timeout, gateway
 * communication errors, gateway configuration issues.
 */
public class PaymentGatewayException extends BaseECommerceException {

  private static final String ERROR_CODE = "PAYMENT_GATEWAY_ERROR";
  private static final String DEFAULT_MESSAGE = "Payment gateway operation failed";

  public PaymentGatewayException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PaymentGatewayException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public PaymentGatewayException(String customMessage, Throwable cause) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage, cause);
  }
}

