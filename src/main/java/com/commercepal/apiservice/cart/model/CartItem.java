package com.commercepal.apiservice.cart.model;

import com.commercepal.apiservice.shared.BaseAuditEntity;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shopping cart item entity with price tracking and stock validation.
 * <p>
 * Features: - Product variant support (configId, configuration) - Price tracking for price drop
 * notifications - Stock status monitoring - Provider tracking - Cached product information for
 * performance
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
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
    @Index(name = "idx_cart_item_product", columnList = "product_id"),
    @Index(name = "idx_cart_item_cart_product", columnList = "cart_id,product_id,config_id")
})
public class CartItem extends BaseAuditEntity {

  /**
   * Parent cart
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
  private Cart cart;

  /**
   * Product ID from provider
   */
  @Column(name = "product_id", length = 100, nullable = false)
  private String productId;

  /**
   * Cached product name for display
   */
  @Column(name = "product_name", length = 255)
  private String productName;

  /**
   * Cached product image URL
   */
  @Column(name = "product_image_url", length = 500)
  private String productImageUrl;

  /**
   * Configuration/variant ID (nullable for simple products)
   */
  @Column(name = "config_id", length = 100)
  private String configId;
  /**
   * Quantity
   */
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  /**
   * Currency
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
   * Unit price when added to cart
   */
  @Column(name = "unit_price", precision = 19, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  /**
   * Price when first added (for price drop detection)
   */
  @Column(name = "price_when_added", precision = 19, scale = 2, nullable = false)
  private BigDecimal priceWhenAdded;

  /**
   * Current price (updated periodically)
   */
  @Column(name = "current_price", precision = 19, scale = 2)
  private BigDecimal currentPrice;

  /**
   * Base price in USD (from provider)
   */
  @Column(name = "base_price_in_usd", precision = 19, scale = 2)
  private BigDecimal basePriceInUSD;

  /**
   * Exchange rate used for conversion (USD to Target Currency)
   */
  @Column(name = "exchange_rate", precision = 19, scale = 6)
  private BigDecimal exchangeRate;

  /**
   * Product provider
   */
  @Column(name = "provider", length = 50)
  private String provider;

  /**
   * Stock status
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "stock_status", length = 20)
  private StockStatus stockStatus;

  /**
   * Whether product is currently available
   */
  @Column(name = "is_available", nullable = false)
  private Boolean isAvailable;

  /**
   * Timestamp when item was added to cart
   */
  @Column(name = "added_at", nullable = false)
  private LocalDateTime addedAt;

  /**
   * Last price check timestamp
   */
  @Column(name = "last_price_check_at")
  private LocalDateTime lastPriceCheckAt;

  /**
   * Flag indicating price dropped since adding
   */
  @Column(name = "price_dropped", nullable = false)
  private Boolean priceDropped;

  /**
   * Price drop notification sent flag
   */
  @Column(name = "price_drop_notified", nullable = false)
  private Boolean priceDropNotified;

  /**
   * Calculate subtotal for this item
   */
  public BigDecimal getSubtotal() {
    if (unitPrice == null || quantity == null) {
      return BigDecimal.ZERO;
    }
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * Calculate price difference (negative means price drop)
   */
  public BigDecimal getPriceDifference() {
    if (currentPrice == null || priceWhenAdded == null) {
      return BigDecimal.ZERO;
    }
    return currentPrice.subtract(priceWhenAdded);
  }

  /**
   * Check if price has dropped
   */
  public boolean hasPriceDropped() {
    return getPriceDifference().compareTo(BigDecimal.ZERO) < 0;
  }

  /**
   * Get savings amount if price dropped
   */
  public BigDecimal getSavingsAmount() {
    if (!hasPriceDropped()) {
      return BigDecimal.ZERO;
    }
    return getPriceDifference().abs().multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * Check if this item matches product and config
   */
  public boolean matches(String productId, String configId) {
    boolean productMatches = this.productId != null && this.productId.equals(productId);
    if (!productMatches) {
      return false;
    }

    // Both null or both equal
    if (this.configId == null && configId == null) {
      return true;
    }

    return this.configId != null && this.configId.equals(configId);
  }

  @PrePersist
  protected void onCreate() {
    if (addedAt == null) {
      addedAt = LocalDateTime.now();
    }
    if (isAvailable == null) {
      isAvailable = true;
    }
    if (stockStatus == null) {
      stockStatus = StockStatus.UNKNOWN;
    }
    if (priceDropped == null) {
      priceDropped = false;
    }
    if (priceDropNotified == null) {
      priceDropNotified = false;
    }
    if (priceWhenAdded == null && unitPrice != null) {
      priceWhenAdded = unitPrice;
    }
    if (currentPrice == null && unitPrice != null) {
      currentPrice = unitPrice;
    }
  }
}
