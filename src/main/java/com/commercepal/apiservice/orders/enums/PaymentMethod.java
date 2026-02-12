package com.commercepal.apiservice.orders.enums;

/**
 * PaymentMethod Enum
 * <p>
 * Represents the payment method used for an order.
 */
public enum PaymentMethod {

  /**
   * Credit or debit card payment
   */
  CARD,

  /**
   * Mobile money (e.g., M-Pesa, Telebirr)
   */
  MOBILE_MONEY,

  /**
   * Bank transfer
   */
  BANK_TRANSFER,

  /**
   * Cash on delivery
   */
  CASH_ON_DELIVERY,

  /**
   * Digital wallet (e.g., PayPal, Stripe)
   */
  DIGITAL_WALLET,

  /**
   * CommercePal wallet/account balance
   */
  COMMERCEPAL_WALLET,

  /**
   * Telebirr (Ethiopian mobile payment)
   */
  TELEBIRR,

  /**
   * CBE Birr (Commercial Bank of Ethiopia)
   */
  CBE_BIRR,

  /**
   * M-Pesa (Kenyan mobile payment)
   */
  MPESA,

  /**
   * PayPal
   */
  PAYPAL,

  /**
   * Stripe
   */
  STRIPE,

  /**
   * Other payment method
   */
  OTHER;

  /**
   * Parse from string (case-insensitive)
   */
  public static PaymentMethod fromString(String method) {
    if (method == null || method.trim().isEmpty()) {
      return OTHER;
    }

    try {
      return PaymentMethod.valueOf(method.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return OTHER;
    }
  }

  /**
   * Check if this is a mobile payment method
   */
  public boolean isMobilePayment() {
    return this == MOBILE_MONEY || this == TELEBIRR || this == CBE_BIRR || this == MPESA;
  }

  /**
   * Check if this is a digital/online payment method
   */
  public boolean isDigitalPayment() {
    return this == CARD || this == DIGITAL_WALLET || this == PAYPAL ||
        this == STRIPE || this == COMMERCEPAL_WALLET || isMobilePayment();
  }
}
