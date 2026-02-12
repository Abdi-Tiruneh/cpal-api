package com.commercepal.apiservice.shared.exceptions.transaction;

import com.commercepal.apiservice.shared.exceptions.BaseECommerceException;

/**
 * Exception thrown when payment capture fails.
 */
public class PaymentCaptureFailedException extends BaseECommerceException {

  private static final String ERROR_CODE = "PAYMENT_CAPTURE_FAILED";
  private static final String DEFAULT_MESSAGE = "Payment capture failed";

  public PaymentCaptureFailedException() {
    super(ERROR_CODE, DEFAULT_MESSAGE);
  }

  public PaymentCaptureFailedException(String customMessage) {
    super(ERROR_CODE, DEFAULT_MESSAGE, customMessage);
  }

  public PaymentCaptureFailedException(String paymentId, String reason) {
    super(ERROR_CODE, DEFAULT_MESSAGE,
        String.format("Payment capture failed for payment '%s': %s", paymentId, reason));
  }
}

