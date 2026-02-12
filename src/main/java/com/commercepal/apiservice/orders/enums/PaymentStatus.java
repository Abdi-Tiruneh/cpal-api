package com.commercepal.apiservice.orders.enums;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum PaymentStatus {
  PENDING(1, "Pending", "Payment not yet made. Awaiting user action."),
  PROCESSING(2, "Processing", "Payment is being processed by the payment provider."),
  SUCCESS(3, "Success", "Payment completed successfully."),
  FAILED(4, "Failed", "Payment failed due to an error or being declined."),
  CANCELLED(5, "Cancelled", "Payment was cancelled by the user or the system."),
  REFUNDED(6, "Refunded", "The full amount was refunded."),
  PARTIALLY_REFUNDED(7, "Partially Refunded", "A portion of the amount was refunded.");

  private final int code;
  private final String label;
  private final String description;

  PaymentStatus(int code, String label, String description) {
    this.code = code;
    this.label = label;
    this.description = description;
  }

  public static PaymentStatus fromCode(int code) {
    return Arrays.stream(values())
        .filter(status -> status.getCode() == code)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid PaymentStatus code: " + code));
  }
}
