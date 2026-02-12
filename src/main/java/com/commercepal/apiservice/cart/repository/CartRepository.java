package com.commercepal.apiservice.cart.repository;

import com.commercepal.apiservice.cart.model.Cart;
import com.commercepal.apiservice.cart.model.CartStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Cart entity with advanced query methods.
 * <p>
 * Supports: - Guest and authenticated cart management - Abandoned cart detection - Cart analytics
 * and reporting - Status-based queries
 *
 * @author CommercePal
 * @version 1.0
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, JpaSpecificationExecutor<Cart> {

  /**
   * Find active cart by customer ID
   */
  @Query("SELECT c FROM Cart c WHERE c.customer.id = :customerId AND c.status = 'ACTIVE'")
  Optional<Cart> findActiveCartByCustomerId(@Param("customerId") Long customerId);

  /**
   * Find cart by customer ID (any status)
   */
  Optional<Cart> findByCustomerId(Long customerId);

  /**
   * Find active cart by session ID (for guest carts)
   */
  @Query("SELECT c FROM Cart c WHERE c.sessionId = :sessionId AND c.status = 'ACTIVE'")
  Optional<Cart> findActiveCartBySessionId(@Param("sessionId") String sessionId);

  /**
   * Find cart by session ID (any status)
   */
  Optional<Cart> findBySessionId(String sessionId);

  /**
   * Find all carts for a customer
   */
  List<Cart> findAllByCustomerId(Long customerId);

  /**
   * Find abandoned carts (inactive for specified duration)
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' " +
      "AND c.lastActivityAt < :cutoffTime " +
      "AND c.totalItems > 0")
  List<Cart> findAbandonedCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

  /**
   * Find abandoned carts that haven't been notified
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'ABANDONED' " +
      "AND c.abandonedNotificationSent = false " +
      "AND c.totalItems > 0")
  List<Cart> findAbandonedCartsNotNotified();

  /**
   * Find abandoned carts ready for another notification
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'ABANDONED' " +
      "AND c.lastNotificationAt < :notificationCutoff " +
      "AND c.totalItems > 0")
  List<Cart> findAbandonedCartsForReminder(
      @Param("notificationCutoff") LocalDateTime notificationCutoff);

  /**
   * Find expired carts (very old, ready for cleanup)
   */
  @Query("SELECT c FROM Cart c WHERE c.lastActivityAt < :expirationTime " +
      "AND c.status NOT IN ('CONVERTED', 'EXPIRED')")
  List<Cart> findExpiredCarts(@Param("expirationTime") LocalDateTime expirationTime);

  /**
   * Find high-value abandoned carts for priority recovery
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'ABANDONED' " +
      "AND c.estimatedTotal >= :minValue " +
      "AND c.totalItems > 0 " +
      "ORDER BY c.estimatedTotal DESC")
  List<Cart> findHighValueAbandonedCarts(@Param("minValue") BigDecimal minValue);

  /**
   * Count active carts by customer
   */
  @Query("SELECT COUNT(c) FROM Cart c WHERE c.customer.id = :customerId AND c.status = 'ACTIVE'")
  long countActiveCartsByCustomer(@Param("customerId") Long customerId);

  /**
   * Count all active carts
   */
  @Query("SELECT COUNT(c) FROM Cart c WHERE c.status = 'ACTIVE'")
  long countTotalActiveCarts();

  /**
   * Count total items across all active carts
   */
  @Query("SELECT COALESCE(SUM(c.totalItems), 0) FROM Cart c WHERE c.status = 'ACTIVE'")
  long countTotalActiveCartItems();

  /**
   * Get total value of active carts
   */
  @Query("SELECT COALESCE(SUM(c.estimatedTotal), 0) FROM Cart c WHERE c.status = 'ACTIVE'")
  BigDecimal getTotalActiveCartValue();

  /**
   * Find carts by status
   */
  List<Cart> findByStatus(CartStatus status);

  /**
   * Find carts created within date range
   */
  @Query("SELECT c FROM Cart c WHERE c.createdAt BETWEEN :startDate AND :endDate")
  List<Cart> findCartsCreatedBetween(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Find converted carts (for analytics)
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'CONVERTED' " +
      "AND c.convertedAt BETWEEN :startDate AND :endDate")
  List<Cart> findConvertedCartsBetween(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Calculate cart abandonment rate
   */
  @Query("SELECT " +
      "CAST(COUNT(CASE WHEN c.status = 'ABANDONED' THEN 1 END) AS double) / " +
      "CAST(COUNT(*) AS double) * 100 " +
      "FROM Cart c WHERE c.totalItems > 0")
  Double getAbandonmentRate();

  /**
   * Calculate cart conversion rate
   */
  @Query("SELECT " +
      "CAST(COUNT(CASE WHEN c.status = 'CONVERTED' THEN 1 END) AS double) / " +
      "CAST(COUNT(*) AS double) * 100 " +
      "FROM Cart c WHERE c.totalItems > 0")
  Double getConversionRate();

  /**
   * Get average cart value
   */
  @Query("SELECT AVG(c.estimatedTotal) FROM Cart c WHERE c.status = 'ACTIVE' AND c.totalItems > 0")
  BigDecimal getAverageCartValue();

  /**
   * Delete carts by status (for cleanup)
   */
  void deleteByStatus(CartStatus status);

  /**
   * Check if customer has active cart
   */
  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
      "FROM Cart c WHERE c.customer.id = :customerId AND c.status = 'ACTIVE'")
  boolean hasActiveCart(@Param("customerId") Long customerId);

  /**
   * Find active carts modified between two timestamps
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' " +
      "AND c.updatedAt BETWEEN :start AND :end")
  List<Cart> findActiveCartsModifiedBetween(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
