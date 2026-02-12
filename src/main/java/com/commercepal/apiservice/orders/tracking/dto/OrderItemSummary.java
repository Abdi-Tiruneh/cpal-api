package com.commercepal.apiservice.orders.tracking.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderItemSummary
 * <p>
 * Summary of an order item for the order list view. Contains essential product information to
 * display in order cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemSummary {

  /**
   * Sub-order number for this item
   */
  private String subOrderNumber;

  /**
   * Product name
   */
  private String productName;

  /**
   * Product thumbnail image URL
   */
  private String productImageUrl;

  /**
   * Product configuration/variant details Example: "Yellow", "Size: L, Color: Blue"
   */
  private String productConfiguration;

  /**
   * Unit price per item
   */
  private BigDecimal unitPrice;

  /**
   * Quantity ordered
   */
  private Integer quantity;

  /**
   * Subtotal for this item (unitPrice × quantity)
   */
  private BigDecimal subTotal;

  /**
   * Currency code
   */
  private String currency;

  /**
   * Current stage of this specific item May differ from overall order stage
   */
  private String itemStage;

  /**
   * Item stage label for display
   */
  private String itemStageLabel;
}
