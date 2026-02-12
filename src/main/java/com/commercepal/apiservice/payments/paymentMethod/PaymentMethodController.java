package com.commercepal.apiservice.payments.paymentMethod;

import com.commercepal.apiservice.payments.paymentMethod.dto.PaymentMethodResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment method operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
@Tag(
    name = "Payment Methods",
    description = "APIs for retrieving available payment methods, items, and variants"
)
public class PaymentMethodController {

  private final PaymentMethodService paymentMethodService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get all payment methods",
      description = """
          Retrieves all active payment methods with their associated items and variants.
          Returns a hierarchical structure: Payment Methods -> Items -> Variants.
          Only active payment methods are returned.
          """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Payment methods retrieved successfully",
          content = @Content(schema = @Schema(implementation = PaymentMethodResponse.class))
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponseWrapper<List<PaymentMethodResponse>>> getAllPaymentMethods() {
    log.info("[PAYMENT_METHOD_API] GET /payment-methods - Request received");

    List<PaymentMethodResponse> paymentMethods = paymentMethodService.getAllPaymentMethods();

    log.info("[PAYMENT_METHOD_API] GET /payment-methods - Success: {} payment methods found",
        paymentMethods.size());

    return ResponseWrapper.success(paymentMethods);
  }
}
