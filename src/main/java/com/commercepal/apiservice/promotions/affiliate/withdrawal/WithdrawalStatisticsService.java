package com.commercepal.apiservice.promotions.affiliate.withdrawal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalStatisticsService {

    private final AffiliateWithdrawalRepository withdrawalRepository;

    /**
     * Get comprehensive withdrawal statistics for an affiliate
     */
    public Map<String, Object> getAffiliateWithdrawalStats(Long affiliateId) {
        log.info("Fetching withdrawal statistics | WithdrawalStatisticsService | getAffiliateWithdrawalStats | affiliateId={}", affiliateId);

        Map<String, Object> stats = new HashMap<>();

        // Total withdrawals by status
        stats.put("totalPending", withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.PENDING));
        stats.put("totalApproved", withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.APPROVED));
        stats.put("totalRejected", withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.REJECTED));
        stats.put("totalPaid", withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.PAID));

        // Amount statistics
        Object[] pendingStats = withdrawalRepository.getWithdrawalStats(affiliateId, AffiliateWithdrawal.WithdrawalStatus.PENDING);
        Object[] approvedStats = withdrawalRepository.getWithdrawalStats(affiliateId, AffiliateWithdrawal.WithdrawalStatus.APPROVED);
        Object[] paidStats = withdrawalRepository.getWithdrawalStats(affiliateId, AffiliateWithdrawal.WithdrawalStatus.PAID);

        stats.put("pendingAmount", pendingStats != null && pendingStats.length > 1 ? pendingStats[1] : BigDecimal.ZERO);
        stats.put("approvedAmount", approvedStats != null && approvedStats.length > 1 ? approvedStats[1] : BigDecimal.ZERO);
        stats.put("paidAmount", paidStats != null && paidStats.length > 1 ? paidStats[1] : BigDecimal.ZERO);

        // Calculate total amount
        BigDecimal totalAmount = ((BigDecimal) stats.get("pendingAmount"))
                .add((BigDecimal) stats.get("approvedAmount"))
                .add((BigDecimal) stats.get("paidAmount"));
        stats.put("totalAmount", totalAmount);

        // Recent activity (last 30 days)
        Timestamp thirtyDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
        stats.put("recentWithdrawals", getRecentWithdrawalStats(affiliateId, thirtyDaysAgo));

        return stats;
    }

    /**
     * Get system-wide withdrawal statistics (admin)
     */
    public Map<String, Object> getSystemWithdrawalStats() {
        log.info("Fetching system-wide withdrawal statistics | WithdrawalStatisticsService | getSystemWithdrawalStats");

        Map<String, Object> stats = new HashMap<>();

        // Total withdrawals by status
        stats.put("totalPending", withdrawalRepository.countByStatus(AffiliateWithdrawal.WithdrawalStatus.PENDING));
        stats.put("totalApproved", withdrawalRepository.countByStatus(AffiliateWithdrawal.WithdrawalStatus.APPROVED));
        stats.put("totalRejected", withdrawalRepository.countByStatus(AffiliateWithdrawal.WithdrawalStatus.REJECTED));
        stats.put("totalPaid", withdrawalRepository.countByStatus(AffiliateWithdrawal.WithdrawalStatus.PAID));

        // Amount statistics
        Object[] pendingStats = withdrawalRepository.getWithdrawalStats(AffiliateWithdrawal.WithdrawalStatus.PENDING);
        Object[] approvedStats = withdrawalRepository.getWithdrawalStats(AffiliateWithdrawal.WithdrawalStatus.APPROVED);
        Object[] paidStats = withdrawalRepository.getWithdrawalStats(AffiliateWithdrawal.WithdrawalStatus.PAID);

        stats.put("pendingAmount", pendingStats != null && pendingStats.length > 1 ? pendingStats[1] : BigDecimal.ZERO);
        stats.put("approvedAmount", approvedStats != null && approvedStats.length > 1 ? approvedStats[1] : BigDecimal.ZERO);
        stats.put("paidAmount", paidStats != null && paidStats.length > 1 ? paidStats[1] : BigDecimal.ZERO);

        // Calculate total amount
        BigDecimal totalAmount = ((BigDecimal) stats.get("pendingAmount"))
                .add((BigDecimal) stats.get("approvedAmount"))
                .add((BigDecimal) stats.get("paidAmount"));
        stats.put("totalAmount", totalAmount);

        // Recent activity (last 30 days)
        Timestamp thirtyDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
        stats.put("recentWithdrawals", getRecentWithdrawalStats(null, thirtyDaysAgo));

        return stats;
    }

    /**
     * Get withdrawal trends over time
     */
    public Map<String, Object> getWithdrawalTrends(Long affiliateId, int days) {
        log.info("Fetching withdrawal trends | WithdrawalStatisticsService | getWithdrawalTrends | affiliateId={}, days={}", affiliateId, days);

        Map<String, Object> trends = new HashMap<>();
        Timestamp startDate = Timestamp.valueOf(LocalDateTime.now().minusDays(days));

        // This would require additional repository methods to get daily/weekly/monthly trends
        // For now, return basic structure
        trends.put("period", days + " days");
        trends.put("startDate", startDate);
        trends.put("endDate", Timestamp.valueOf(LocalDateTime.now()));

        return trends;
    }

    /**
     * Get recent withdrawal statistics
     */
    private Map<String, Object> getRecentWithdrawalStats(Long affiliateId, Timestamp since) {
        Map<String, Object> recentStats = new HashMap<>();

        // This would require additional repository methods to get recent stats
        // For now, return basic structure
        recentStats.put("since", since);
        recentStats.put("count", 0); // Placeholder
        recentStats.put("amount", BigDecimal.ZERO); // Placeholder

        return recentStats;
    }

    /**
     * Get withdrawal performance metrics
     */
    public Map<String, Object> getWithdrawalPerformanceMetrics(Long affiliateId) {
        log.info("Fetching withdrawal performance metrics | WithdrawalStatisticsService | getWithdrawalPerformanceMetrics | affiliateId={}", affiliateId);

        Map<String, Object> metrics = new HashMap<>();

        // Calculate approval rate
        long totalRequests = withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.PENDING) +
                            withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.APPROVED) +
                            withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.REJECTED) +
                            withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.PAID);

        long approvedRequests = withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.APPROVED) +
                              withdrawalRepository.countByAffiliateIdAndStatus(affiliateId, AffiliateWithdrawal.WithdrawalStatus.PAID);

        double approvalRate = totalRequests > 0 ? (double) approvedRequests / totalRequests * 100 : 0;
        metrics.put("approvalRate", approvalRate);

        // Calculate average processing time (this would require additional logic)
        metrics.put("averageProcessingTime", "N/A"); // Placeholder

        // Calculate withdrawal frequency
        metrics.put("withdrawalFrequency", "N/A"); // Placeholder

        return metrics;
    }
}
