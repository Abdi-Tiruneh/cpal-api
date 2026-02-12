package com.commercepal.apiservice.promotions.affiliate.dashboard;

import com.commercepal.apiservice.promotions.affiliate.referral.AffiliateReferralRepository;
import com.commercepal.apiservice.promotions.affiliate.commission.CommissionType;
import com.commercepal.apiservice.promotions.affiliate.commission.AffiliateCommissionRepository;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import com.commercepal.apiservice.utils.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/affiliate/dashboard")
@RequiredArgsConstructor
public class AffiliateDashboardController {

    private final AffiliateCommissionRepository commissionRepository;
    private final AffiliateReferralRepository referralRepository;
    private final CurrentUserService currentUserService;

    @GetMapping("/current-month-summary")
    public ResponseEntity<ResponseWrapper<CurrentMonthSummaryResponse>> getCurrentMonthSummary() {
        Affiliate affiliate = currentUserService.getCurrentAffiliate();
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        BigDecimal totalEarnings = commissionRepository.sumCommissionAmountByAffiliateAndDateRange(
            affiliate.getId(),
            startDateTime, endDateTime);
        BigDecimal safeTotalEarnings = totalEarnings != null ? totalEarnings : BigDecimal.ZERO;

        Long totalOrders = commissionRepository.countByAffiliateAndCommissionTypeAndDateRange(
            affiliate.getId(),
            CommissionType.ORDER, startDateTime, endDateTime);
        long safeTotalOrders = totalOrders != null ? totalOrders : 0L;

        Long totalClicks = referralRepository.sumClicksByAffiliateAndDateRange(affiliate.getId(),
                                                                               startDateTime,
                                                                               endDateTime);
        long safeTotalClicks = totalClicks != null ? totalClicks : 0L;

        Long totalSignups = commissionRepository.countByAffiliateAndCommissionTypeAndDateRange(
            affiliate.getId(),
            CommissionType.SIGNUP, startDateTime, endDateTime);
        long safeTotalSignups = totalSignups != null ? totalSignups : 0L;

        // Calculate conversion rate
        double conversionRate =
            safeTotalClicks > 0 ? ((double) (safeTotalOrders + safeTotalSignups) / totalClicks)
                * 100 : 0.0;
        String conversionRateStr = String.format("%.2f%%", conversionRate);

        CurrentMonthSummaryResponse currentMonthSummaryResponse = new CurrentMonthSummaryResponse(
            safeTotalEarnings,
            safeTotalOrders, safeTotalClicks, conversionRateStr);

        return ResponseWrapper.success(currentMonthSummaryResponse);
    }


    @GetMapping("/lifetime-summary")
    public ResponseEntity<ResponseWrapper<LifetimeSummaryResponse>> getLifetimeSummary() {
        Affiliate affiliate = currentUserService.getCurrentAffiliate();

        BigDecimal totalCommissions = commissionRepository.sumCommissionAmountByAffiliateAndCommissionType(
            affiliate.getId(), CommissionType.ORDER);
        BigDecimal safeTotalCommissions =
            totalCommissions != null ? totalCommissions : BigDecimal.ZERO;

        BigDecimal totalBounties = commissionRepository.sumCommissionAmountByAffiliateAndCommissionType(
            affiliate.getId(), CommissionType.SIGNUP);
        BigDecimal safeTotalBounties = totalBounties != null ? totalBounties : BigDecimal.ZERO;

        Long totalClicks = referralRepository.sumTotalClicksByAffiliate(affiliate.getId());
        long safeTotalClicks = totalClicks != null ? totalClicks : 0L;

        LifetimeSummaryResponse lifetimeSummaryResponse = new LifetimeSummaryResponse(
            safeTotalCommissions,
            safeTotalBounties, safeTotalClicks);

        return ResponseWrapper.success(lifetimeSummaryResponse);
    }


    @GetMapping("/earnings-overview")
    public ResponseEntity<ResponseWrapper<EarningsOverviewResponse>> getEarningsOverview(
        @RequestParam(defaultValue = "30") int days
    ) {
        Affiliate affiliate = currentUserService.getCurrentAffiliate();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Convert to LocalDateTime for timestamp comparison
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Get daily commission data for ORDER type (commissions)
        List<DailyEarning> dailyCommissions = commissionRepository
            .findDailyEarningsByAffiliateAndDateRange(affiliate.getId(), CommissionType.ORDER,
                                                      startDateTime,
                                                      endDateTime)
            .stream()
            .map(this::mapToDailyEarning)
            .toList();

        // Get daily commission data for SIGNUP type (bounties)
        List<DailyEarning> dailyBounties = commissionRepository
            .findDailyEarningsByAffiliateAndDateRange(affiliate.getId(), CommissionType.SIGNUP,
                                                      startDateTime,
                                                      endDateTime)
            .stream()
            .map(this::mapToDailyEarning)
            .toList();

        // Get daily click data
        List<DailyClick> dailyClicks = referralRepository
            .findDailyClicksByAffiliateAndDateRange(affiliate.getId(), startDateTime, endDateTime)
            .stream()
            .map(this::mapToDailyClick)
            .toList();

        // Combine data
        List<EarningsOverviewData> overviewData = IntStream.range(0, days).mapToObj(i -> {
            LocalDate date = startDate.plusDays(i);

            // Get commissions for this date
            BigDecimal commissions = dailyCommissions
                .stream()
                .filter(e -> e.getDate().equals(date))
                .findFirst()
                .map(DailyEarning::getAmount)
                .orElse(BigDecimal.ZERO);

            // Get bounties for this date
            BigDecimal bounties = dailyBounties
                .stream()
                .filter(e -> e.getDate().equals(date))
                .findFirst()
                .map(DailyEarning::getAmount)
                .orElse(BigDecimal.ZERO);

            // Get clicks for this date
            long clicks = dailyClicks
                .stream()
                .filter(c -> c.getDate().equals(date))
                .findFirst()
                .map(DailyClick::getClicks)
                .orElse(0L);

            return new EarningsOverviewData(date, commissions, bounties, clicks);
        }).toList();

        EarningsOverviewResponse earningsOverviewResponse = new EarningsOverviewResponse(
            overviewData);
        return ResponseWrapper.success(earningsOverviewResponse);
    }

    private DailyEarning mapToDailyEarning(Object[] result) {
        return new DailyEarning(((Date) result[0]).toLocalDate(), (BigDecimal) result[1]);
    }

    private DailyClick mapToDailyClick(Object[] result) {
        return new DailyClick(((Date) result[0]).toLocalDate(), (Long) result[1]);
    }
}

