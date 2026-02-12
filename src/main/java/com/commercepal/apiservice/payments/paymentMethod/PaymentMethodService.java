package com.commercepal.apiservice.payments.paymentMethod;

import com.commercepal.apiservice.payments.paymentMethod.dto.PaymentMethodResponse;
import java.util.List;

/**
 * Service interface for payment method operations.
 */
public interface PaymentMethodService {

  /**
   * Retrieves all active payment methods with their items and variants.
   *
   * @return list of active payment methods
   */
  List<PaymentMethodResponse> getAllPaymentMethods();
}
