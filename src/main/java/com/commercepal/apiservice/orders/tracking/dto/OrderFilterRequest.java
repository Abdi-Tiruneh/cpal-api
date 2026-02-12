package com.commercepal.apiservice.orders.tracking.dto;

import com.commercepal.apiservice.orders.tracking.enums.OrderStageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderFilterRequest
 * <p>
 * Request DTO for filtering and searching orders in the list view. Supports filtering by stage
 * category, search query, and date range.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {

  /**
   * Filter by stage category Examples: ALL, TO_PAY, TO_SHIP, SHIPPED, PROCESSED, CANCELLED
   */
  private OrderStageCategory stageCategory;

  /**
   * Search query (searches order number, product name)
   */
  private String searchQuery;

  /**
   * Filter by date range start (ISO format: yyyy-MM-dd)
   */
  private String dateFrom;

  /**
   * Filter by date range end (ISO format: yyyy-MM-dd)
   */
  private String dateTo;

  /**
   * Page number (0-indexed)
   */
  @Builder.Default
  private final Integer page = 0;

  /**
   * Page size
   */
  @Builder.Default
  private final Integer size = 20;

  /**
   * Sort field Examples: "orderDate", "totalAmount", "currentStage"
   */
  @Builder.Default
  private final String sort = "orderDate";

  /**
   * Sort direction: "asc" or "desc"
   */
  @Builder.Default
  private final String direction = "desc";
}
