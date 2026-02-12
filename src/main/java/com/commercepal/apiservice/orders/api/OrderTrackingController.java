package com.commercepal.apiservice.orders.api;

import com.commercepal.apiservice.orders.tracking.dto.OrderFilterRequest;
import com.commercepal.apiservice.orders.tracking.dto.OrderListResponse;
import com.commercepal.apiservice.orders.tracking.dto.OrderStageStatistics;
import com.commercepal.apiservice.orders.tracking.dto.OrderTrackingResponse;
import com.commercepal.apiservice.orders.tracking.enums.OrderStageCategory;
import com.commercepal.apiservice.orders.tracking.service.OrderQueryService;
import com.commercepal.apiservice.orders.tracking.service.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OrderTrackingController
 * <p>
 * Customer-facing REST API for order tracking and management. Provides endpoints for viewing
 * orders, tracking timeline, and confirming receipt.
 * <p>
 * Endpoints match AliExpress-style order management: - List orders with filters (To pay, To ship,
 * Shipped, Processed) - View detailed tracking timeline - Confirm order received - Get order
 * statistics
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders/cv")
@RequiredArgsConstructor
@Tag(name = "Order Tracking", description = "Customer order tracking and management API")
public class OrderTrackingController {

  private final OrderQueryService orderQueryService;

  /**
   * Get list of customer orders with filtering and pagination
   * <p>
   * Supports filtering by order stage category: - ALL: All orders - TO_PAY: Orders awaiting payment
   * - TO_SHIP: Orders paid but not shipped - SHIPPED: Orders in transit - PROCESSED: Completed
   * orders - CANCELLED: Cancelled/failed orders
   *
   * @param customerId    Customer ID (from security context/token)
   * @param filterRequest Filter and pagination parameters
   * @return Page of order list responses
   */
  @GetMapping
  @Operation(summary = "List customer orders", description = "Get paginated list of orders with optional filtering by stage category and search")
  public ResponseEntity<Page<OrderListResponse>> getOrders(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId,
      @ParameterObject OrderFilterRequest filterRequest) {

    log.debug("Getting orders for customer: {}, filters: {}", customerId, filterRequest);

    Page<OrderListResponse> orders = orderQueryService.getCustomerOrders(customerId,
        filterRequest);

    return ResponseEntity.ok(orders);
  }

  /**
   * Get order statistics (counts by stage category) Used for displaying badge counts on tabs
   *
   * @param customerId Customer ID
   * @return Order statistics
   */
  @GetMapping("/statistics")
  @Operation(summary = "Get order statistics", description = "Get count of orders in each stage category for tab badges")
  public ResponseEntity<OrderStageStatistics> getOrderStatistics(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId) {

    log.debug("Getting order statistics for customer: {}", customerId);

    OrderStageStatistics statistics = orderQueryService.getOrderStatistics(customerId);

    return ResponseEntity.ok(statistics);
  }

  /**
   * Get orders by specific stage category
   *
   * @param customerId Customer ID
   * @param category   Stage category
   * @param page       Page number (0-indexed)
   * @param size       Page size
   * @return Page of order list responses
   */
  @GetMapping("/category/{category}")
  @Operation(summary = "Get orders by category", description = "Get orders filtered by specific stage category")
  public ResponseEntity<Page<OrderListResponse>> getOrdersByCategory(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId,
      @Parameter(description = "Order stage category", required = true) @PathVariable OrderStageCategory category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.debug("Getting orders for customer: {}, category: {}", customerId, category);

    OrderFilterRequest filterRequest = OrderFilterRequest.builder()
        .stageCategory(category)
        .page(page)
        .size(size)
        .build();

    Page<OrderListResponse> orders = orderQueryService.getCustomerOrders(customerId,
        filterRequest);

    return ResponseEntity.ok(orders);
  }

  /**
   * Get order details by order number
   *
   * @param customerId  Customer ID (for authorization)
   * @param orderNumber Order number
   * @return Order details
   */
  @GetMapping("/{orderNumber}")
  @Operation(summary = "Get order details", description = "Get detailed information for a specific order")
  public ResponseEntity<OrderListResponse> getOrderDetails(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId,
      @Parameter(description = "Order number", required = true) @PathVariable String orderNumber) {

    log.debug("Getting order details: customerId={}, orderNumber={}",
        customerId, orderNumber);

    OrderListResponse order = orderQueryService.getOrderDetails(customerId, orderNumber);

    return ResponseEntity.ok(order);
  }

  /**
   * Get order tracking timeline Shows complete tracking history with events in reverse
   * chronological order
   *
   * @param customerId  Customer ID (for authorization)
   * @param orderNumber Order number
   * @return Order tracking response with timeline
   */
  @GetMapping("/{orderNumber}/tracking")
  @Operation(summary = "Get order tracking timeline", description = "Get detailed tracking timeline with all tracking events for an order")
  public ResponseEntity<OrderTrackingResponse> getOrderTracking(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId,
      @Parameter(description = "Order number", required = true) @PathVariable String orderNumber) {

    log.debug("Getting tracking for order: customerId={}, orderNumber={}",
        customerId, orderNumber);

    OrderTrackingResponse tracking = orderQueryService.getOrderTracking(customerId, orderNumber);

    return ResponseEntity.ok(tracking);
  }

  /**
   * Confirm order received by customer Updates order stage to DELIVERED and creates tracking event
   *
   * @param customerId  Customer ID (for authorization)
   * @param orderNumber Order number
   * @return Success response
   */
  @PostMapping("/{orderNumber}/confirm-received")
  @Operation(summary = "Confirm order received", description = "Customer confirms receipt of order, updates status to DELIVERED")
  public ResponseEntity<Void> confirmOrderReceived(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId,
      @Parameter(description = "Order number", required = true) @PathVariable String orderNumber) {

    log.info("Customer confirming order received: customerId={}, orderNumber={}",
        customerId, orderNumber);

    orderQueryService.confirmOrderReceived(customerId, orderNumber);

    return ResponseEntity.ok().build();
  }

  /**
   * Refresh tracking from external provider Fetches latest tracking information from provider API
   * (AliExpress, Amazon, etc.)
   *
   * @param customerId  Customer ID (for authorization)
   * @param orderNumber Order number
   * @return Success response
   */
  @PostMapping("/{orderNumber}/tracking/refresh")
  @Operation(summary = "Refresh provider tracking", description = "Fetch latest tracking information from external provider")
  public ResponseEntity<Void> refreshProviderTracking(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId,
      @Parameter(description = "Order number", required = true) @PathVariable String orderNumber) {

    log.info("Refreshing provider tracking: customerId={}, orderNumber={}",
        customerId, orderNumber);

    // Get order to validate customer ownership
    OrderListResponse order = orderQueryService.getOrderDetails(customerId, orderNumber);

    // Refresh tracking (this will integrate with provider APIs in the future)
    // For now it just creates a tracking update event
    // orderTrackingService.refreshProviderTracking(order);

    return ResponseEntity.ok().build();
  }

  /**
   * Search orders by order number or product name
   *
   * @param customerId Customer ID
   * @param query      Search query
   * @param page       Page number
   * @param size       Page size
   * @return Page of matching orders
   */
  @GetMapping("/search")
  @Operation(summary = "Search orders", description = "Search orders by order number or product name")
  public ResponseEntity<Page<OrderListResponse>> searchOrders(
      @Parameter(description = "Customer ID", required = true) @RequestParam Long customerId,
      @Parameter(description = "Search query", required = true) @RequestParam String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.debug("Searching orders: customerId={}, query={}", customerId, query);

    OrderFilterRequest filterRequest = OrderFilterRequest.builder()
        .searchQuery(query)
        .page(page)
        .size(size)
        .build();

    Page<OrderListResponse> orders = orderQueryService.getCustomerOrders(customerId,
        filterRequest);

    return ResponseEntity.ok(orders);
  }
}
