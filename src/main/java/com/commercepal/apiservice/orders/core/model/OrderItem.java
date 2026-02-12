package com.commercepal.apiservice.orders.core.model;

import com.commercepal.apiservice.orders.enums.OrderItemStage;
import com.commercepal.apiservice.shared.BaseAuditEntity;
import com.commercepal.apiservice.shared.enums.Provider;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OrderItem Entity
 * <p>
 * Represents individual items within an order. Each order item tracks its own
 * lifecycle, pricing,
 * provider information, and fulfillment status.
 * <p>
 * Key Features: - Proper relationship with parent Order - Multi-provider
 * support with
 * provider-specific tracking - Comprehensive pricing breakdown (unit price,
 * discounts, taxes,
 * markups) - Multi-currency support with exchange rate tracking - Individual
 * item lifecycle
 * tracking - Product snapshot at time of order - Warehouse and fulfillment
 * tracking
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order", columnList = "order_id"),
    @Index(name = "idx_order_items_stage", columnList = "current_stage"),
    @Index(name = "idx_order_items_provider", columnList = "provider"),
    @Index(name = "idx_order_items_order_stage", columnList = "order_id,current_stage"),
    @Index(name = "idx_order_items_sub_order_number", columnList = "sub_order_number")
})
public class OrderItem extends BaseAuditEntity {

  // RELATIONSHIPS
  /**
   * Parent order this item belongs to
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_item_order"))
  private Order order;

  // CORE IDENTIFIERS
  /**
   * Sub-order number for this specific item (e.g., ORD-2024-001234-01) Useful for
   * tracking
   * individual items in multi-item orders
   */
  @Column(name = "sub_order_number", nullable = false, unique = true, length = 50)
  private String subOrderNumber;

  // PRODUCT INFORMATION (SNAPSHOT AT TIME OF ORDER)
  /**
   * Product name at time of order (snapshot)
   */
  @Column(name = "product_name", nullable = false, columnDefinition = "TEXT")
  private String productName;

  /**
   * Product description at time of order
   */
  @Column(name = "product_description", columnDefinition = "TEXT")
  private String productDescription;

  /**
   * Product image URL at time of order
   */
  @Column(name = "product_image_url", length = 500)
  private String productImageUrl;

  /**
   * Product configuration details (e.g., "Size: L, Color: Blue")
   */
  @Column(name = "product_configuration", columnDefinition = "TEXT")
  private String productConfiguration;

  /**
   * Product category ID
   */
  @Column(name = "category_id")
  private Long categoryId;

  /**
   * Weight of a single item (in kg)
   */
  @Column(name = "weight_kg", precision = 10, scale = 3)
  private BigDecimal weightKg;

  // PROVIDER INFORMATION
  /**
   * Product provider/source (AliExpress, Amazon, Alibaba, etc.)
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false, length = 30)
  private Provider provider;

  /**
   * Provider's product ID (external reference)
   */
  @Column(name = "provider_product_id", length = 255)
  private String providerProductId;

  /**
   * Provider's product Configuration ID
   */
  @Column(name = "config_id")
  private String configId;

  /**
   * External product URL from provider
   */
  @Column(name = "provider_product_url", columnDefinition = "TEXT")
  private String providerProductUrl;

  /**
   * Whether this item has been placed/ordered on the external provider
   */
  @Column(name = "is_placed_on_provider", nullable = false)
  @Builder.Default
  private final Boolean isPlacedOnProvider = false;

  /**
   * When this item was placed on the external provider
   */
  @Column(name = "placed_on_provider_at")
  private LocalDateTime placedOnProviderAt;

  /**
   * Provider's tracking number for this item
   */
  @Column(name = "provider_tracking_number", length = 255)
  private String providerTrackingNumber;

  /**
   * Provider's order status (from their system) Examples: "Processing",
   * "Shipped", "In Transit",
   * "Delivered"
   */
  @Column(name = "provider_order_status", length = 100)
  private String providerOrderStatus;

  /**
   * When provider order status was last updated
   */
  @Column(name = "provider_status_updated_at")
  private LocalDateTime providerStatusUpdatedAt;

  /**
   * Whether item has arrived in Ethiopia
   */
  @Column(name = "is_arrived_in_ethiopia", nullable = false)
  @Builder.Default
  private final Boolean isArrivedInEthiopia = false;

  /**
   * When item arrived in Ethiopia
   */
  @Column(name = "arrived_in_ethiopia_at")
  private LocalDateTime arrivedInEthiopiaAt;

  /**
   * Ethiopian customs status for this item Possible values: PENDING,
   * IN_CLEARANCE, CLEARED, HELD,
   * RELEASED
   */
  @Column(name = "customs_status", length = 30)
  private String customsStatus;

  /**
   * When item cleared Ethiopian customs
   */
  @Column(name = "customs_cleared_at")
  private LocalDateTime customsClearedAt;

  /**
   * Ethiopian customs reference number
   */
  @Column(name = "customs_reference_number", length = 100)
  private String customsReferenceNumber;

  /**
   * Merchant/vendor ID (if applicable)
   */
  @Column(name = "merchant_id")
  private Long merchantId;

  // PRICING & CURRENCY
  /**
   * Currency for this order item
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "order_currency", nullable = false, length = 3)
  private SupportedCurrency orderCurrency;

  /**
   * Unit price (selling price per item)
   */
  @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  /**
   * Quantity ordered
   */
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  /**
   * Subtotal (unit price × quantity, before discounts)
   */
  @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
  private BigDecimal subtotal;

  /**
   * Discount amount applied to this item
   */
  @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private final BigDecimal discountAmount = BigDecimal.ZERO;

  /**
   * Tax amount for this item
   */
  @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private final BigDecimal taxAmount = BigDecimal.ZERO;

  /**
   * Total amount for this item (subtotal - discount + tax)
   */
  @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
  private BigDecimal totalAmount;

  // PROVIDER PRICING (COST/WHOLESALE)
  /**
   * Provider's original currency
   */
  @Column(name = "provider_currency", length = 3)
  private String providerCurrency;

  /**
   * Provider's original unit price (wholesale/cost price)
   */
  @Column(name = "provider_unit_price", precision = 15, scale = 2)
  private BigDecimal providerUnitPrice;

  /**
   * Markup amount per unit (selling price - cost price)
   */
  @Column(name = "unit_markup", precision = 15, scale = 2)
  private BigDecimal unitMarkup;

  /**
   * Total markup for this item (unit markup × quantity)
   */
  @Column(name = "total_markup", precision = 15, scale = 2)
  private BigDecimal totalMarkup;

  /**
   * Exchange rate used for currency conversion
   */
  @Column(name = "exchange_rate", precision = 10, scale = 4, nullable = false)
  private BigDecimal exchangeRate;

  // DELIVERY & FULFILLMENT
  /**
   * Delivery fee for this item
   */
  @Column(name = "delivery_fee", precision = 15, scale = 2)
  @Builder.Default
  private final BigDecimal deliveryFee = BigDecimal.ZERO;

  /**
   * Warehouse that first receives the item (e.g., from provider/customs)
   */
  @Column(name = "receiving_warehouse_id")
  private Long receivingWarehouseId;

  /**
   * Final warehouse where item was processed
   */
  @Column(name = "finalizing_warehouse_id")
  private Long finalizingWarehouseId;

  // ITEM LIFECYCLE & STATUS
  /**
   * Current stage of this order item
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "current_stage", nullable = false, length = 30)
  @Builder.Default
  private OrderItemStage currentStage = OrderItemStage.PENDING;

  /**
   * When item stage was last updated
   */
  @Column(name = "stage_updated_at")
  private LocalDateTime stageUpdatedAt;

  /**
   * Whether item is completed (delivered/refunded/cancelled)
   */
  @Column(name = "is_completed", nullable = false)
  @Builder.Default
  private Boolean isCompleted = false;

  /**
   * Whether item is cancelled
   */
  @Column(name = "is_cancelled", nullable = false)
  @Builder.Default
  private Boolean isCancelled = false;

  /**
   * Whether item is paid for
   */
  @Column(name = "is_paid", nullable = false)
  @Builder.Default
  private Boolean isPaid = false;

  // TRACKING & LOGISTICS
  /**
   * QR code assigned to this item for tracking
   */
  @Column(name = "qr_code", length = 100)
  private String qrCode;

  /**
   * When QR code was assigned
   */
  @Column(name = "qr_code_assigned_at")
  private LocalDateTime qrCodeAssignedAt;

  /**
   * Shipment tracking number
   */
  @Column(name = "shipment_tracking_number", length = 255)
  private String shipmentTrackingNumber;

  /**
   * Shipment type/method (e.g., AIR, SEA, GROUND)
   */
  @Column(name = "shipment_type", length = 50)
  private String shipmentType;

  /**
   * Comments about shipment
   */
  @Column(name = "shipment_comments", columnDefinition = "TEXT")
  private String shipmentComments;

  // SETTLEMENT & ACCOUNTING
  /**
   * Settlement status (for merchant/vendor payments)
   */
  @Column(name = "settlement_status", length = 30)
  private String settlementStatus;

  /**
   * Settlement reference number
   */
  @Column(name = "settlement_reference", length = 255)
  private String settlementReference;

  /**
   * When settlement was completed
   */
  @Column(name = "settlement_completed_at")
  private LocalDateTime settlementCompletedAt;

  // NOTES & COMMENTS
  /**
   * Customer comments/notes about this item
   */
  @Column(name = "customer_notes", columnDefinition = "TEXT")
  private String customerNotes;

  /**
   * Operator/admin comments about this item
   */
  @Column(name = "operator_notes", columnDefinition = "TEXT")
  private String operatorNotes;

  // HELPER METHODS

  /**
   * Mark item as paid
   */
  public void markAsPaid() {
    this.isPaid = true;
  }

  /**
   * Cancel the item
   */
  public void cancel() {
    this.isCancelled = true;
    this.currentStage = OrderItemStage.CANCELLED;
    this.stageUpdatedAt = LocalDateTime.now();
  }

  /**
   * Complete the item
   */
  public void complete() {
    this.isCompleted = true;
    this.currentStage = OrderItemStage.DELIVERED;
    this.stageUpdatedAt = LocalDateTime.now();
  }

  /**
   * Update stage
   */
  public void updateStage(OrderItemStage newStage) {
    this.currentStage = newStage;
    this.stageUpdatedAt = LocalDateTime.now();
  }
}
