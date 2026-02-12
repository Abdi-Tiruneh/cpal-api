package com.commercepal.apiservice.payments.oderPayment;

import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentRetryRequest;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer-facing REST controller for order payment operations. Provides APIs for customers to
 * manage their payment transactions.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(
    name = "Payments",
    description = """
        Customer-facing APIs for payment operations and transaction management.
        
        **Features:**
        - Retry failed or pending payment transactions
        - Change payment provider for retry attempts
        - Receive payment instructions and gateway URLs
        
        **Authentication:**
        - Requires customer authentication
        - Customers can only retry payments for their own orders
        """
)
public class OrderPaymentController {

  private final OrderPaymentService orderPaymentService;

  @PostMapping("/retry")
  @Operation(
      summary = "Retry payment transaction",
      description = """
          Retries a payment transaction using the payment reference.
          Optionally allows changing the payment provider or variant for the retry attempt.
          
          **Validation:**
          - Payment reference must exist and belong to the authenticated customer
          - Order must be in a payable state (not cancelled or completed)
          - Payment must be in a retryable state (typically PENDING or FAILED)
          
          **Optional Parameters:**
          - Payment provider code: If provided, will use this provider for retry instead of the original
          - Payment provider variant code: If provided, will use this variant for retry
          
          **Response:**
          Returns the payment initiation response with updated payment details, gateway URL, and instructions.
          The response includes next action guidance (e.g., RETRY_PAYMENT) if the retry fails.
          
          **Supported Payment Gateways:**
          - AMOLE
          - SAHAY
          - EBIRR, KAAFI_EBIRR, COOPAY_EBIRR
          - CBE-BIRR, CBEBIRR
          - TELEBIRR
          """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Payment retry initiated successfully",
          content = @Content(schema = @Schema(implementation = PaymentInitiationResponse.class))
      ),
      @ApiResponse(responseCode = "400", description = "Bad request - Invalid payment, order, or payment provider"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Customer authentication required"),
      @ApiResponse(responseCode = "404", description = "Payment or order not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponseWrapper<PaymentInitiationResponse>> retryPayment(
      @Valid @RequestBody PaymentRetryRequest request) {
    log.info(
        "[PAYMENT_API] POST /payments/retry - Request received - paymentReference={}, provider={}, variant={}",
        request.paymentReference(), request.paymentProviderCode(),
        request.paymentProviderVariantCode());

    PaymentInitiationResponse payment = orderPaymentService.retryPayment(
        request.paymentReference(),
        request.paymentProviderCode(),
        request.paymentProviderVariantCode());

    log.info("[PAYMENT_API] POST /payments/retry - Success - Payment Reference: {}",
        request.paymentReference());

    return ResponseWrapper.success(payment);
  }
}
