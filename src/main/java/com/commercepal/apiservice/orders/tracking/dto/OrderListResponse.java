package com.commercepal.apiservice.orders.tracking.dto;

import com.commercepal.apiservice.orders.tracking.enums.OrderStageCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderListResponse
 * <p>
 * Response DTO for order list view (AliExpress-style). Shows order summary with action buttons and
 * product details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

  // ORDER IDENTIFIERS

  /**
   * Unique order number
   */
  private String orderNumber;

  /**
   * When order was placed
   */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime orderDate;

  /**
   * Order ID (for internal use, optional in response)
   */
  private Long orderId;

  // ORDER STATUS

  /**
   * Current order stage (enum value)
   */
  private String currentStage;

  /**
   * Customer-facing stage label Example: "Awaiting delivery", "Delivered"
   */
  private String stageLabel;

  /**
   * High-level stage category for filtering
   */
  private OrderStageCategory stageCategory;

  /**
   * Additional status description Example: "Expected delivery: Nov 22 - Dec 12"
   */
  private String statusDescription;

  // ITEMS

  /**
   * List of items in this order
   */
  @Builder.Default
  private final List<OrderItemSummary> items = new ArrayList<>();

  /**
   * Total number of items
   */
  private Integer totalItemsCount;

  // PRICING

  /**
   * Order subtotal
   */
  private BigDecimal subtotal;

  /**
   * Total amount
   */
  private BigDecimal totalAmount;

  /**
   * Currency code
   */
  private String currency;

  // DELIVERY

  /**
   * Delivery address summary
   */
  private DeliveryAddressSummary deliveryAddress;

  /**
   * Store/provider information
   */
  private String storeName;

  /**
   * Store icon/logo URL
   */
  private String storeIconUrl;

  // ACTION BUTTONS

  /**
   * Whether customer can confirm receipt
   */
  private boolean canConfirmReceived;

  /**
   * Whether order can be tracked
   */
  private boolean canTrack;

  /**
   * Whether order can be cancelled
   */
  private boolean canCancel;

  /**
   * Whether customer can request a return
   */
  private boolean canReturn;

  /**
   * Whether customer can pay now
   */
  private boolean canPay;

  /**
   * Whether customer can write a review
   */
  private boolean canReview;

  // ADDITIONAL INFO

  /**
   * Payment status label
   */
  private String paymentStatus;

  /**
   * Payment status label for display
   */
  private String paymentStatusLabel;

  /**
   * Whether this order has issues/exceptions
   */
  private boolean hasException;

  /**
   * Exception message (if any)
   */
  private String exceptionMessage;
}
