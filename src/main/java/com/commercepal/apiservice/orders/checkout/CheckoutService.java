package com.commercepal.apiservice.orders.checkout;

import com.commercepal.apiservice.orders.checkout.dto.CheckoutRequest;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutResult;
import com.commercepal.apiservice.users.customer.Customer;

/**
 * Service interface for checkout operations.
 * <p>
 * Handles the complete checkout process including order creation,
 * validation, and payment initialization.
 *
 * @author CommercePal
 * @version 1.0
 */
public interface CheckoutService {

  /**
   * Process checkout request and create a new order.
   * <p>
   * This method handles the complete checkout flow:
   * 1. Validates delivery address ownership
   * 2. Creates order with customer and address information
   * 3. Processes each item (fetches details, validates, creates order items)
   * 4. Calculates financial totals
   * 5. Persists the order
   * 6. Initializes payment
   * 7. Links cart if applicable
   *
   * @param request  Customer's checkout request containing items and delivery details
   * @param customer Authenticated customer making the purchase
   * @return CheckoutResult containing the created order and payment information
   * @throws com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException if delivery address is not found
   * @throws com.commercepal.apiservice.shared.exceptions.business.BadRequestException       if validation fails
   */
  CheckoutResult checkout(CheckoutRequest request, Customer customer);
}
