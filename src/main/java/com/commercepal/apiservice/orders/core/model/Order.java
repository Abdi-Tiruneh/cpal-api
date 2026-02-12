package com.commercepal.apiservice.orders.core.model;

import com.commercepal.apiservice.orders.enums.OrderPriority;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.orders.enums.RefundStatus;
import com.commercepal.apiservice.shared.BaseAuditEntity;
import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.customer.address.CustomerAddress;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.OrderUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Order Entity
 * <p>
 * Professional order management entity designed for scalability and efficiency.
 * Supports
 * multi-provider e-commerce with proper relationships to Customer and
 * DeliveryAddress.
 * <p>
 * Key Features: - Proper JPA relationships with Customer and CustomerAddress -
 * Multi-currency
 * support with exchange rate tracking - Multi-provider support (AliExpress,
 * Amazon, Alibaba, Shein,
 * etc.) - Comprehensive order lifecycle tracking - Payment and refund
 * management - Fraud detection
 * integration - Promotion and referral tracking
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders", uniqueConstraints = {
    @UniqueConstraint(name = "uk_orders_order_number", columnNames = "order_number")
}, indexes = {
    @Index(name = "idx_orders_customer", columnList = "customer_id"),
    @Index(name = "idx_orders_stage", columnList = "current_stage"),
    @Index(name = "idx_orders_payment_status", columnList = "payment_status"),
    @Index(name = "idx_orders_created_at", columnList = "created_at"),
    @Index(name = "idx_orders_customer_stage", columnList = "customer_id,current_stage"),
    @Index(name = "idx_orders_order_number", columnList = "order_number")
})
public class Order extends BaseAuditEntity {

  // CORE IDENTIFIERS
  /**
   * Unique order reference number (e.g., ORD-2024-001234) Human-readable
   * identifier for customer
   * service and tracking
   */
  @Column(name = "order_number", nullable = false, unique = true, length = 50)
  private String orderNumber;

  // RELATIONSHIPS
  /**
   * Customer who placed the order
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_customer"))
  private Customer customer;

  /**
   * Delivery address for this order
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "delivery_address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_delivery_address"))
  private CustomerAddress deliveryAddress;

  /**
   * Order items in this order
   */
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private final List<OrderItem> orderItems = new ArrayList<>();

  // CART TRACKING (for analytics and conversion tracking)
  /**
   * Source cart ID (optional soft link for analytics) Tracks which cart was
   * converted to this order
   * for: - Conversion rate analysis - Customer journey tracking - Abandoned cart
   * recovery insights
   */
  @Column(name = "cart_id")
  private Long cartId;

  // ORDER METADATA
  /**
   * Platform/channel where order was placed
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "platform", nullable = false, length = 20)
  private Channel platform;

  /**
   * Order priority level
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false, length = 20)
  @Builder.Default
  private final OrderPriority priority = OrderPriority.NORMAL;

  /**
   * Total number of items in this order
   */
  @Column(name = "total_items_count", nullable = false)
  @Builder.Default
  private Integer totalItemsCount = 0;

  // PRICING & CURRENCY
  /**
   * Currency for this order
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "order_currency", nullable = false, length = 3)
  private SupportedCurrency orderCurrency;

  /**
   * Subtotal (sum of all item prices before discounts/taxes)
   */
  @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal subtotal = BigDecimal.ZERO;

  /**
   * Total discount amount applied to order
   */
  @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private final BigDecimal discountAmount = BigDecimal.ZERO;

  /**
   * Tax amount
   */
  @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  /**
   * Delivery/shipping fee
   */
  @Column(name = "delivery_fee", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal deliveryFee = BigDecimal.ZERO;

  /**
   * Additional charges (processing fees, etc.)
   */
  @Column(name = "additional_charges", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private final BigDecimal additionalCharges = BigDecimal.ZERO;

  /**
   * Final total amount (subtotal - discount + tax + delivery + charges)
   */
  @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal totalAmount = BigDecimal.ZERO;

  // PAYMENT INFORMATION
  /**
   * Payment status
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status", nullable = false, length = 20)
  @Builder.Default
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  /**
   * When payment was confirmed
   */
  @Column(name = "payment_confirmed_at")
  private LocalDateTime paymentConfirmedAt;

  /**
   * Payment gateway reference/transaction ID
   */
  @Column(name = "payment_reference", length = 255)
  private String paymentReference;

  // ORDER LIFECYCLE & STATUS
  /**
   * Current stage of the order
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "current_stage", nullable = false, length = 30)
  @Builder.Default
  private OrderStage currentStage = OrderStage.PENDING;

  @Column(name = "ordered_at", nullable = false)
  private LocalDateTime orderedAt;

  /**
   * When the order was completed/delivered
   */
  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  /**
   * When the order was cancelled (if applicable)
   */
  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  /**
   * Reason for cancellation
   */
  @Column(name = "cancellation_reason", length = 500)
  private String cancellationReason;

  // REFUND MANAGEMENT
  /**
   * Refund status
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "refund_status", nullable = false, length = 20)
  @Builder.Default
  private final RefundStatus refundStatus = RefundStatus.NONE;

  /**
   * Total amount refunded
   */
  @Column(name = "refunded_amount", precision = 15, scale = 2)
  @Builder.Default
  private final BigDecimal refundedAmount = BigDecimal.ZERO;

  /**
   * When refund was initiated
   */
  @Column(name = "refund_initiated_at")
  private LocalDateTime refundInitiatedAt;

  /**
   * When refund was completed
   */
  @Column(name = "refund_completed_at")
  private LocalDateTime refundCompletedAt;

  // PROMOTIONS & REFERRALS
  /**
   * Promotion ID applied to this order
   */
  @Column(name = "promotion_id")
  private Long promotionId;

  /**
   * Discount amount from promotion
   */
  @Column(name = "promotion_discount_amount", precision = 15, scale = 2)
  @Builder.Default
  private final BigDecimal promotionDiscountAmount = BigDecimal.ZERO;

  /**
   * Referral user type (if order came through referral)
   */
  @Column(name = "referral_user_type", length = 50)
  private String referralUserType;

  /**
   * Referral user ID
   */
  @Column(name = "referral_user_id")
  private Long referralUserId;

  // AGENT TRACKING
  /**
   * Whether order was initiated by an agent
   */
  @Column(name = "is_agent_initiated", nullable = false)
  @Builder.Default
  private final Boolean isAgentInitiated = false;
  /**
   * Agent who assisted with the order (if applicable)
   */
  @Column(name = "agent_id")
  private Long agentId;

  // CUSTOMER COMMUNICATION TRACKING
  /**
   * Whether customer was contacted about failed order to understand the reason
   */
  @Column(name = "failure_follow_up_complete")
  @Builder.Default
  private final Boolean failureFollowUpComplete = false;

  /**
   * When failure follow-up was completed
   */
  @Column(name = "failure_follow_up_completed_at")
  private LocalDateTime failureFollowUpCompletedAt;

  /**
   * Whether customer was contacted to verify the order and address
   */
  @Column(name = "order_verification_complete")
  @Builder.Default
  private final Boolean orderVerificationComplete = false;

  /**
   * When order verification was completed
   */
  @Column(name = "order_verification_completed_at")
  private LocalDateTime orderVerificationCompletedAt;

  // NOTES & COMMENTS
  /**
   * Customer notes/special instructions
   */
  @Column(name = "customer_notes", columnDefinition = "TEXT")
  private String customerNotes;

  /**
   * Internal admin/operator notes
   */
  @Column(name = "admin_notes", columnDefinition = "TEXT")
  private String adminNotes;

  // TRACKING INFORMATION (for order tracking system)
  /**
   * Estimated delivery window start date
   */
  @Column(name = "estimated_delivery_start")
  private java.time.LocalDate estimatedDeliveryStart;

  /**
   * Estimated delivery window end date
   */
  @Column(name = "estimated_delivery_end")
  private java.time.LocalDate estimatedDeliveryEnd;

  /**
   * Local/internal tracking number (for delivery within Ethiopia)
   */
  @Column(name = "local_tracking_number", length = 100)
  private String localTrackingNumber;

  /**
   * Shipping method description Example: "AliExpress Standard Shipping", "DHL
   * Express"
   */
  @Column(name = "shipping_method", length = 255)
  private String shippingMethod;

  // HELPER METHODS

  /**
   * Add an order item to this order
   */
  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
    this.totalItemsCount = orderItems.size();
  }

  /**
   * Remove an order item from this order
   */
  public void removeOrderItem(OrderItem orderItem) {
    orderItems.remove(orderItem);
    orderItem.setOrder(null);
    this.totalItemsCount = orderItems.size();
  }

  /**
   * Calculate and update total amount
   */
  public void recalculateTotalAmount() {
    OrderUtil.recalculateTotalAmount(this);
  }

  /**
   * Mark order as paid
   */
  public void markAsPaid(String paymentRef, LocalDateTime paidAt) {
    this.paymentStatus = PaymentStatus.SUCCESS;
    this.paymentReference = paymentRef;
    this.paymentConfirmedAt = paidAt;
  }

  /**
   * Cancel the order
   */
  public void cancel(String reason) {
    this.currentStage = OrderStage.CANCELLED;
    this.cancelledAt = LocalDateTime.now();
    this.cancellationReason = reason;
  }

  /**
   * Complete the order
   */
  public void complete() {
    this.currentStage = OrderStage.DELIVERED;
    this.completedAt = LocalDateTime.now();
  }
}
