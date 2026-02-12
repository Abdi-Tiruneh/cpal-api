package com.commercepal.apiservice.promotions.affiliate.withdrawal;

import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import java.sql.Timestamp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateWithdrawalRepository extends JpaRepository<AffiliateWithdrawal, Long> {

    /**
     * Find all withdrawals for a specific affiliate with pagination
     */
    Page<AffiliateWithdrawal> findByAffiliate(Affiliate affiliate,
                                              Pageable pageable);

    /**
     * Find withdrawals by affiliate and status with pagination
     */
    Page<AffiliateWithdrawal> findByAffiliateAndStatus(
        Affiliate affiliate,
        AffiliateWithdrawal.WithdrawalStatus status,
        Pageable pageable);

    /**
     * Find withdrawals by status with pagination
     */
    Page<AffiliateWithdrawal> findByStatus(
        AffiliateWithdrawal.WithdrawalStatus status,
        Pageable pageable);

    /**
     * Find withdrawals by payment method with pagination
     */
    Page<AffiliateWithdrawal> findByPaymentMethodOrderByRequestedAtDesc(
        AffiliateWithdrawal.PaymentMethod paymentMethod,
        Pageable pageable);

    /**
     * Find withdrawals by affiliate and payment method with pagination
     */
    Page<AffiliateWithdrawal> findByAffiliateAndPaymentMethodOrderByRequestedAtDesc(
        Affiliate affiliate,
        AffiliateWithdrawal.PaymentMethod paymentMethod,
        Pageable pageable);

    /**
     * Find withdrawals by date range with pagination
     */
    @Query("SELECT aw FROM AffiliateWithdrawal aw WHERE aw.requestedAt BETWEEN :startDate AND :endDate")
    Page<AffiliateWithdrawal> findByRequestedAtBetween(
        @Param("startDate") java.sql.Timestamp startDate,
        @Param("endDate") java.sql.Timestamp endDate,
        Pageable pageable);

    /**
     * Find withdrawals by affiliate and date range with pagination
     */
    @Query("SELECT aw FROM AffiliateWithdrawal aw WHERE aw.affiliate = :affiliate AND aw.requestedAt BETWEEN :startDate AND :endDate")
    Page<AffiliateWithdrawal> findByAffiliateAndRequestedAtBetween(
        @Param("affiliate") Affiliate affiliate,
        @Param("startDate") java.sql.Timestamp startDate,
        @Param("endDate") java.sql.Timestamp endDate,
        Pageable pageable);

    /**
     * Find withdrawals by amount range with pagination
     */
    @Query("SELECT aw FROM AffiliateWithdrawal aw WHERE aw.amount BETWEEN :minAmount AND :maxAmo")
    Page<AffiliateWithdrawal> findByAmountBetween(
        @Param("minAmount") java.math.BigDecimal minAmount,
        @Param("maxAmount") java.math.BigDecimal maxAmount,
        Pageable pageable);

    /**
     * Find withdrawals by affiliate and amount range with pagination
     */
    @Query("SELECT aw FROM AffiliateWithdrawal aw WHERE aw.affiliate = :affiliate AND aw.amount BETWEEN :minAmount AND :maxAmo")
    Page<AffiliateWithdrawal> findByAffiliateAndAmountBetween(
        @Param("affiliate") Affiliate affiliate,
        @Param("minAmount") java.math.BigDecimal minAmount,
        @Param("maxAmount") java.math.BigDecimal maxAmount,
        Pageable pageable);

    /**
     * Count withdrawals by status for a specific affiliate
     */
    long countByAffiliateAndStatus(Affiliate affiliate,
                                   AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Count withdrawals by status
     */
    long countByStatus(AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Find total amount of withdrawals by status for a specific affiliate
     */
    @Query("SELECT COALESCE(SUM(aw.amount), 0) FROM AffiliateWithdrawal aw WHERE aw.affiliate = :affiliate AND aw.status = :status")
    java.math.BigDecimal getTotalAmountByAffiliateAndStatus(
        @Param("affiliate") Affiliate affiliate,
        @Param("status") AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Find total amount of withdrawals by status
     */
    @Query("SELECT COALESCE(SUM(aw.amount), 0) FROM AffiliateWithdrawal aw WHERE aw.status = :status")
    java.math.BigDecimal getTotalAmountByStatus(AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Find pending withdrawals for processing
     */
    List<AffiliateWithdrawal> findByStatusOrderByRequestedAtAsc(
        AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Check if affiliate has withdrawal request today
     */
    List<AffiliateWithdrawal> findByAffiliateAndStatusAndRequestedAtBetween(
        Affiliate affiliate,
        AffiliateWithdrawal.WithdrawalStatus status,
        Timestamp start,
        Timestamp end
    );

    /**
     * Find latest withdrawal for an affiliate
     */
    Optional<AffiliateWithdrawal> findFirstByAffiliateOrderByRequestedAtDesc(Affiliate affiliate);

    /**
     * Count withdrawals by affiliate
     */
    long countByAffiliate(Affiliate affiliate);

    /**
     * Find total amount of withdrawals by affiliate
     */
    @Query("SELECT COALESCE(SUM(aw.amount), 0) FROM AffiliateWithdrawal aw WHERE aw.affiliate = :affiliate")
    java.math.BigDecimal getTotalAmountByAffiliate(@Param("affiliate") Affiliate affiliate);

    /**
     * Find total amount of all withdrawals
     */
    @Query("SELECT COALESCE(SUM(aw.amount), 0) FROM AffiliateWithdrawal aw")
    java.math.BigDecimal getTotalAmount();

    /**
     * Find withdrawals by affiliate and status and date range
     */
    @Query("SELECT aw FROM AffiliateWithdrawal aw WHERE aw.affiliate = :affiliate AND aw.status = :status AND aw.requestedAt BETWEEN :startDate AND :endDate")
    Page<AffiliateWithdrawal> findByAffiliateAndStatusAndRequestedAtBetween(
        @Param("affiliate") Affiliate affiliate,
        @Param("status") AffiliateWithdrawal.WithdrawalStatus status,
        @Param("startDate") java.sql.Timestamp startDate,
        @Param("endDate") java.sql.Timestamp endDate,
        Pageable pageable);

//ORDER BY aw.requestedAt DESC

    /**
     * Find withdrawals by status and date range
     */
    @Query("SELECT aw FROM AffiliateWithdrawal aw WHERE aw.status = :status AND aw.requestedAt BETWEEN :startDate AND :endDate")
    Page<AffiliateWithdrawal> findByStatusAndRequestedAtBetween(
        @Param("status") AffiliateWithdrawal.WithdrawalStatus status,
        @Param("startDate") java.sql.Timestamp startDate,
        @Param("endDate") java.sql.Timestamp endDate,
        Pageable pageable);


    /**
     * Find all withdrawals for a specific affiliate, sorted by requested date descending
     */
    Page<AffiliateWithdrawal> findByAffiliateIdOrderByRequestedAtDesc(Long affiliateId,
                                                                      Pageable pageable);

    /**
     * Find all withdrawals with optional filtering, sorted by requested date descending
     */
    @Query("SELECT w FROM AffiliateWithdrawal w WHERE " +
        "(:affiliateId IS NULL OR w.affiliate.id = :affiliateId) AND " +
        "(:status IS NULL OR w.status = :status) AND " +
        "(:paymentMethod IS NULL OR w.paymentMethod = :paymentMethod) AND " +
        "(:startDate IS NULL OR w.requestedAt >= :startDate) AND " +
        "(:endDate IS NULL OR w.requestedAt <= :endDate)")
    Page<AffiliateWithdrawal> findWithFilters(
        @Param("affiliateId") Long affiliateId,
        @Param("status") AffiliateWithdrawal.WithdrawalStatus status,
        @Param("paymentMethod") AffiliateWithdrawal.PaymentMethod paymentMethod,
        @Param("startDate") Timestamp startDate,
        @Param("endDate") Timestamp endDate,
        Pageable pageable);

    /**
     * Find withdrawals by status
     */
//    Page<AffiliateWithdrawal> findByStatusOrderByRequestedAtDesc(
//        AffiliateWithdrawal.WithdrawalStatus status,
//        Pageable pageable);

    /**
     * Find all withdrawals sorted by requested date descending
     */
//    Page<AffiliateWithdrawal> findAllByOrderByRequestedAtDesc(Pageable pageable);

    /**
     * Find withdrawals by affiliate and status
     */
    List<AffiliateWithdrawal> findByAffiliateIdAndStatusOrderByRequestedAtDesc(
        Long affiliateId,
        AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Count withdrawals by affiliate and status
     */
    long countByAffiliateIdAndStatus(Long affiliateId, AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Count withdrawals by status
     */
//    long countByStatus(AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Find pending withdrawals for processing
     */
//    List<AffiliateWithdrawal> findByStatusOrderByRequestedAtAsc(AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Check if affiliate has pending withdrawal
     */
    boolean existsByAffiliateIdAndStatus(Long affiliateId,
                                         AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Find withdrawal by ID with affiliate details
     */
    @Query("SELECT w FROM AffiliateWithdrawal w LEFT JOIN FETCH w.affiliate WHERE w.id = :id")
    Optional<AffiliateWithdrawal> findByIdWithAffiliate(@Param("id") Long id);

    /**
     * Get withdrawal statistics for an affiliate
     */
    @Query("SELECT COUNT(w), COALESCE(SUM(w.amount), 0) FROM AffiliateWithdrawal w " +
        "WHERE w.affiliate.id = :affiliateId AND w.status = :status")
    Object[] getWithdrawalStats(@Param("affiliateId") Long affiliateId,
                                @Param("status") AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Get withdrawal statistics for all affiliates
     */
    @Query("SELECT COUNT(w), COALESCE(SUM(w.amount), 0) FROM AffiliateWithdrawal w " +
        "WHERE w.status = :status")
    Object[] getWithdrawalStats(@Param("status") AffiliateWithdrawal.WithdrawalStatus status);

    /**
     * Find recent withdrawals for dashboard
     */
    @Query("SELECT w FROM AffiliateWithdrawal w ORDER BY w.requestedAt DESC")
    List<AffiliateWithdrawal> findRecentWithdrawals(Pageable pageable);
}
