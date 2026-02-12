package com.commercepal.apiservice.orders.core.repository;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * OrderRepository
 * <p>
 * Repository for Order entity with comprehensive query methods for order
 * management. Extends
 * JpaSpecificationExecutor for complex dynamic queries.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find all orders for a customer
     */
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Find all orders for a customer with specific stage
     */
    Page<Order> findByCustomerIdAndCurrentStage(Long customerId, OrderStage stage, Pageable pageable);

    /**
     * Find all orders for a customer with payment status
     */
    Page<Order> findByCustomerIdAndPaymentStatus(Long customerId, PaymentStatus paymentStatus,
            Pageable pageable);

    /**
     * Find orders by current stage
     */
    Page<Order> findByCurrentStage(OrderStage stage, Pageable pageable);

    /**
     * Find orders by payment status
     */
    Page<Order> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    /**
     * Find orders created within date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate")
    Page<Order> findByOrderedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find orders for a customer within date range
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.orderedAt BETWEEN :startDate AND :endDate")
    Page<Order> findByCustomerIdAndOrderedAtBetween(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find pending payment orders older than specified date
     */
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'PENDING' AND o.orderedAt < :cutoffDate")
    List<Order> findPendingPaymentOrdersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find orders by agent
     */
    Page<Order> findByAgentId(Long agentId, Pageable pageable);

    /**
     * Find agent-initiated orders
     */
    Page<Order> findByIsAgentInitiatedTrue(Pageable pageable);

    /**
     * Count orders by customer
     */
    long countByCustomerId(Long customerId);

    /**
     * Count orders by customer and stage
     */
    long countByCustomerIdAndCurrentStage(Long customerId, OrderStage stage);

    /**
     * Count orders by payment status
     */
    long countByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Get total order value for customer
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.customer.id = :customerId AND o.paymentStatus = 'PAID'")
    Double getTotalOrderValueByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find orders with failed payment that haven't been contacted
     */
    // @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'FAILED' AND
    // o.failedOrderContacted = false")
    // List<Order> findFailedOrdersNotContacted();

    /**
     * Find delivered orders that haven't been contacted for feedback
     */
    // @Query("SELECT o FROM Order o WHERE o.currentStage = 'DELIVERED' AND
    // o.successOrderContacted = false AND o.completedAt < :cutoffDate")
    // List<Order> findDeliveredOrdersNotContacted(@Param("cutoffDate")
    // LocalDateTime cutoffDate);

    /**
     * Find orders by delivery address
     */
    Page<Order> findByDeliveryAddressId(Long deliveryAddressId, Pageable pageable);

    /**
     * Find orders with refund in progress
     */
    @Query("SELECT o FROM Order o WHERE o.refundStatus IN ('REQUESTED', 'PROCESSING', 'APPROVED')")
    Page<Order> findOrdersWithRefundInProgress(Pageable pageable);

    /**
     * Search orders by order number (partial match)
     */
    @Query("SELECT o FROM Order o WHERE LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchByOrderNumber(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find orders by business ID
     */
    // Page<Order> findByBusinessId(Long businessId, Pageable pageable); //
    // BusinessId not in Order entity explicitly shown earlier

    /**
     * Find high priority orders
     */
    @Query("SELECT o FROM Order o WHERE o.priority IN ('HIGH', 'URGENT', 'CRITICAL') AND o.currentStage NOT IN ('DELIVERED', 'CANCELLED')")
    List<Order> findHighPriorityActiveOrders();

    // =========================================================================
    // ORDER TRACKING SYSTEM METHODS
    // =========================================================================

    /**
     * Find orders by customer and multiple stages (for category filtering)
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.currentStage IN :stages")
    Page<Order> findByCustomerIdAndCurrentStageIn(
            @Param("customerId") Long customerId,
            @Param("stages") java.util.Set<OrderStage> stages,
            Pageable pageable);

    /**
     * Count orders by customer and multiple stages
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId AND o.currentStage IN :stages")
    long countByCustomerIdAndCurrentStageIn(
            @Param("customerId") Long customerId,
            @Param("stages") java.util.Set<OrderStage> stages);

    /**
     * Search orders by customer and order number (partial match)
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Order> findByCustomerIdAndOrderNumberContainingIgnoreCase(
            @Param("customerId") Long customerId,
            @Param("searchQuery") String searchQuery,
            Pageable pageable);

    // =========================================================================
    // ANALYTICS & REPORTS
    // =========================================================================

    /**
     * Calculate total revenue between dates
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate AND o.paymentStatus = 'SUCCESS'")
    java.math.BigDecimal sumTotalRevenueBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count orders between dates
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate")
    long countOrdersBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get order count by stage between dates
     */
    @Query("SELECT o.currentStage, COUNT(o) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate GROUP BY o.currentStage")
    List<Object[]> countOrdersByStageBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get daily sales stats
     */
    @Query("SELECT function('DATE', o.orderedAt) as date, COUNT(o), SUM(o.totalAmount) FROM Order o WHERE o.orderedAt BETWEEN :startDate AND :endDate GROUP BY function('DATE', o.orderedAt) ORDER BY date ASC")
    List<Object[]> findDailySalesStats(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Check if address has orders in specific stages
     */
    boolean existsByDeliveryAddressIdAndCurrentStageIn(Long deliveryAddressId,
            java.util.Collection<OrderStage> stages);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId,
            Pageable pageable);

    Optional<Order> findByOrderNumberAndCustomerId(
            String orderNumber,
            Long customerId);
}
