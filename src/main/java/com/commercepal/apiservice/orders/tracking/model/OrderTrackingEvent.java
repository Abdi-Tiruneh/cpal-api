package com.commercepal.apiservice.orders.tracking.model;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.model.OrderItem;
import com.commercepal.apiservice.orders.tracking.enums.TrackingEventType;
import com.commercepal.apiservice.shared.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OrderTrackingEvent Entity
 * <p>
 * Stores individual tracking timeline events for orders and order items. Provides a comprehensive
 * audit trail of order progress from creation to delivery.
 * <p>
 * Features: - Order-level and item-level tracking support - Geographic location tracking - Carrier
 * and tracking number association - Customer visibility control - Rich metadata support for
 * additional event data - Timeline visualization support
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_tracking_events", indexes = {
    @Index(name = "idx_tracking_events_order", columnList = "order_id"),
    @Index(name = "idx_tracking_events_order_item", columnList = "order_item_id"),
    @Index(name = "idx_tracking_events_timestamp", columnList = "event_timestamp"),
    @Index(name = "idx_tracking_events_order_timestamp", columnList = "order_id,event_timestamp"),
    @Index(name = "idx_tracking_events_type", columnList = "event_type")
})
public class OrderTrackingEvent extends BaseAuditEntity {

  // RELATIONSHIPS

  /**
   * Parent order this tracking event belongs to
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tracking_event_order"))
  private Order order;

  /**
   * Specific order item this event relates to (optional - for item-level tracking) If null, this
   * event applies to the entire order
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_item_id", foreignKey = @ForeignKey(name = "fk_tracking_event_order_item"))
  private OrderItem orderItem;

  // EVENT DETAILS

  /**
   * Type of tracking event
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 50)
  private TrackingEventType eventType;

  /**
   * When this event occurred This is the actual event timestamp, not when it was recorded in the
   * system
   */
  @Column(name = "event_timestamp", nullable = false)
  private LocalDateTime eventTimestamp;

  /**
   * Geographic location where event occurred Examples: "Shanghai, China", "Dubai, UAE", "Addis
   * Ababa, Ethiopia"
   */
  @Column(name = "location", length = 255)
  private String location;

  /**
   * Human-readable event description Customer-facing description of what happened
   */
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  /**
   * Additional event metadata in JSON format Can store provider-specific data, temperature logs,
   * package condition, etc. Example: {"providerEventId": "12345", "temperature": "20C", "humidity":
   * "45%"}
   */
  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata;

  // CARRIER & TRACKING

  /**
   * Name of shipping carrier for this event Examples: "DHL", "FedEx", "Ethiopian Airlines Cargo",
   * "Local Courier"
   */
  @Column(name = "carrier_name", length = 100)
  private String carrierName;

  /**
   * Tracking number associated with this event Can be provider tracking number or local tracking
   * number
   */
  @Column(name = "tracking_number", length = 255)
  private String trackingNumber;

  // VISIBILITY & DISPLAY

  /**
   * Whether this event should be visible to customers Set to false for internal/system events
   */
  @Column(name = "is_customer_visible", nullable = false)
  @Builder.Default
  private final Boolean isCustomerVisible = true;

  /**
   * Whether this is the current active/highlighted event in the timeline Only one event per order
   * should typically be active at a time
   */
  @Column(name = "is_active_event", nullable = false)
  @Builder.Default
  private Boolean isActiveEvent = false;

  /**
   * Display order/sequence number for timeline visualization Lower numbers appear first in timeline
   * (chronological order)
   */
  @Column(name = "sequence_number")
  private Integer sequenceNumber;

  // ADDITIONAL INFORMATION

  /**
   * Internal notes (not shown to customers) For admin/operator reference
   */
  @Column(name = "internal_notes", columnDefinition = "TEXT")
  private String internalNotes;

  /**
   * User/agent who created this event (if manually added)
   */
  @Column(name = "created_by_user_id")
  private Long createdByUserId;

  /**
   * Whether this event was automatically created by the system vs manually added by an admin/agent
   */
  @Column(name = "is_auto_generated", nullable = false)
  @Builder.Default
  private final Boolean isAutoGenerated = true;

  // HELPER METHODS

  /**
   * Mark this event as the active event
   */
  public void markAsActive() {
    this.isActiveEvent = true;
  }

  /**
   * Remove active status
   */
  public void markAsInactive() {
    this.isActiveEvent = false;
  }

  /**
   * Check if this is an exception event
   */
  public boolean isExceptionEvent() {
    return eventType != null && eventType.isException();
  }

  /**
   * Check if this is a completion event
   */
  public boolean isCompletionEvent() {
    return eventType != null && eventType.isCompletion();
  }

  /**
   * Get customer-facing label from event type
   */
  public String getEventLabel() {
    return eventType != null ? eventType.getLabel() : "Unknown Event";
  }
}
