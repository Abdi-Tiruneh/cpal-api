package com.commercepal.apiservice.promotions.affiliate.commission;

import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.promotions.affiliate.referral.AffiliateReferral;
import com.commercepal.apiservice.promotions.affiliate.referral.AffiliateReferralRepository;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.promotions.affiliate.user.AffiliateRepository;
import com.commercepal.apiservice.promotions.affiliate.common.dto.AffiliateEarningsOverviewResponse;
import com.commercepal.apiservice.promotions.affiliate.common.dto.AffiliateSummaryDTO;
import com.commercepal.apiservice.promotions.affiliate.common.dto.CommissionCreateRequest;
import com.commercepal.apiservice.promotions.affiliate.common.dto.DailyEarningsDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateCommissionService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliateReferralRepository affiliateReferralRepository;
    private final AffiliateCommissionRepository commissionRepository;

    public AffiliateCommission createCommission(CommissionCreateRequest request) {
        if (commissionRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalArgumentException("Commission for this order already exists.");
        }

        Affiliate affiliate = affiliateRepository
            .findById(request.getAffiliateId())
            .orElseThrow(() -> new ResourceNotFoundException("Affiliate not found"));

        BigDecimal commissionAmount;
        if (affiliate.getCommissionType() == Commission.PERCENTAGE) {
            BigDecimal rate = affiliate.getCommissionRate().divide(BigDecimal.valueOf(100));
            commissionAmount = request.getOrderAmount().multiply(rate);
        } else {
            commissionAmount = affiliate.getCommissionRate();
        }

        AffiliateCommission commission = new AffiliateCommission();
        commission.setAffiliate(affiliate);
        commission.setOrderId(request.getOrderId());
        commission.setCustomerId(request.getCustomerId());
        commission.setBaseAmount(request.getOrderAmount());
//        commission.setOrderAmount(request.getOrderAmount());
        commission.setCommissionAmount(commissionAmount);
        commission.setPaid(false);
//        commission.setCreatedAt(LocalDateTime.now());

        return commissionRepository.save(commission);
    }

    public List<AffiliateCommission> getByAffiliateId(Long affiliateId) {
        return commissionRepository.findAllByAffiliate_IdOrderByCreatedAtDesc(affiliateId);
    }

    public void markCommissionAsPaid(Long commissionId) {
        AffiliateCommission commission = commissionRepository
            .findById(commissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Commission not found"));

        commission.setPaid(true);
//        commission.setUpdatedAt(LocalDateTime.now());
        commissionRepository.save(commission);
    }

    public AffiliateEarningsOverviewResponse getLast30DaysOverview(Long affiliateId) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(29);

        List<DailyEarningsDTO> stats = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = fromDate.plusDays(i);
            stats.add(new DailyEarningsDTO() {{
                setDate(date);
            }});
        }

        // Fill commissions
        List<AffiliateCommission> commissions = commissionRepository.findAllByAffiliate_IdAndCreatedAtBetween(
            affiliateId, Timestamp.valueOf(fromDate.atStartOfDay()),
            Timestamp.valueOf(today.plusDays(1).atStartOfDay()));

        for (AffiliateCommission c : commissions) {
            LocalDate d = c.getCreatedAt().toLocalDateTime().toLocalDate();
            stats
                .stream()
                .filter(s -> s.getDate().equals(d))
                .findFirst()
                .ifPresent(s -> s.setCommissionAmount(
                    s.getCommissionAmount().add(c.getCommissionAmount())));
        }

        // Fill clicks
        List<AffiliateReferral> referrals = new ArrayList<>();
//                affiliateReferralRepository
//                .findAllByAffiliate_IdAndReferredAtBetween(
//                        affiliateId,
//                        Timestamp.valueOf(fromDate.atStartOfDay()),
//                        Timestamp.valueOf(today.plusDays(1).atStartOfDay())
//                );

//        for (AffiliateReferral r : referrals) {
//            LocalDate d = r.getReferredAt().toLocalDateTime().toLocalDate();
//            stats.stream().filter(s -> s.getDate().equals(d)).findFirst()
//                 .ifPresent(s -> s.setClickCount(s.getClickCount() + 1));
//        }

        AffiliateEarningsOverviewResponse response = new AffiliateEarningsOverviewResponse();
        response.setDailyStats(stats);
        return response;
    }

    public AffiliateSummaryDTO getMonthlySummary(Long affiliateId) {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);

        Timestamp start = Timestamp.valueOf(firstOfMonth.atStartOfDay());
        Timestamp end = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        BigDecimal earnings = commissionRepository.sumCommissionForAffiliateInRange(affiliateId,
                                                                                    start, end);
        int totalOrders = commissionRepository.countTotalOrdersForAffiliateInRange(affiliateId,
                                                                                   start, end);
//        int clicks = affiliateReferralRepository.countTotalClicksInRange(affiliateId, start, end);
        int clicks = 0;
        int converted = 0;
//        int converted = affiliateReferralRepository.countConvertedClicksInRange(affiliateId, start, end);

        BigDecimal conversionRate = BigDecimal.ZERO;
        if (clicks > 0) {
            conversionRate = BigDecimal.valueOf((converted * 100.0) / clicks)
                .setScale(2, RoundingMode.HALF_UP);
        }

        AffiliateSummaryDTO summary = new AffiliateSummaryDTO();
        summary.setTotalEarnings(earnings);
        summary.setTotalOrderedItems(totalOrders);
        summary.setTotalClicks(clicks);
        summary.setConversionRate(conversionRate);

        // optional: shipped orders if you track that separately
        summary.setTotalShippedItems(0);

        return summary;
    }

    public AffiliateSummaryDTO getLifetimeSummary(Long affiliateId) {
        BigDecimal earnings = commissionRepository.sumTotalEarningsByAffiliate(affiliateId);
        int totalOrders = commissionRepository.countTotalOrdersByAffiliate(affiliateId);
        int clicks = affiliateReferralRepository.countTotalClicksByAffiliate(affiliateId);
//        int converted = affiliateReferralRepository.countConvertedClicksByAffiliate(affiliateId);
        int converted = 0;

        BigDecimal conversionRate = BigDecimal.ZERO;
        if (clicks > 0) {
            conversionRate = BigDecimal.valueOf((converted * 100.0) / clicks)
                .setScale(2, RoundingMode.HALF_UP);
        }

        AffiliateSummaryDTO summary = new AffiliateSummaryDTO();
        summary.setTotalEarnings(earnings);
        summary.setTotalOrderedItems(totalOrders);
        summary.setTotalShippedItems(0); // future extension
        summary.setTotalClicks(clicks);
        summary.setConversionRate(conversionRate);

        return summary;
    }

    /**
     * Calculates the current balance (available balance) for an affiliate. Current balance = Sum of
     * all unpaid commissions (where paid = false)
     *
     * @param affiliateId The ID of the affiliate
     * @return The current balance as BigDecimal, rounded to 2 decimal places. Returns ZERO if no
     * unpaid commissions exist.
     * @throws ResourceNotFoundException if affiliate does not exist
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateCurrentBalance(Long affiliateId) {
        log.debug("Calculating current balance | AffiliateCommissionService | calculateCurrentBalance | affiliateId={}", affiliateId);

        // Validate affiliate exists
        if (!affiliateRepository.existsById(affiliateId)) {
            log.warn("Affiliate not found | AffiliateCommissionService | calculateCurrentBalance | affiliateId={}", affiliateId);
            throw new ResourceNotFoundException("Affiliate not found with ID: " + affiliateId);
        }

        // Sum all unpaid commissions (COALESCE ensures it never returns null)
        BigDecimal unpaidCommissions = commissionRepository.sumUnpaidCommissionAmountByAffiliate(
            affiliateId);

        // Round to 2 decimal places for currency precision
        BigDecimal currentBalance = unpaidCommissions.setScale(2, RoundingMode.HALF_UP);

        log.info("Current balance calculated | AffiliateCommissionService | calculateCurrentBalance | affiliateId={}, currentBalance={}", affiliateId, currentBalance);
        return currentBalance;
    }

}
