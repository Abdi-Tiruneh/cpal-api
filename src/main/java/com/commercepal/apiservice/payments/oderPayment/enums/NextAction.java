package com.commercepal.apiservice.payments.oderPayment.enums;

/**
 * Enum representing the next action required for payment processing.
 */
public enum NextAction {

  /**
   * Redirect customer to an external payment page
   */
  REDIRECT_TO_PAYMENT_URL,

  /**
   * Open additional input form (OTP, PIN, etc.)
   */
  OPEN_ADDITIONAL_INPUT,

  /**
   * Retry the same payment attempt
   */
  RETRY_PAYMENT,

  /**
   * Ask user to choose a different payment method
   */
  CHOOSE_ANOTHER_PAYMENT_METHOD,

  /**
   * No further action (payment completed or terminal state)
   */
  NONE
}
