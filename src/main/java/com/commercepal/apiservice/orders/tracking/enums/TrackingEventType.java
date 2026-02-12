package com.commercepal.apiservice.orders.tracking.enums;

import lombok.Getter;

/**
 * TrackingEventType Enum
 * <p>
 * Comprehensive tracking event types for order tracking timeline. Inspired by AliExpress tracking
 * system, tailored for Ethiopia-based dropshipping.
 * <p>
 * Categories: - ORDER: Order placement and verification events - PROVIDER: External provider
 * (AliExpress, Amazon, etc.) events - INTERNATIONAL_SHIPPING: International transit events -
 * CUSTOMS: Ethiopian customs clearance events - LOCAL_DELIVERY: Local delivery within Ethiopia -
 * EXCEPTION: Delays, holds, cancellations - COMPLETION: Final delivery or refund events
 */
@Getter
public enum TrackingEventType {

  // ORDER EVENTS (Initial order processing)

  ORDER_CREATED(
      "ORDER",
      "Your order has been successfully created",
      "Order placed on CommercePal platform",
      "order_created",
      true),

  PAYMENT_CONFIRMED(
      "ORDER",
      "Payment confirmed",
      "Your payment has been received and verified",
      "payment_confirmed",
      true),

  ORDER_VERIFIED(
      "ORDER",
      "Order verified",
      "Order details and delivery address verified",
      "order_verified",
      true),

  ORDER_PROCESSING(
      "ORDER",
      "Order is being processed",
      "Preparing to place order on external provider",
      "order_processing",
      true),

  // PROVIDER EVENTS (External provider order placement)

  ORDERED_ON_PROVIDER(
      "PROVIDER",
      "Order placed on provider",
      "Order placed on external provider (AliExpress, Amazon, etc.)",
      "ordered_on_provider",
      true),

  PROVIDER_CONFIRMED(
      "PROVIDER",
      "Provider confirmed order",
      "External provider confirmed the order",
      "provider_confirmed",
      true),

  PROVIDER_PROCESSING(
      "PROVIDER",
      "Your order is being prepared",
      "Provider is preparing your package",
      "provider_processing",
      true),

  READY_TO_SHIP(
      "PROVIDER",
      "Package ready to be shipped",
      "Your package is ready for shipment",
      "ready_to_ship",
      true),

  // INTERNATIONAL SHIPPING EVENTS

  SHIPPED_FROM_PROVIDER(
      "INTERNATIONAL_SHIPPING",
      "Package shipped",
      "Package left warehouse and shipped internationally",
      "shipped_from_provider",
      true),

  DEPARTED_ORIGIN(
      "INTERNATIONAL_SHIPPING",
      "Package leaving origin country/region",
      "Package departed from origin country",
      "departed_origin",
      true),

  IN_INTERNATIONAL_TRANSIT(
      "INTERNATIONAL_SHIPPING",
      "In transit",
      "Your package is in international transit",
      "in_international_transit",
      true),

  ARRIVED_TRANSIT_HUB(
      "INTERNATIONAL_SHIPPING",
      "Package arrived at transit country/region",
      "Package arrived at intermediate transit hub",
      "arrived_transit_hub",
      true),

  LEFT_TRANSIT_HUB(
      "INTERNATIONAL_SHIPPING",
      "Package left transit country/region",
      "Package departed from transit hub",
      "left_transit_hub",
      true),

  AWAITING_FLIGHT(
      "INTERNATIONAL_SHIPPING",
      "Awaiting flight",
      "Your package is waiting for the next flight",
      "awaiting_flight",
      true),

  ARRIVED_AT_AIRPORT(
      "INTERNATIONAL_SHIPPING",
      "Your package arrived at local airport",
      "Package arrived at destination airport",
      "arrived_at_airport",
      true),

  // ETHIOPIAN CUSTOMS EVENTS

  ARRIVED_IN_ETHIOPIA(
      "CUSTOMS",
      "Arrived in Ethiopia",
      "Package arrived in Ethiopia",
      "arrived_in_ethiopia",
      true),

  CUSTOMS_CLEARANCE_STARTED(
      "CUSTOMS",
      "Import customs clearance started",
      "Ethiopian customs clearance process started",
      "customs_clearance_started",
      true),

  CUSTOMS_IN_PROGRESS(
      "CUSTOMS",
      "Customs clearance in progress",
      "Your package is being processed by Ethiopian customs",
      "customs_in_progress",
      true),

  CUSTOMS_CLEARED(
      "CUSTOMS",
      "Export customs clearance complete",
      "Successfully cleared Ethiopian customs",
      "customs_cleared",
      true),

  CUSTOMS_HELD(
      "CUSTOMS",
      "Customs hold",
      "Package temporarily held by customs (additional documentation may be required)",
      "customs_held",
      true),

  CUSTOMS_RELEASED(
      "CUSTOMS",
      "Released from customs",
      "Package released from customs hold",
      "customs_released",
      true),

  // LOCAL DELIVERY EVENTS (Within Ethiopia)

  RECEIVED_AT_SORTING_CENTER(
      "LOCAL_DELIVERY",
      "Package received by sorting center of origin",
      "Package arrived at local sorting center",
      "received_at_sorting_center",
      true),

  LEFT_SORTING_CENTER(
      "LOCAL_DELIVERY",
      "Package left sorting center of origin",
      "Package departed from sorting center",
      "left_sorting_center",
      true),

  AT_LOCAL_HUB(
      "LOCAL_DELIVERY",
      "Package arrived at local warehouse",
      "Package at CommercePal local hub in Ethiopia",
      "at_local_hub",
      true),

  OUT_FOR_DELIVERY(
      "LOCAL_DELIVERY",
      "Out for delivery",
      "Package is out with delivery courier in your area",
      "out_for_delivery",
      true),

  DELIVERY_ATTEMPTED(
      "LOCAL_DELIVERY",
      "Delivery attempted",
      "Delivery attempt made but unsuccessful",
      "delivery_attempted",
      true),

  DELIVERY_RESCHEDULED(
      "LOCAL_DELIVERY",
      "Delivery rescheduled",
      "Delivery has been rescheduled",
      "delivery_rescheduled",
      true),

  // COMPLETION EVENTS

  DELIVERED(
      "COMPLETION",
      "Delivered",
      "Package successfully delivered to customer",
      "delivered",
      true),

  RECEIVED_BY_CUSTOMER(
      "COMPLETION",
      "Package received by customer",
      "Customer confirmed receipt of package",
      "received_by_customer",
      true),

  // RETURN & REFUND EVENTS

  RETURN_REQUESTED(
      "RETURN",
      "Return requested",
      "Customer requested to return items",
      "return_requested",
      true),

  RETURN_APPROVED(
      "RETURN",
      "Return approved",
      "Return request has been approved",
      "return_approved",
      true),

  RETURN_IN_TRANSIT(
      "RETURN",
      "Return in transit",
      "Returned items being shipped back to hub",
      "return_in_transit",
      true),

  RETURN_RECEIVED(
      "RETURN",
      "Return received",
      "Returned items received at hub",
      "return_received",
      true),

  REFUND_INITIATED(
      "RETURN",
      "Refund initiated",
      "Refund process started",
      "refund_initiated",
      true),

  REFUND_COMPLETED(
      "RETURN",
      "Refund completed",
      "Refund successfully issued to customer",
      "refund_completed",
      true),

  // EXCEPTION EVENTS

  DELAYED(
      "EXCEPTION",
      "Delivery delayed",
      "Package delivery has been delayed",
      "delayed",
      true),

  ON_HOLD(
      "EXCEPTION",
      "On hold",
      "Package temporarily on hold",
      "on_hold",
      true),

  ADDRESS_ISSUE(
      "EXCEPTION",
      "Address verification needed",
      "There is an issue with the delivery address",
      "address_issue",
      true),

  CANCELLED(
      "EXCEPTION",
      "Order cancelled",
      "Order has been cancelled",
      "cancelled",
      true),

  FAILED(
      "EXCEPTION",
      "Delivery failed",
      "Order processing or delivery failed",
      "failed",
      true),

  // SYSTEM EVENTS (Internal tracking, not always customer-visible)

  TRACKING_UPDATED(
      "SYSTEM",
      "Tracking information updated",
      "Tracking information refreshed from provider",
      "tracking_updated",
      false),

  NOTE_ADDED(
      "SYSTEM",
      "Note added",
      "Additional note added to order",
      "note_added",
      false);

  /**
   * Event category for grouping related events
   */
  private final String category;

  /**
   * Customer-facing label (short)
   */
  private final String label;

  /**
   * Detailed description
   */
  private final String description;

  /**
   * Icon identifier for UI rendering
   */
  private final String icon;

  /**
   * Whether this event should be visible to customers
   */
  private final boolean customerVisible;

  TrackingEventType(String category, String label, String description, String icon,
      boolean customerVisible) {
    this.category = category;
    this.label = label;
    this.description = description;
    this.icon = icon;
    this.customerVisible = customerVisible;
  }

  /**
   * Check if event is an exception/problem event
   */
  public boolean isException() {
    return category.equals("EXCEPTION");
  }

  /**
   * Check if event is a completion event
   */
  public boolean isCompletion() {
    return category.equals("COMPLETION") || this == REFUND_COMPLETED;
  }

  /**
   * Check if event is related to customs
   */
  public boolean isCustomsEvent() {
    return category.equals("CUSTOMS");
  }

  /**
   * Check if event is related to local delivery
   */
  public boolean isLocalDeliveryEvent() {
    return category.equals("LOCAL_DELIVERY");
  }
}
