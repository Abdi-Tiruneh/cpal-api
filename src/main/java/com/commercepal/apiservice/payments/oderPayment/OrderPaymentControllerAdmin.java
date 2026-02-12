package com.commercepal.apiservice.payments.oderPayment;

import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentPageRequest;
import com.commercepal.apiservice.payments.oderPayment.dto.OrderPaymentResponse;
import com.commercepal.apiservice.utils.response.PagedResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST controller for order payment operations. Provides administrative APIs for viewing and
 * managing all payment transactions.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@Tag(
    name = "Payment Management (Admin)",
    description = """
        Administrative APIs for payment management and monitoring.
        
        **Features:**
        - View all payment transactions across the system
        - Monitor payment status and history
        - Track payment references and gateway interactions
        
        Requires admin authentication and appropriate permissions.
        """
)
public class OrderPaymentControllerAdmin {

  private final OrderPaymentService orderPaymentService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get order payments (Admin)",
      description = """
          Retrieves order payments with pagination and optional filters.
          Sorted by createdAt descending by default.
          
          **Query Parameters:**
          - `page`: Page number (0-based index), default: 0
          - `size`: Number of records per page, default: 15
          - `paymentReference`: Filter by payment reference (partial match, case-insensitive)
          - `orderReference`: Filter by order number (partial match, case-insensitive)
          - `status`: Filter by payment status (PENDING, SUCCESS, FAILED, CANCELLED)
          - `gateway`: Filter by payment gateway (partial match, case-insensitive)
          - `customerEmail`: Filter by customer email (partial match, case-insensitive)
          - `customerPhoneNumber`: Filter by customer phone number (partial match, case-insensitive)
          - `amount`: Filter by payment amount (searches with +/- 0.5 tolerance, e.g., amount=100 matches 99.5-100.5)
          - `sortBy`: Sort field (createdAt, resolvedAt, amount, reference, status), default: createdAt
          - `direction`: Sort direction (ASC, DESC), default: DESC
          
          **Returns:**
          - Paged payment information including payment details, status, and related order information
          - Payment references, gateway information, and transaction history
          """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Payments retrieved successfully",
          content = @Content(schema = @Schema(implementation = OrderPaymentResponse.class))
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Admin authentication required"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<OrderPaymentResponse>>> getOrderPayments(
      @Valid OrderPaymentPageRequest request) {
    log.info("[PAYMENT_API] GET /payments - page: {}, size: {}", request.page(), request.size());

    Page<OrderPaymentResponse> page = orderPaymentService.getOrderPayments(request);

    log.info("[PAYMENT_API] GET /payments - Success: {} of {} total", page.getNumberOfElements(),
        page.getTotalElements());

    return ResponseWrapper.success(page);
  }

}
