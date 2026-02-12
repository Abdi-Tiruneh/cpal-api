package com.commercepal.apiservice.cart.model;

import com.commercepal.apiservice.shared.BaseAuditEntity;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.customer.Customer;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
 * Tracks customer shopping sessions with comprehensive features: - Persistent cart storage across
 * sessions - Guest cart support via session IDs - Abandoned cart detection and recovery - Price
 * tracking and notifications - Multi-currency support - Cart status management
 *
 * @author CommercePal
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_customer", columnList = "customer_id"),
    @Index(name = "idx_cart_session", columnList = "session_id"),
    @Index(name = "idx_cart_status_activity", columnList = "status,last_activity_at"),
    @Index(name = "idx_cart_status", columnList = "status")
})
public class Cart extends BaseAuditEntity {

  /**
   * Associated customer (nullable for guest carts)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_cart_customer"))
  private Customer customer;

  /**
   * Session ID for guest carts
   */
  @Column(name = "session_id", length = 255)
  private String sessionId;

  /**
   * Cart status
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private CartStatus status;

  /**
   * Cart currency
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "currency", length = 3, nullable = false)
  private SupportedCurrency currency;

  /**
   * Country context for pricing
   */
  @Column(name = "country", length = 4)
  private String country;

  /**
   * Total number of items in cart
   */
  @Column(name = "total_items", nullable = false)
  private Integer totalItems;

  /**
   * Cached subtotal for performance
   */
  @Column(name = "subtotal", precision = 19, scale = 2)
  private BigDecimal subtotal;

  /**
   * Estimated total including fees
   */
  @Column(name = "estimated_total", precision = 19, scale = 2)
  private BigDecimal estimatedTotal;

  /**
   * Last activity timestamp for abandoned cart detection
   */
  @Column(name = "last_activity_at")
  private LocalDateTime lastActivityAt;

  /**
   * Timestamp when cart was abandoned
   */
  @Column(name = "abandoned_at")
  private LocalDateTime abandonedAt;

  /**
   * Timestamp when cart was converted to order
   */
  @Column(name = "converted_at")
  private LocalDateTime convertedAt;

  /**
   * Order number if converted
   */
  @Column(name = "order_number", length = 50)
  private String orderNumber;

  /**
   * Flag indicating if abandoned cart notification sent
   */
  @Column(name = "abandoned_notification_sent", nullable = false)
  private Boolean abandonedNotificationSent;

  /**
   * Timestamp of last abandoned cart notification
   */
  @Column(name = "last_notification_at")
  private LocalDateTime lastNotificationAt;

  /**
   * Cart items
   */
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @org.hibernate.annotations.SQLRestriction("is_deleted = 0")
  @Builder.Default
  private final List<CartItem> items = new ArrayList<>();

  /**
   * Add item to cart
   */
  public void addItem(CartItem item) {
    items.add(item);
    item.setCart(this);
    updateLastActivity();
  }

  /**
   * Remove item from cart
   */
  public void removeItem(CartItem item) {
    items.remove(item);
    item.setCart(null);
    updateLastActivity();
  }

  /**
   * Clear all items
   */
  public void clearItems() {
    items.clear();
    updateLastActivity();
  }

  /**
   * Update last activity timestamp
   */
  public void updateLastActivity() {
    this.lastActivityAt = LocalDateTime.now();
    if (this.status == CartStatus.ABANDONED) {
      this.status = CartStatus.ACTIVE;
      this.abandonedAt = null;
    }
  }

  /**
   * Mark cart as abandoned
   */
  public void markAsAbandoned() {
    this.status = CartStatus.ABANDONED;
    this.abandonedAt = LocalDateTime.now();
  }

  /**
   * Mark cart as converted
   */
  public void markAsConverted(String orderNumber) {
    this.status = CartStatus.CONVERTED;
    this.convertedAt = LocalDateTime.now();
    this.orderNumber = orderNumber;
  }

  /**
   * Mark cart as expired
   */
  public void markAsExpired() {
    this.status = CartStatus.EXPIRED;
  }

  /**
   * Check if cart is active
   */
  public boolean isActive() {
    return this.status == CartStatus.ACTIVE;
  }

  /**
   * Check if cart belongs to guest user
   */
  public boolean isGuestCart() {
    return this.customer == null && this.sessionId != null;
  }

  /**
   * Check if cart belongs to authenticated user
   */
  public boolean isAuthenticatedCart() {
    return this.customer != null;
  }

  @PrePersist
  protected void onCreate() {
    if (status == null) {
      status = CartStatus.ACTIVE;
    }
    if (totalItems == null) {
      totalItems = 0;
    }
    if (abandonedNotificationSent == null) {
      abandonedNotificationSent = false;
    }
    if (lastActivityAt == null) {
      lastActivityAt = LocalDateTime.now();
    }
    if (subtotal == null) {
      subtotal = BigDecimal.ZERO;
    }
    if (estimatedTotal == null) {
      estimatedTotal = BigDecimal.ZERO;
    }
  }
}
