package com.commercepal.apiservice.orders.tracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderTrackingResponse
 * <p>
 * Detailed tracking timeline response for a specific order. Matches AliExpress tracking detail view
 * with timeline, delivery window, and tracking numbers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {

  // ORDER INFO

  /**
   * Order number
   */
  private String orderNumber;

  /**
   * When order was placed
   */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime orderDate;

  /**
   * Current order stage
   */
  private String currentStage;

  /**
   * Current stage label for display
   */
  private String currentStageLabel;

  // DELIVERY WINDOW

  /**
   * Estimated delivery start date
   */
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate estimatedDeliveryStart;

  /**
   * Estimated delivery end date
   */
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate estimatedDeliveryEnd;

  /**
   * Formatted delivery window for display Example: "Nov. 22 - Dec. 12"
   */
  private String deliveryWindow;

  // SHIPPING INFO

  /**
   * Shipping method description Example: "AliExpress Selection Standard"
   */
  private String shippingMethod;

  /**
   * Provider's tracking number Example: "RR09330280SAE"
   */
  private String providerTrackingNumber;

  /**
   * Local/internal tracking number Example: "ET-2024-12345"
   */
  private String localTrackingNumber;

  /**
   * Main carrier name Example: "DHL", "FedEx"
   */
  private String carrierName;

  // TRACKING TIMELINE

  /**
   * List of tracking events in reverse chronological order (newest first)
   */
  @Builder.Default
  private final List<TrackingEventDto> trackingEvents = new ArrayList<>();

  /**
   * Total number of tracking events
   */
  private Integer totalEvents;

  // DELIVERY ADDRESS

  /**
   * Delivery address details
   */
  private DeliveryAddressSummary deliveryAddress;

  // ITEMS INFO (optional, for multi-item orders)

  /**
   * List of items in this order
   */
  @Builder.Default
  private final List<OrderItemSummary> items = new ArrayList<>();

  // ADDITIONAL INFO

  /**
   * Whether customer can confirm receipt
   */
  private boolean canConfirmReceived;

  /**
   * Whether tracking can be refreshed from provider
   */
  private boolean canRefreshTracking;

  /**
   * Last time tracking was updated
   */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime lastTrackingUpdate;

  /**
   * Whether order has any exceptions/issues
   */
  private boolean hasException;

  /**
   * Current exception message (if any)
   */
  private String exceptionMessage;
}
