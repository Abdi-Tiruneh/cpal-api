package com.commercepal.apiservice.cart.repository;

import com.commercepal.apiservice.cart.model.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for CartItem entity with item-specific queries.
 * <p>
 * Supports: - Item management within carts - Price drop detection - Stock validation - Product
 * analytics
 *
 * @author CommercePal
 * @version 1.0
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  /**
   * Find all items in a cart
   */
  List<CartItem> findByCartId(Long cartId);

  /**
   * Find specific item in cart by product ID
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId")
  List<CartItem> findByCartIdAndProductId(
      @Param("cartId") Long cartId,
      @Param("productId") String productId);
//
//    /**
//     * Find exact item match (product + config)
//     */
//    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId " +
//            "AND ci.productId = :productId " +
//            "AND (ci.configId = :configId OR (ci.configId IS NULL AND :configId IS NULL))")
//    Optional<CartItem> findByCartIdAndProductIdAndConfigId(
//            @Param("cartId") Long cartId,
//            @Param("productId") String productId,
//            @Param("configId") String configId);

  @Query("""
      SELECT ci FROM CartItem ci
      WHERE ci.cart.id = :cartId
      AND ci.productId = :productId
      AND ci.configId = :configId
      """)
  Optional<CartItem> findByCartIdAndProductIdAndConfigId(
      @Param("cartId") Long cartId,
      @Param("productId") String productId,
      @Param("configId") String configId);


  /**
   * Find item match where configId is NULL
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId " +
      "AND ci.productId = :productId " +
      "AND ci.configId IS NULL")
  Optional<CartItem> findByCartIdAndProductIdAndConfigIdIsNull(
      @Param("cartId") Long cartId,
      @Param("productId") String productId);

  /**
   * Count items in cart
   */
  long countByCartId(Long cartId);

  /**
   * Delete all items in cart
   */
  void deleteByCartId(Long cartId);

  /**
   * Find items with price drops
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.priceDropped = true " +
      "AND ci.priceDropNotified = false " +
      "AND ci.cart.status = 'ACTIVE'")
  List<CartItem> findItemsWithPriceDrops();

  /**
   * Find items that are out of stock
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.stockStatus = 'OUT_OF_STOCK' " +
      "AND ci.cart.status = 'ACTIVE'")
  List<CartItem> findOutOfStockItems();

  /**
   * Find unavailable items
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.isAvailable = false " +
      "AND ci.cart.status = 'ACTIVE'")
  List<CartItem> findUnavailableItems();

  /**
   * Find items needing price check (not checked recently)
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.status = 'ACTIVE' " +
      "AND (ci.lastPriceCheckAt IS NULL OR ci.lastPriceCheckAt < :checkCutoff)")
  List<CartItem> findItemsNeedingPriceCheck(
      @Param("checkCutoff") java.time.LocalDateTime checkCutoff);

  /**
   * Find most frequently added products
   */
  @Query("SELECT ci.productId, COUNT(ci) as addCount FROM CartItem ci " +
      "GROUP BY ci.productId ORDER BY addCount DESC")
  List<Object[]> findMostAddedProducts();

  /**
   * Find products frequently in abandoned carts
   */
  @Query("SELECT ci.productId, COUNT(ci) as abandonCount FROM CartItem ci " +
      "WHERE ci.cart.status = 'ABANDONED' " +
      "GROUP BY ci.productId ORDER BY abandonCount DESC")
  List<Object[]> findMostAbandonedProducts();

  /**
   * Get total quantity of specific product in active carts
   */
  @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci " +
      "WHERE ci.productId = :productId AND ci.cart.status = 'ACTIVE'")
  Long getTotalQuantityInActiveCarts(@Param("productId") String productId);

  /**
   * Find items by provider
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.provider = :provider " +
      "AND ci.cart.status = 'ACTIVE'")
  List<CartItem> findActiveItemsByProvider(
      @Param("provider") com.commercepal.apiservice.shared.enums.Provider provider);

  /**
   * Delete items older than specified date
   */
  void deleteByAddedAtBefore(java.time.LocalDateTime cutoffDate);
}
