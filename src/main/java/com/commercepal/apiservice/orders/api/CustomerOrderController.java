package com.commercepal.apiservice.orders.api;

import com.commercepal.apiservice.orders.checkout.CheckoutService;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutRequest;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutResponse;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutResult;
import com.commercepal.apiservice.orders.checkout.mapper.CheckoutResponseMapper;
import com.commercepal.apiservice.orders.core.service.CustomerOrderService;
import com.commercepal.apiservice.orders.core.customer.dto.CustomerOrderDetailsDto;
import com.commercepal.apiservice.orders.core.customer.dto.CustomerOrderListDto;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.CurrentUserService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Customer order management APIs")

public class CustomerOrderController {

  private final CustomerOrderService customerOrderService;
  private final CheckoutService checkoutService;
  private final CurrentUserService currentUserService;
  private final CheckoutResponseMapper responseMapper;

  /**
   * Initiates the checkout process for customer orders.
   *
   * @param request Checkout request containing items, delivery address, and currency
   * @return Complete checkout response with order details ready for payment
   */
  @PostMapping("/checkout")
  @Operation(
      summary = "Process Checkout",
      description = "Validates checkout request and creates a new order with payment initialization",
      security = @SecurityRequirement(name = "Bearer Authentication")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Checkout processed successfully", content = @Content(schema = @Schema(implementation = CheckoutResponse.class))),
      @ApiResponse(responseCode = "400", description = "Bad Request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Not Found"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error")
  })
  public ResponseEntity<ResponseWrapper<CheckoutResponse>> checkout(
      @Valid @RequestBody CheckoutRequest request) {
    log.info("Checkout API called | CheckoutController | checkout | channel={}, currency={}, itemCount={}",
        request.channel(), request.currency(), request.items().size());

    // Get authenticated customer
    Customer customer = currentUserService.getCurrentCustomer();
    log.debug("Processing checkout | CheckoutController | checkout | customerId={}", customer.getId());

    // Process checkout
    CheckoutResult result = checkoutService.checkout(request, customer);
    log.info("Checkout processed | CheckoutController | checkout | orderNumber={}", result.order().getOrderNumber());

    // Map to response DTO
    CheckoutResponse response = responseMapper.toCheckoutResponse(result);

    log.info("Checkout API completed | CheckoutController | checkout | orderNumber={}", result.order().getOrderNumber());
    return ResponseWrapper.success(
        "Checkout processed successfully. Order is ready for payment.",
        response);
  }


  @GetMapping
  @Operation(
      summary = "Get My Orders",
      description = "Retrieves a paginated list of orders for the authenticated customer",
      security = @SecurityRequirement(name = "Bearer Authentication")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(schema = @Schema(implementation = CustomerOrderListDto.class))),
      @ApiResponse(responseCode = "400", description = "Bad Request"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<CustomerOrderListDto>>> myOrders(
      @Parameter(description = "Pagination parameters", example = "page=0&size=20") Pageable pageable
  ) {
    return ResponseWrapper.success(customerOrderService.getMyOrders(16997L, pageable));
  }

  @GetMapping("/{orderNumber}")
  @Operation(
      summary = "Get Order Details",
      description = "Retrieves detailed information for a specific order by order number",
      security = @SecurityRequirement(name = "Bearer Authentication")
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order details retrieved successfully", content = @Content(schema = @Schema(implementation = CustomerOrderDetailsDto.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error")
  })
  public ResponseEntity<ResponseWrapper<CustomerOrderDetailsDto>> orderDetails(
      @Parameter(description = "Order number", example = "ORD-20240101-ABC12345", required = true)
      @PathVariable String orderNumber
  ) {
    return ResponseWrapper.success(customerOrderService.getOrderDetails(16997L, orderNumber));
  }
}
