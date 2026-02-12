package com.commercepal.apiservice.promotions.affiliate.commission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateCommissionRepository extends JpaRepository<AffiliateCommission, Long> {

    List<AffiliateCommission> findAllByAffiliate_IdOrderByCreatedAtDesc(Long affiliateId);

    boolean existsByOrderId(Long orderId);

    List<AffiliateCommission> findAllByAffiliate_IdAndCreatedAtBetween(Long affiliateId, Timestamp timestamp, Timestamp timestamp1);

    @Query("SELECT COALESCE(SUM(ac.commissionAmount), 0) FROM AffiliateCommission ac " + "WHERE ac.affiliate.id = :affiliateId AND ac.createdAt BETWEEN :start AND :end")
    BigDecimal sumCommissionForAffiliateInRange(@Param("affiliateId") Long affiliateId, @Param("start") Timestamp start, @Param("end") Timestamp end);

    @Query("SELECT COUNT(ac) FROM AffiliateCommission ac " + "WHERE ac.affiliate.id = :affiliateId AND ac.createdAt BETWEEN :start AND :end")
    int countTotalOrdersForAffiliateInRange(@Param("affiliateId") Long affiliateId, @Param("start") Timestamp start, @Param("end") Timestamp end);

    @Query("SELECT COALESCE(SUM(ac.commissionAmount), 0) FROM AffiliateCommission ac " + "WHERE ac.affiliate.id = :affiliateId")
    BigDecimal sumTotalEarningsByAffiliate(@Param("affiliateId") Long affiliateId);

    @Query("SELECT COUNT(ac) FROM AffiliateCommission ac " + "WHERE ac.affiliate.id = :affiliateId")
    int countTotalOrdersByAffiliate(@Param("affiliateId") Long affiliateId);

    @Query("SELECT SUM(c.commissionAmount) " + "FROM AffiliateCommission c " + "WHERE c.affiliate.id = :affiliateId " + "AND c.commissionType = :commissionType")
    BigDecimal sumCommissionAmountByAffiliateAndCommissionType(@Param("affiliateId") Long affiliateId, @Param("commissionType") CommissionType commissionType);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM AffiliateCommission c WHERE c.affiliate.id = :affiliateId AND c.paid = false")
    BigDecimal sumUnpaidCommissionAmountByAffiliate(@Param("affiliateId") Long affiliateId);


    @Query("""
                SELECT SUM(c.commissionAmount)
                FROM AffiliateCommission c
                WHERE c.affiliate.id = :affiliateId
                  AND c.createdAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumCommissionAmountByAffiliateAndDateRange(@Param("affiliateId") Long affiliateId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
                SELECT COUNT(c)
                FROM AffiliateCommission c
                WHERE c.affiliate.id = :affiliateId
                  AND c.commissionType = :commissionType
                  AND c.createdAt BETWEEN :startDate AND :endDate
            """)
    Long countByAffiliateAndCommissionTypeAndDateRange(@Param("affiliateId") Long affiliateId, @Param("commissionType") CommissionType commissionType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    @Query("""
                SELECT CAST(c.createdAt AS date), SUM(c.commissionAmount)
                FROM AffiliateCommission c
                WHERE c.affiliate.id = :affiliateId
                  AND c.commissionType = :commissionType
                  AND c.createdAt BETWEEN :startDate AND :endDate
                GROUP BY CAST(c.createdAt AS date)
                ORDER BY CAST(c.createdAt AS date)
            """)
    List<Object[]> findDailyEarningsByAffiliateAndDateRange(@Param("affiliateId") Long affiliateId, @Param("commissionType") CommissionType commissionType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    boolean existsByAffiliateIdAndOrderId(Long affiliateId, Long orderId);
    boolean existsByAffiliateIdAndCustomerId(Long affiliateId, Long customerId);

    @Query("SELECT COALESCE(MAX(ac.orderId), 0) FROM AffiliateCommission ac WHERE ac.affiliate.id = :affiliateId")
    Optional<Long> findMaxOrderIdByAffiliateId(@Param("affiliateId") Long affiliateId);

    @Query("SELECT COALESCE(MAX(ac.customerId), 0) FROM AffiliateCommission ac WHERE ac.affiliate.id = :affiliateId")
    Optional<Long> findMaxCustomerIdByAffiliateId(@Param("affiliateId") Long affiliateId);

    boolean existsByAffiliateIdAndCustomerIdAndCommissionType(Long affiliateId, Long customerId, CommissionType commissionType);
}


