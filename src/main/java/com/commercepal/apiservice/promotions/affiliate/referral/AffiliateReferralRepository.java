package com.commercepal.apiservice.promotions.affiliate.referral;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateReferralRepository extends JpaRepository<AffiliateReferral, Long> {

    // Single row per affiliate+session
    Optional<AffiliateReferral> findByAffiliateIdAndSessionId(Long affiliateId, String sessionId);

    Optional<AffiliateReferral> findByAffiliateIdAndIpAddress(Long affiliateId, String ipAddress);

    Optional<AffiliateReferral> findByOrderRef(String orderRef);

    // For last-touch attribution when we only know the session
    Optional<AffiliateReferral> findTopBySessionIdOrderByLastSeenAtDesc(String sessionId);

//    Optional<AffiliateReferral> findFirstBySessionIdAndConvertedFalseOrderByReferredAtDesc(String sessionId);

//    List<AffiliateReferral> findAllByAffiliate_IdOrderByReferredAtDesc(Long affiliateId);

//    List<AffiliateReferral> findAllByAffiliate_IdAndReferredAtBetween(Long affiliateId, Timestamp timestamp, Timestamp timestamp1);

//    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar " + "WHERE ar.affiliate.id = :affiliateId AND ar.referredAt BETWEEN :start AND :end")
//    int countTotalClicksInRange(@Param("affiliateId") Long affiliateId, @Param("start") Timestamp start, @Param("end") Timestamp end);

//    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar " + "WHERE ar.affiliate.id = :affiliateId AND ar.converted = true AND ar.referredAt BETWEEN :start AND :end")
//    int countConvertedClicksInRange(@Param("affiliateId") Long affiliateId, @Param("start") Timestamp start, @Param("end") Timestamp end);

    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar WHERE ar.affiliate.id = :affiliateId")
    int countTotalClicksByAffiliate(@Param("affiliateId") Long affiliateId);

//    @Query("SELECT COUNT(ar) FROM AffiliateReferral ar " + "WHERE ar.affiliate.id = :affiliateId AND ar.converted = true")
//    int countConvertedClicksByAffiliate(@Param("affiliateId") Long affiliateId);

//    @Query("SELECT DATE(r.firstSeenAt), SUM(r.viewCount) " + "FROM AffiliateReferral r " + "WHERE r.affiliate.id = :affiliateId " + "AND r.firstSeenAt BETWEEN :startDate AND :endDate " + "GROUP BY DATE(r.firstSeenAt)")
//    List<Object[]> findDailyClicksByAffiliateAndDateRange(@Param("affiliateId") Long affiliateId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT CAST(r.firstSeenAt AS date), SUM(r.viewCount)
            FROM AffiliateReferral r
            WHERE r.affiliate.id = :affiliateId
              AND r.firstSeenAt BETWEEN :startDate AND :endDate
            GROUP BY CAST(r.firstSeenAt AS date)
            ORDER BY CAST(r.firstSeenAt AS date)
        """)
    List<Object[]> findDailyClicksByAffiliateAndDateRange(@Param("affiliateId") Long affiliateId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);


    @Query("""
            SELECT SUM(r.viewCount)
            FROM AffiliateReferral r
            WHERE r.affiliate.id = :affiliateId
              AND r.firstSeenAt BETWEEN :startDate AND :endDate
        """)
    Long sumClicksByAffiliateAndDateRange(@Param("affiliateId") Long affiliateId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(r.viewCount) " + "FROM AffiliateReferral r "
        + "WHERE r.affiliate.id = :affiliateId")
    Long sumTotalClicksByAffiliate(@Param("affiliateId") Long affiliateId);

    Page<AffiliateReferral> findAllByAffiliateId(Long affiliateId, Pageable pageable);

}


