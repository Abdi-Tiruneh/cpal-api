package com.commercepal.apiservice.orders.api;

import com.commercepal.apiservice.orders.core.dto.AdminOrderPageRequestDto;
import com.commercepal.apiservice.orders.core.dto.AdminOrderResponse;
import com.commercepal.apiservice.orders.core.service.AdminOrderService;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST controller for order management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'ORDER_MANAGER')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
    name = "Order Management (Admin)",
    description = """
        Comprehensive administrative APIs for order management and monitoring.
        
        **Features:**
        - List all orders with advanced filtering and sorting
        - Get order details by ID or order number
        - Filter by order stage, payment status, customer, dates, amounts
        - Multi-level sorting and pagination
        
        Requires ADMIN, SUPER_ADMIN, or ORDER_MANAGER role.
        """
)
public class AdminOrderController {

  private final AdminOrderService adminOrderService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get all orders (Admin)",
      description = """
          Retrieves a paginated list of all orders with comprehensive filtering and sorting capabilities.
          
          **Supported Filters:**
          - Customer: customerId, searchQuery (name/email/phone/order number)
          - Order Status: currentStage, paymentStatus, refundStatus
          - Order Details: priority, platform, currency
          - Agent: agentId, isAgentInitiated
          - Dates: orderedAfter/Before, completedAfter/Before
          - Amounts: minAmount, maxAmount
          
          **Sorting:**
          - Sort by any order field (e.g., orderedAt, totalAmount, currentStage)
          - Sort direction: ASC or DESC
          - Default: orderedAt DESC (latest first)
          
          **Pagination:**
          - Page number (0-based index)
          - Page size (default: 20)
          """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Orders retrieved successfully",
          content = @Content(schema = @Schema(implementation = AdminOrderResponse.class))
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<AdminOrderResponse>>> getOrders(
      @Valid AdminOrderPageRequestDto requestDto) {
    log.info(
        "[ADMIN-ORDER-API] GET /admin/orders - Request received - page: {}, size: {}, filters: {}",
        requestDto.page(), requestDto.size(), requestDto.searchQuery());

    Page<AdminOrderResponse> ordersPage = adminOrderService.getOrders(requestDto);

    log.info("[ADMIN-ORDER-API] GET /admin/orders - Success: {} orders found (page {} of {})",
        ordersPage.getNumberOfElements(),
        ordersPage.getNumber() + 1,
        ordersPage.getTotalPages());

    return ResponseWrapper.success(ordersPage);
  }

  @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get order by ID (Admin)",
      description = """
          Retrieves detailed order information by order ID.
          Returns comprehensive order details including customer information, items, financials, and status.
          """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Order retrieved successfully",
          content = @Content(schema = @Schema(implementation = AdminOrderResponse.class))
      ),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponseWrapper<AdminOrderResponse>> getOrderById(
      @Parameter(description = "Order ID", example = "123", required = true)
      @PathVariable Long orderId) {
    log.info("[ADMIN-ORDER-API] GET /admin/orders/{} - Request received", orderId);

    AdminOrderResponse order = adminOrderService.getOrderById(orderId);

    log.info("[ADMIN-ORDER-API] GET /admin/orders/{} - Success", orderId);

    return ResponseWrapper.success(order);
  }

  @GetMapping(value = "/order-number/{orderNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get order by order number (Admin)",
      description = """
          Retrieves detailed order information by order number (order reference).
          Returns comprehensive order details including customer information, items, financials, and status.
          """
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Order retrieved successfully",
          content = @Content(schema = @Schema(implementation = AdminOrderResponse.class))
      ),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ResponseWrapper<AdminOrderResponse>> getOrderByOrderNumber(
      @Parameter(description = "Order number/reference", example = "ORD-20240101-ABC12345", required = true)
      @PathVariable String orderNumber) {
    log.info("[ADMIN-ORDER-API] GET /admin/orders/order-number/{} - Request received", orderNumber);

    AdminOrderResponse order = adminOrderService.getOrderByOrderNumber(orderNumber);

    log.info("[ADMIN-ORDER-API] GET /admin/orders/order-number/{} - Success", orderNumber);

    return ResponseWrapper.success(order);
  }
}
