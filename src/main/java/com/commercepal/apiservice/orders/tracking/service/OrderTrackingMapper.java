package com.commercepal.apiservice.orders.tracking.service;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.model.OrderItem;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.orders.tracking.dto.DeliveryAddressSummary;
import com.commercepal.apiservice.orders.tracking.dto.OrderItemSummary;
import com.commercepal.apiservice.orders.tracking.dto.OrderListResponse;
import com.commercepal.apiservice.orders.tracking.dto.OrderTrackingResponse;
import com.commercepal.apiservice.orders.tracking.dto.TrackingEventDto;
import com.commercepal.apiservice.orders.tracking.enums.OrderStageCategory;
import com.commercepal.apiservice.orders.tracking.model.OrderTrackingEvent;
import com.commercepal.apiservice.users.customer.address.CustomerAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * OrderTrackingMapper
 * <p>
 * Maps Order entities and tracking events to customer-facing DTOs. Handles
 * formatting, masking
 * sensitive data, and determining action button states.
 */
@Service
public class OrderTrackingMapper {

  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
      "MMM d, HH:mm");

  private static final DateTimeFormatter DELIVERY_DATE_FORMATTER = DateTimeFormatter.ofPattern(
      "MMM. d");

  /**
   * Map Order to OrderListResponse
   */
  public OrderListResponse toOrderListResponse(Order order) {
    return OrderListResponse.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber())
        .orderDate(order.getOrderedAt())
        .currentStage(order.getCurrentStage().name())
        .stageLabel(order.getCurrentStage().getLabel())
        .stageCategory(OrderStageCategory.fromOrderStage(order.getCurrentStage()))
        .statusDescription(generateStatusDescription(order))
        .items(mapOrderItems(order.getOrderItems()))
        .totalItemsCount(order.getTotalItemsCount())
        .subtotal(order.getSubtotal())
        .totalAmount(order.getTotalAmount())
        .currency(order.getOrderCurrency().name())
        .deliveryAddress(mapDeliveryAddress(order.getDeliveryAddress()))
        .storeName(determineStoreName(order))
        .canConfirmReceived(canConfirmReceived(order))
        .canTrack(canTrack(order))
        .canCancel(canCancel(order))
        .canReturn(canReturn(order))
        .canPay(canPay(order))
        .canReview(canReview(order))
        .paymentStatus(order.getPaymentStatus().name())
        .paymentStatusLabel(order.getPaymentStatus().name())
        .hasException(hasException(order))
        .exceptionMessage(getExceptionMessage(order))
        .build();
  }

  /**
   * Map Order to OrderTrackingResponse with tracking events
   */
  public OrderTrackingResponse toOrderTrackingResponse(
      Order order,
      List<OrderTrackingEvent> trackingEvents) {

    List<TrackingEventDto> eventDtos = trackingEvents.stream()
        .map(this::toTrackingEventDto)
        .collect(Collectors.toList());

    return OrderTrackingResponse.builder()
        .orderNumber(order.getOrderNumber())
        .orderDate(order.getOrderedAt())
        .currentStage(order.getCurrentStage().name())
        .currentStageLabel(order.getCurrentStage().getLabel())
        .estimatedDeliveryStart(order.getEstimatedDeliveryStart())
        .estimatedDeliveryEnd(order.getEstimatedDeliveryEnd())
        .deliveryWindow(formatDeliveryWindow(order))
        .shippingMethod(determineShippingMethod(order))
        .providerTrackingNumber(getProviderTrackingNumber(order))
        .localTrackingNumber(order.getLocalTrackingNumber())
        .carrierName(determineCarrierName(order))
        .trackingEvents(eventDtos)
        .totalEvents(eventDtos.size())
        .deliveryAddress(mapDeliveryAddress(order.getDeliveryAddress()))
        .items(mapOrderItems(order.getOrderItems()))
        .canConfirmReceived(canConfirmReceived(order))
        .canRefreshTracking(canRefreshTracking(order))
        .lastTrackingUpdate(getLastTrackingUpdate(trackingEvents))
        .hasException(hasException(order))
        .exceptionMessage(getExceptionMessage(order))
        .build();
  }

  /**
   * Map OrderTrackingEvent to TrackingEventDto
   */
  public TrackingEventDto toTrackingEventDto(OrderTrackingEvent event) {
    return TrackingEventDto.builder()
        .eventType(event.getEventType())
        .eventLabel(event.getEventType().getLabel())
        .description(event.getDescription())
        .eventTimestamp(event.getEventTimestamp())
        .formattedTimestamp(formatTimestamp(event.getEventTimestamp()))
        .location(event.getLocation())
        .carrierName(event.getCarrierName())
        .isActive(event.getIsActiveEvent())
        .isException(event.isExceptionEvent())
        .icon(event.getEventType().getIcon())
        .category(event.getEventType().getCategory())
        .build();
  }

  /**
   * Map OrderItem to OrderItemSummary
   */
  public OrderItemSummary toOrderItemSummary(OrderItem item) {
    return OrderItemSummary.builder()
        .subOrderNumber(item.getSubOrderNumber())
        .productName(item.getProductName())
        .productImageUrl(item.getProductImageUrl())
        .productConfiguration(item.getProductConfiguration())
        .unitPrice(item.getUnitPrice())
        .quantity(item.getQuantity())
        .subTotal(item.getSubtotal())
        .currency(item.getOrderCurrency().name())
        .itemStage(item.getCurrentStage().name())
        .itemStageLabel(item.getCurrentStage().getLabel())
        .build();
  }

  /**
   * Map CustomerAddress to DeliveryAddressSummary
   */
  public DeliveryAddressSummary mapDeliveryAddress(CustomerAddress address) {
    if (address == null) {
      return null;
    }

    return DeliveryAddressSummary.builder()
        .fullName(address.getReceiverName())
        .streetAddress(address.getStreet())
        .city(address.getCity())
        .subcity(address.getDistrict()) // Using district as subcity
        .region(address.getState()) // Using state as region
        .phoneNumber(maskPhoneNumber(address.getPhoneNumber()))
        .postalCode(null) // Not in current model
        .country(address.getCountry())
        .formattedAddress(formatAddress(address))
        .build();
  }

  // =========================================================================
  // HELPER METHODS
  // =========================================================================

  private List<OrderItemSummary> mapOrderItems(List<OrderItem> items) {
    if (items == null) {
      return List.of();
    }
    return items.stream()
        .map(this::toOrderItemSummary)
        .collect(Collectors.toList());
  }

  private String generateStatusDescription(Order order) {
    if (order.getEstimatedDeliveryStart() != null && order.getEstimatedDeliveryEnd() != null) {
      return String.format("Expected delivery: %s - %s",
          order.getEstimatedDeliveryStart().format(DELIVERY_DATE_FORMATTER),
          order.getEstimatedDeliveryEnd().format(DELIVERY_DATE_FORMATTER));
    }
    return order.getCurrentStage().getDescription();
  }

  private String determineStoreName(Order order) {
    // Get from first item's provider
    if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
      return order.getOrderItems().get(0).getProvider().name();
    }
    return "Store";
  }

  private String formatDeliveryWindow(Order order) {
    if (order.getEstimatedDeliveryStart() == null || order.getEstimatedDeliveryEnd() == null) {
      return null;
    }
    return String.format("%s - %s",
        order.getEstimatedDeliveryStart().format(DELIVERY_DATE_FORMATTER),
        order.getEstimatedDeliveryEnd().format(DELIVERY_DATE_FORMATTER));
  }

  private String determineShippingMethod(Order order) {
    // Could be enhanced to get actual shipping method from order items
    if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
      String provider = order.getOrderItems().get(0).getProvider().name();
      return provider + " Standard Shipping";
    }
    return "Standard Shipping";
  }

  private String getProviderTrackingNumber(Order order) {
    // Get from first item if available
    if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
      return order.getOrderItems().get(0).getProviderTrackingNumber();
    }
    return null;
  }

  private String determineCarrierName(Order order) {
    // Could be enhanced to get actual carrier from tracking events or items
    return "International Carrier";
  }

  private LocalDateTime getLastTrackingUpdate(List<OrderTrackingEvent> events) {
    if (events == null || events.isEmpty()) {
      return null;
    }
    return events.get(0).getCreatedAt(); // Events are ordered by timestamp desc
  }

  private String formatTimestamp(LocalDateTime timestamp) {
    if (timestamp == null) {
      return null;
    }
    return timestamp.format(TIMESTAMP_FORMATTER);
  }

  private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 8) {
      return phoneNumber;
    }
    // Mask middle digits: +251 91*****901
    int visibleStart = Math.min(7, phoneNumber.length() - 3);
    int visibleEnd = Math.max(visibleStart + 1, phoneNumber.length() - 3);

    String start = phoneNumber.substring(0, visibleStart);
    String end = phoneNumber.substring(visibleEnd);
    String masked = "*".repeat(visibleEnd - visibleStart);

    return start + masked + end;
  }

  private String formatAddress(CustomerAddress address) {
    StringBuilder sb = new StringBuilder();
    if (address.getReceiverName() != null) {
      sb.append(address.getReceiverName());
    }
    if (address.getStreet() != null) {
      if (!sb.isEmpty()) {
        sb.append(" - ");
      }
      sb.append(address.getStreet());
    }
    if (address.getDistrict() != null) {
      if (!sb.isEmpty()) {
        sb.append(", ");
      }
      sb.append(address.getDistrict());
    }
    if (address.getCity() != null) {
      if (!sb.isEmpty()) {
        sb.append(", ");
      }
      sb.append(address.getCity());
    }
    return sb.toString();
  }

  // =========================================================================
  // ACTION BUTTON LOGIC
  // =========================================================================

  private boolean canConfirmReceived(Order order) {
    return order.getCurrentStage() == OrderStage.OUT_FOR_LOCAL_DELIVERY;
  }

  private boolean canTrack(Order order) {
    return !order.getCurrentStage().isFinalStage();
  }

  private boolean canCancel(Order order) {
    return order.getCurrentStage() == OrderStage.PENDING ||
        order.getCurrentStage() == OrderStage.PAYMENT_CONFIRMED ||
        order.getCurrentStage() == OrderStage.PROCESSING;
  }

  private boolean canReturn(Order order) {
    return order.getCurrentStage() == OrderStage.DELIVERED;
  }

  private boolean canPay(Order order) {
    return order.getCurrentStage() == OrderStage.PENDING &&
        order.getPaymentStatus() == PaymentStatus.PENDING;
  }

  private boolean canReview(Order order) {
    return order.getCurrentStage() == OrderStage.DELIVERED;
  }

  private boolean canRefreshTracking(Order order) {
    return order.getCurrentStage().isWithProvider() ||
        order.getCurrentStage().isInEthiopia();
  }

  private boolean hasException(Order order) {
    return order.getCurrentStage() == OrderStage.FAILED ||
        order.getCurrentStage() == OrderStage.CANCELLED ||
        order.getCurrentStage() == OrderStage.ON_HOLD ||
        order.getCurrentStage() == OrderStage.CUSTOMS_HELD;
  }

  private String getExceptionMessage(Order order) {
    if (!hasException(order)) {
      return null;
    }

    return switch (order.getCurrentStage()) {
      case CUSTOMS_HELD -> "Package held by customs. Additional documentation may be required.";
      case ON_HOLD -> "Order temporarily on hold. We'll update you soon.";
      case CANCELLED -> "Order has been cancelled. " + order.getCancellationReason();
      case FAILED -> "Order processing failed. Please contact support.";
      default -> null;
    };
  }
}
