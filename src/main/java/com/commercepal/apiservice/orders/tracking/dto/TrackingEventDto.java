package com.commercepal.apiservice.orders.tracking.dto;

import com.commercepal.apiservice.orders.tracking.enums.TrackingEventType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TrackingEventDto
 * <p>
 * DTO for individual tracking timeline events shown to customers. Matches AliExpress tracking event
 * format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventDto {

  /**
   * Event type identifier
   */
  private TrackingEventType eventType;

  /**
   * Customer-facing event label (short) Example: "Customs clearance started"
   */
  private String eventLabel;

  /**
   * Detailed event description Example: "Import customs clearance started"
   */
  private String description;

  /**
   * When the event occurred
   */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime eventTimestamp;

  /**
   * Formatted timestamp for display Example: "Dec 1, 12:03 (GMT+3)"
   */
  private String formattedTimestamp;

  /**
   * Geographic location where event occurred Example: "Addis Ababa, Ethiopia"
   */
  private String location;

  /**
   * Carrier name (if applicable) Example: "DHL", "Ethiopian Airlines Cargo"
   */
  private String carrierName;

  /**
   * Whether this is the current active/highlighted event in the timeline
   */
  private boolean isActive;

  /**
   * Whether this is an exception event (delay, hold, failure)
   */
  private boolean isException;

  /**
   * Icon identifier for UI rendering Maps to event type's icon
   */
  private String icon;

  /**
   * Event category for grouping Example: "ORDER", "SHIPPING", "CUSTOMS", "DELIVERY"
   */
  private String category;
}
