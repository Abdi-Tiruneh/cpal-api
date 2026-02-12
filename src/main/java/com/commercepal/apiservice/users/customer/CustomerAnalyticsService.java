package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.ChannelCategoryBreakdown;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.ChannelMetric;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.ChannelMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.CountryMetric;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.DailyDataPoint;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.DateRange;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.EngagementMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.GeographicMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.GrowthMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.MonthlyGrowth;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.OverviewMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.RegionMetric;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.SecurityMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.StatusBreakdown;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.StatusMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.TimeSeriesData;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.VerificationFunnel;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.VerificationMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.WeeklyDataPoint;
import com.commercepal.apiservice.users.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for comprehensive customer analytics.
 * <p>
 * Provides Amazon-level analytics including overview metrics, growth trends, geographic
 * distribution, engagement metrics, and security insights.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomerAnalyticsService {

  private static final Map<String, List<SupportedCountry>> REGION_COUNTRIES = Map.of(
      "Africa", List.of(SupportedCountry.ETHIOPIA, SupportedCountry.KENYA, SupportedCountry.SOMALIA,
          SupportedCountry.UNITED_ARAB_EMIRATES),
      "Middle East", List.of(SupportedCountry.UNITED_ARAB_EMIRATES),
      "Europe", List.of(SupportedCountry.INTERNATIONAL),
      "North America", List.of(SupportedCountry.INTERNATIONAL),
      "Asia Pacific", List.of(SupportedCountry.INTERNATIONAL),
      "Latin America", List.of(SupportedCountry.INTERNATIONAL)
  );
  private final CustomerRepository customerRepository;
  private final EntityManager entityManager;

  /**
   * Generate comprehensive customer analytics for the specified date range.
   *
   * @param startDate start date of the analysis period
   * @param endDate   end date of the analysis period
   * @return comprehensive analytics response
   */
  public CustomerAnalyticsResponse generateAnalytics(LocalDate startDate, LocalDate endDate) {
    log.info("[CUSTOMER-ANALYTICS] Generating analytics for period: {} to {}", startDate, endDate);
    long startTime = System.currentTimeMillis();

    // Calculate comparison period (same duration, immediately before)
    int periodDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    LocalDate comparisonStartDate = startDate.minusDays(periodDays);
    LocalDate comparisonEndDate = startDate.minusDays(1);

    DateRange dateRange = DateRange.builder()
        .startDate(startDate)
        .endDate(endDate)
        .periodDays(periodDays)
        .comparisonStartDate(comparisonStartDate)
        .comparisonEndDate(comparisonEndDate)
        .build();

    // Generate all metrics
    OverviewMetrics overview = generateOverviewMetrics(startDate, endDate);
    GrowthMetrics growth = generateGrowthMetrics(startDate, endDate, comparisonStartDate,
        comparisonEndDate, periodDays);
    GeographicMetrics geographic = generateGeographicMetrics();
    ChannelMetrics channels = generateChannelMetrics();
    EngagementMetrics engagement = generateEngagementMetrics();
    VerificationMetrics verification = generateVerificationMetrics();
    SecurityMetrics security = generateSecurityMetrics(startDate, endDate);
    StatusMetrics status = generateStatusMetrics();
    TimeSeriesData timeSeries = generateTimeSeriesData(startDate, endDate);

    long duration = System.currentTimeMillis() - startTime;
    log.info("[CUSTOMER-ANALYTICS] Analytics generated | durationMs={}", duration);

    return CustomerAnalyticsResponse.builder()
        .generatedAt(LocalDateTime.now())
        .dateRange(dateRange)
        .overview(overview)
        .growth(growth)
        .geographic(geographic)
        .channels(channels)
        .engagement(engagement)
        .verification(verification)
        .security(security)
        .status(status)
        .timeSeries(timeSeries)
        .build();
  }

  /**
   * Generate quick overview analytics (lightweight version).
   */
  public OverviewMetrics generateQuickOverview() {
    LocalDate today = LocalDate.now();
    LocalDate thirtyDaysAgo = today.minusDays(30);
    return generateOverviewMetrics(thirtyDaysAgo, today);
  }

  // ========== OVERVIEW METRICS ==========

  private OverviewMetrics generateOverviewMetrics(LocalDate startDate, LocalDate endDate) {
    log.debug("[CUSTOMER-ANALYTICS] Generating overview metrics");

    long totalCustomers = customerRepository.count();
    long activeCustomers = countByStatus(UserStatus.ACTIVE);
    long newCustomers = countNewCustomers(startDate, endDate);
    long churnedCustomers = countChurnedCustomers(startDate, endDate);

    long emailVerified = countEmailVerified();
    long phoneVerified = countPhoneVerified();
    long fullyVerified = countFullyVerified();

    double retentionRate = totalCustomers > 0
        ? ((double) (totalCustomers - churnedCustomers) / totalCustomers) * 100 : 0;
    double churnRate = 100 - retentionRate;
    long netGrowth = newCustomers - churnedCustomers;
    double netGrowthRate = totalCustomers > 0 ? ((double) netGrowth / totalCustomers) * 100 : 0;

    int periodDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    double avgNewCustomersPerDay = periodDays > 0 ? (double) newCustomers / periodDays : 0;

    return OverviewMetrics.builder()
        .totalCustomers(totalCustomers)
        .activeCustomers(activeCustomers)
        .newCustomers(newCustomers)
        .churnedCustomers(churnedCustomers)
        .retentionRate(round(retentionRate, 2))
        .churnRate(round(churnRate, 2))
        .netGrowth(netGrowth)
        .netGrowthRate(round(netGrowthRate, 2))
        .avgNewCustomersPerDay(round(avgNewCustomersPerDay, 2))
        .emailVerifiedCustomers(emailVerified)
        .phoneVerifiedCustomers(phoneVerified)
        .fullyVerifiedCustomers(fullyVerified)
        .build();
  }

  // ========== GROWTH METRICS ==========

  private GrowthMetrics generateGrowthMetrics(LocalDate startDate, LocalDate endDate,
      LocalDate comparisonStart, LocalDate comparisonEnd, int periodDays) {
    log.debug("[CUSTOMER-ANALYTICS] Generating growth metrics");

    long newInPeriod = countNewCustomers(startDate, endDate);
    long newInComparison = countNewCustomers(comparisonStart, comparisonEnd);

    double dailyAvg = periodDays > 0 ? (double) newInPeriod / periodDays : 0;
    double weeklyAvg = dailyAvg * 7;
    double monthlyAvg = dailyAvg * 30;

    double growthVsPrevious = newInComparison > 0
        ? ((double) (newInPeriod - newInComparison) / newInComparison) * 100 : 0;

    // YoY comparison
    LocalDate yearAgoStart = startDate.minusYears(1);
    LocalDate yearAgoEnd = endDate.minusYears(1);
    long newYearAgo = countNewCustomers(yearAgoStart, yearAgoEnd);
    double growthYoY = newYearAgo > 0
        ? ((double) (newInPeriod - newYearAgo) / newYearAgo) * 100 : 0;

    // Peak and lowest days
    Map<LocalDate, Long> dailyCounts = getDailyRegistrationCounts(startDate, endDate);
    LocalDate peakDay = null;
    long peakCount = 0;
    LocalDate lowestDay = null;
    long lowestCount = Long.MAX_VALUE;

    for (Map.Entry<LocalDate, Long> entry : dailyCounts.entrySet()) {
      if (entry.getValue() > peakCount) {
        peakCount = entry.getValue();
        peakDay = entry.getKey();
      }
      if (entry.getValue() < lowestCount) {
        lowestCount = entry.getValue();
        lowestDay = entry.getKey();
      }
    }

    if (lowestCount == Long.MAX_VALUE) {
      lowestCount = 0;
    }

    // Growth trend determination
    String growthTrend = determineGrowthTrend(dailyCounts);

    // Monthly breakdown
    List<MonthlyGrowth> monthlyBreakdown = generateMonthlyBreakdown(startDate, endDate);

    // Projection
    long totalCustomers = customerRepository.count();
    int daysRemaining = (int) ChronoUnit.DAYS.between(LocalDate.now(), endDate.withDayOfYear(365));
    long projectedYearEnd = totalCustomers + (long) (dailyAvg * Math.max(0, daysRemaining));

    return GrowthMetrics.builder()
        .dailyAverage(round(dailyAvg, 2))
        .weeklyAverage(round(weeklyAvg, 2))
        .monthlyAverage(round(monthlyAvg, 2))
        .growthRateVsPreviousPeriod(round(growthVsPrevious, 2))
        .growthRateYoY(round(growthYoY, 2))
        .peakDay(peakDay)
        .peakDayCount(peakCount)
        .lowestDay(lowestDay)
        .lowestDayCount(lowestCount)
        .growthTrend(growthTrend)
        .projectedYearEndTotal(projectedYearEnd)
        .monthlyBreakdown(monthlyBreakdown)
        .build();
  }

  // ========== GEOGRAPHIC METRICS ==========

  private GeographicMetrics generateGeographicMetrics() {
    log.debug("[CUSTOMER-ANALYTICS] Generating geographic metrics");

    List<Object[]> countryData = getCountryDistribution();
    long totalCustomers = customerRepository.count();

    List<CountryMetric> countryMetrics = new ArrayList<>();
    int rank = 1;

    for (Object[] row : countryData) {
      SupportedCountry country = (SupportedCountry) row[0];
      Long count = (Long) row[1];

      if (country != null) {
        double percentage = totalCustomers > 0 ? ((double) count / totalCustomers) * 100 : 0;

        countryMetrics.add(CountryMetric.builder()
            .country(country)
            .countryName(country.getCode())
            .customerCount(count)
            .percentage(round(percentage, 2))
            .growthRate(0) // Would need historical data
            .rank(rank++)
            .build());
      }
    }

    // Top 10 countries
    List<CountryMetric> topCountries = countryMetrics.stream()
        .limit(10)
        .collect(Collectors.toList());

    // Geographic concentration (top 5)
    double concentration = countryMetrics.stream()
        .limit(5)
        .mapToDouble(CountryMetric::percentage)
        .sum();

    // Regional breakdown
    List<RegionMetric> regions = generateRegionalBreakdown(countryMetrics, totalCustomers);

    return GeographicMetrics.builder()
        .totalCountries(countryMetrics.size())
        .topCountry(countryMetrics.isEmpty() ? null : countryMetrics.get(0))
        .topCountries(topCountries)
        .regions(regions)
        .geographicConcentration(round(concentration, 2))
        .build();
  }

  // ========== CHANNEL METRICS ==========

  private ChannelMetrics generateChannelMetrics() {
    log.debug("[CUSTOMER-ANALYTICS] Generating channel metrics");

    List<Object[]> channelData = getChannelDistribution();
    long totalCustomers = customerRepository.count();

    List<ChannelMetric> channelMetrics = new ArrayList<>();
    long mobileCount = 0;
    long webCount = 0;
    long apiCount = 0;

    for (Object[] row : channelData) {
      Channel channel = (Channel) row[0];
      Long count = (Long) row[1];

      if (channel != null) {
        double percentage = totalCustomers > 0 ? ((double) count / totalCustomers) * 100 : 0;

        channelMetrics.add(ChannelMetric.builder()
            .channel(channel)
            .channelName(channel.getDisplayName())
            .customerCount(count)
            .percentage(round(percentage, 2))
            .growthRate(0) // Would need historical data
            .build());

        // Category breakdown
        if (channel.isMobile()) {
          mobileCount += count;
        } else if (channel.isWeb()) {
          webCount += count;
        } else if (channel.isApi()) {
          apiCount += count;
        }
      }
    }

    // Sort by count descending
    channelMetrics.sort((a, b) -> Long.compare(b.customerCount(), a.customerCount()));

    Channel topChannel = channelMetrics.isEmpty() ? null : channelMetrics.get(0).channel();

    ChannelCategoryBreakdown categoryBreakdown = ChannelCategoryBreakdown.builder()
        .mobileCustomers(mobileCount)
        .mobilePercentage(
            totalCustomers > 0 ? round((double) mobileCount / totalCustomers * 100, 2) : 0)
        .webCustomers(webCount)
        .webPercentage(totalCustomers > 0 ? round((double) webCount / totalCustomers * 100, 2) : 0)
        .apiCustomers(apiCount)
        .apiPercentage(totalCustomers > 0 ? round((double) apiCount / totalCustomers * 100, 2) : 0)
        .build();

    return ChannelMetrics.builder()
        .channels(channelMetrics)
        .topChannel(topChannel)
        .categoryBreakdown(categoryBreakdown)
        .build();
  }

  // ========== ENGAGEMENT METRICS ==========

  private EngagementMetrics generateEngagementMetrics() {
    log.debug("[CUSTOMER-ANALYTICS] Generating engagement metrics");

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneDayAgo = now.minusDays(1);
    LocalDateTime oneWeekAgo = now.minusDays(7);
    LocalDateTime oneMonthAgo = now.minusDays(30);
    LocalDateTime threeMonthsAgo = now.minusDays(90);

    long activeToday = countActiveUsers(oneDayAgo);
    long activeThisWeek = countActiveUsers(oneWeekAgo);
    long activeThisMonth = countActiveUsers(oneMonthAgo);
    long activeThisQuarter = countActiveUsers(threeMonthsAgo);
    long neverSignedIn = countNeverSignedIn();
    long mfaEnabled = countMfaEnabled();

    long totalCustomers = customerRepository.count();
    double mfaAdoptionRate = totalCustomers > 0 ? ((double) mfaEnabled / totalCustomers) * 100 : 0;

    // Stickiness (DAU/MAU)
    double stickiness = activeThisMonth > 0 ? (double) activeToday / activeThisMonth : 0;

    return EngagementMetrics.builder()
        .activeToday(activeToday)
        .activeThisWeek(activeThisWeek)
        .activeThisMonth(activeThisMonth)
        .activeThisQuarter(activeThisQuarter)
        .neverSignedIn(neverSignedIn)
        .dauAverage(activeToday)
        .wauAverage(activeThisWeek)
        .mauAverage(activeThisMonth)
        .stickiness(round(stickiness, 3))
        .avgDaysBetweenSignIns(0) // Would need more complex calculation
        .mfaEnabledCustomers(mfaEnabled)
        .mfaAdoptionRate(round(mfaAdoptionRate, 2))
        .build();
  }

  // ========== VERIFICATION METRICS ==========

  private VerificationMetrics generateVerificationMetrics() {
    log.debug("[CUSTOMER-ANALYTICS] Generating verification metrics");

    long totalCustomers = customerRepository.count();
    long emailVerified = countEmailVerified();
    long phoneVerified = countPhoneVerified();
    long fullyVerified = countFullyVerified();

    double emailRate = totalCustomers > 0 ? ((double) emailVerified / totalCustomers) * 100 : 0;
    double phoneRate = totalCustomers > 0 ? ((double) phoneVerified / totalCustomers) * 100 : 0;
    double fullRate = totalCustomers > 0 ? ((double) fullyVerified / totalCustomers) * 100 : 0;

    long pendingEmail = totalCustomers - emailVerified;
    long pendingPhone = totalCustomers - phoneVerified;

    double emailDropOff = totalCustomers > 0
        ? ((double) (totalCustomers - emailVerified) / totalCustomers) * 100 : 0;
    double phoneDropOff = totalCustomers > 0
        ? ((double) (totalCustomers - phoneVerified) / totalCustomers) * 100 : 0;

    VerificationFunnel funnel = VerificationFunnel.builder()
        .registered(totalCustomers)
        .emailVerified(emailVerified)
        .phoneVerified(phoneVerified)
        .fullyVerified(fullyVerified)
        .emailDropOffRate(round(emailDropOff, 2))
        .phoneDropOffRate(round(phoneDropOff, 2))
        .build();

    return VerificationMetrics.builder()
        .emailVerificationRate(round(emailRate, 2))
        .phoneVerificationRate(round(phoneRate, 2))
        .fullVerificationRate(round(fullRate, 2))
        .pendingEmailVerification(pendingEmail)
        .pendingPhoneVerification(pendingPhone)
        .avgEmailVerificationTime(0) // Would need timestamp data
        .avgPhoneVerificationTime(0)
        .funnel(funnel)
        .build();
  }

  // ========== SECURITY METRICS ==========

  private SecurityMetrics generateSecurityMetrics(LocalDate startDate, LocalDate endDate) {
    log.debug("[CUSTOMER-ANALYTICS] Generating security metrics");

    long lockedAccounts = countLockedAccounts();
    long accountsWithFailedAttempts = countAccountsWithFailedAttempts();
    long requiresPasswordChange = countRequiresPasswordChange();

    return SecurityMetrics.builder()
        .lockedAccounts(lockedAccounts)
        .accountsLockedInPeriod(0) // Would need historical data
        .accountsWithFailedAttempts(accountsWithFailedAttempts)
        .totalFailedAttempts(0) // Would need aggregation
        .avgFailedAttemptsPerLock(0)
        .passwordResetRequests(0) // Would need tracking
        .successfulPasswordResets(0)
        .passwordResetSuccessRate(0)
        .requiresPasswordChange(requiresPasswordChange)
        .suspiciousActivityFlags(0)
        .build();
  }

  // ========== STATUS METRICS ==========

  private StatusMetrics generateStatusMetrics() {
    log.debug("[CUSTOMER-ANALYTICS] Generating status metrics");

    long totalCustomers = customerRepository.count();
    List<StatusBreakdown> breakdown = new ArrayList<>();

    for (UserStatus status : UserStatus.values()) {
      long count = countByStatus(status);
      double percentage = totalCustomers > 0 ? ((double) count / totalCustomers) * 100 : 0;

      breakdown.add(StatusBreakdown.builder()
          .status(status)
          .count(count)
          .percentage(round(percentage, 2))
          .build());
    }

    // Sort by count descending
    breakdown.sort((a, b) -> Long.compare(b.count(), a.count()));

    // Calculate rates
    Map<UserStatus, Double> rates = breakdown.stream()
        .collect(Collectors.toMap(StatusBreakdown::status, StatusBreakdown::percentage));

    return StatusMetrics.builder()
        .breakdown(breakdown)
        .activeRate(rates.getOrDefault(UserStatus.ACTIVE, 0.0))
        .inactiveRate(rates.getOrDefault(UserStatus.INACTIVE, 0.0))
        .suspendedRate(rates.getOrDefault(UserStatus.SUSPENDED, 0.0))
        .lockedRate(rates.getOrDefault(UserStatus.LOCKED, 0.0))
        .pendingVerificationRate(rates.getOrDefault(UserStatus.PENDING_VERIFICATION, 0.0))
        .deactivatedRate(rates.getOrDefault(UserStatus.DEACTIVATED, 0.0))
        .build();
  }

  // ========== TIME SERIES DATA ==========

  private TimeSeriesData generateTimeSeriesData(LocalDate startDate, LocalDate endDate) {
    log.debug("[CUSTOMER-ANALYTICS] Generating time series data");

    Map<LocalDate, Long> dailyRegistrations = getDailyRegistrationCounts(startDate, endDate);

    List<DailyDataPoint> registrationPoints = dailyRegistrations.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> DailyDataPoint.builder().date(e.getKey()).value(e.getValue()).build())
        .collect(Collectors.toList());

    // Cumulative growth
    List<DailyDataPoint> cumulativePoints = new ArrayList<>();
    long cumulative = countCustomersBeforeDate(startDate);
    for (DailyDataPoint point : registrationPoints) {
      cumulative += point.value();
      cumulativePoints.add(DailyDataPoint.builder()
          .date(point.date())
          .value(cumulative)
          .build());
    }

    // Weekly summary
    List<WeeklyDataPoint> weeklySummary = generateWeeklySummary(startDate, endDate);

    return TimeSeriesData.builder()
        .dailyRegistrations(registrationPoints)
        .dailyActiveUsers(List.of()) // Would need sign-in tracking
        .cumulativeGrowth(cumulativePoints)
        .weeklySummary(weeklySummary)
        .build();
  }

  // ========== HELPER METHODS ==========

  private long countByStatus(UserStatus status) {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred WHERE cred.status = :status AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class)
        .setParameter("status", status)
        .getSingleResult();
  }

  private long countNewCustomers(LocalDate startDate, LocalDate endDate) {
    String jpql = "SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :start AND c.createdAt < :end AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class)
        .setParameter("start", startDate.atStartOfDay())
        .setParameter("end", endDate.plusDays(1).atStartOfDay())
        .getSingleResult();
  }

  private long countChurnedCustomers(LocalDate startDate, LocalDate endDate) {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.status IN (:statuses) AND c.updatedAt >= :start AND c.updatedAt < :end";
    return entityManager.createQuery(jpql, Long.class)
        .setParameter("statuses", List.of(UserStatus.INACTIVE, UserStatus.DEACTIVATED))
        .setParameter("start", startDate.atStartOfDay())
        .setParameter("end", endDate.plusDays(1).atStartOfDay())
        .getSingleResult();
  }

  private long countEmailVerified() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred WHERE cred.isEmailVerified = true AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class).getSingleResult();
  }

  private long countPhoneVerified() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred WHERE cred.isPhoneVerified = true AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class).getSingleResult();
  }

  private long countFullyVerified() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.isEmailVerified = true AND cred.isPhoneVerified = true AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class).getSingleResult();
  }

  private long countActiveUsers(LocalDateTime since) {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.lastSignedInAt >= :since AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class)
        .setParameter("since", since)
        .getSingleResult();
  }

  private long countNeverSignedIn() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.lastSignedInAt IS NULL AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class).getSingleResult();
  }

  private long countMfaEnabled() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.mfaEnabled = true AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class).getSingleResult();
  }

  private long countLockedAccounts() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.lockedUntil IS NOT NULL AND cred.lockedUntil > :now AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class)
        .setParameter("now", LocalDateTime.now())
        .getSingleResult();
  }

  private long countAccountsWithFailedAttempts() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.failedSignInAttempts > 0 AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class).getSingleResult();
  }

  private long countRequiresPasswordChange() {
    String jpql = "SELECT COUNT(c) FROM Customer c JOIN c.credential cred " +
        "WHERE cred.requiresPasswordChange = true AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class).getSingleResult();
  }

  private long countCustomersBeforeDate(LocalDate date) {
    String jpql = "SELECT COUNT(c) FROM Customer c WHERE c.createdAt < :date AND c.isDeleted = false";
    return entityManager.createQuery(jpql, Long.class)
        .setParameter("date", date.atStartOfDay())
        .getSingleResult();
  }

  private List<Object[]> getCountryDistribution() {
    String jpql = "SELECT c.country, COUNT(c) FROM Customer c " +
        "WHERE c.isDeleted = false GROUP BY c.country ORDER BY COUNT(c) DESC";
    return entityManager.createQuery(jpql, Object[].class).getResultList();
  }

  private List<Object[]> getChannelDistribution() {
    String jpql = "SELECT c.registrationChannel, COUNT(c) FROM Customer c " +
        "WHERE c.isDeleted = false GROUP BY c.registrationChannel ORDER BY COUNT(c) DESC";
    return entityManager.createQuery(jpql, Object[].class).getResultList();
  }

  private Map<LocalDate, Long> getDailyRegistrationCounts(LocalDate startDate, LocalDate endDate) {
    String jpql = "SELECT FUNCTION('DATE', c.createdAt), COUNT(c) FROM Customer c " +
        "WHERE c.createdAt >= :start AND c.createdAt < :end AND c.isDeleted = false " +
        "GROUP BY FUNCTION('DATE', c.createdAt)";

    List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
        .setParameter("start", startDate.atStartOfDay())
        .setParameter("end", endDate.plusDays(1).atStartOfDay())
        .getResultList();

    Map<LocalDate, Long> dailyCounts = new HashMap<>();

    // Initialize all days with 0
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      dailyCounts.put(date, 0L);
    }

    // Fill in actual counts
    for (Object[] row : results) {
      if (row[0] != null) {
        LocalDate date;
        if (row[0] instanceof java.sql.Date) {
          date = ((java.sql.Date) row[0]).toLocalDate();
        } else if (row[0] instanceof LocalDate) {
          date = (LocalDate) row[0];
        } else {
          continue;
        }
        dailyCounts.put(date, (Long) row[1]);
      }
    }

    return dailyCounts;
  }

  private String determineGrowthTrend(Map<LocalDate, Long> dailyCounts) {
    if (dailyCounts.size() < 7) {
      return "INSUFFICIENT_DATA";
    }

    List<Long> values = dailyCounts.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    int midpoint = values.size() / 2;
    double firstHalfAvg = values.subList(0, midpoint).stream()
        .mapToLong(Long::longValue).average().orElse(0);
    double secondHalfAvg = values.subList(midpoint, values.size()).stream()
        .mapToLong(Long::longValue).average().orElse(0);

    if (secondHalfAvg > firstHalfAvg * 1.1) {
      return "ACCELERATING";
    } else if (secondHalfAvg < firstHalfAvg * 0.9) {
      return "DECELERATING";
    } else {
      return "STABLE";
    }
  }

  private List<MonthlyGrowth> generateMonthlyBreakdown(LocalDate startDate, LocalDate endDate) {
    List<MonthlyGrowth> breakdown = new ArrayList<>();

    LocalDate current = startDate.withDayOfMonth(1);
    long cumulative = countCustomersBeforeDate(current);
    Long previousCount = null;

    while (!current.isAfter(endDate)) {
      LocalDate monthEnd = current.plusMonths(1).minusDays(1);
      if (monthEnd.isAfter(endDate)) {
        monthEnd = endDate;
      }

      long newInMonth = countNewCustomers(current, monthEnd);
      cumulative += newInMonth;

      double growthRate = 0;
      if (previousCount != null && previousCount > 0) {
        growthRate = ((double) (newInMonth - previousCount) / previousCount) * 100;
      }

      breakdown.add(MonthlyGrowth.builder()
          .month(current.getYear() + "-" + String.format("%02d", current.getMonthValue()))
          .newCustomers(newInMonth)
          .cumulativeTotal(cumulative)
          .growthRate(round(growthRate, 2))
          .build());

      previousCount = newInMonth;
      current = current.plusMonths(1);
    }

    return breakdown;
  }

  private List<RegionMetric> generateRegionalBreakdown(List<CountryMetric> countryMetrics,
      long totalCustomers) {
    Map<String, Long> regionCounts = new HashMap<>();
    Map<String, Integer> regionCountryCount = new HashMap<>();

    for (CountryMetric cm : countryMetrics) {
      String region = getRegionForCountry(cm.country());
      regionCounts.merge(region, cm.customerCount(), Long::sum);
      regionCountryCount.merge(region, 1, Integer::sum);
    }

    return regionCounts.entrySet().stream()
        .map(e -> RegionMetric.builder()
            .region(e.getKey())
            .customerCount(e.getValue())
            .percentage(
                totalCustomers > 0 ? round((double) e.getValue() / totalCustomers * 100, 2) : 0)
            .countryCount(regionCountryCount.getOrDefault(e.getKey(), 0))
            .build())
        .sorted((a, b) -> Long.compare(b.customerCount(), a.customerCount()))
        .collect(Collectors.toList());
  }

  private String getRegionForCountry(SupportedCountry country) {
    for (Map.Entry<String, List<SupportedCountry>> entry : REGION_COUNTRIES.entrySet()) {
      if (entry.getValue().contains(country)) {
        return entry.getKey();
      }
    }
    return "Other";
  }

  private List<WeeklyDataPoint> generateWeeklySummary(LocalDate startDate, LocalDate endDate) {
    List<WeeklyDataPoint> summary = new ArrayList<>();

    LocalDate weekStart = startDate;
    while (!weekStart.isAfter(endDate)) {
      LocalDate weekEnd = weekStart.plusDays(6);
      if (weekEnd.isAfter(endDate)) {
        weekEnd = endDate;
      }

      long newRegistrations = countNewCustomers(weekStart, weekEnd);

      summary.add(WeeklyDataPoint.builder()
          .weekStart(weekStart)
          .weekEnd(weekEnd)
          .newRegistrations(newRegistrations)
          .activeUsers(0) // Would need tracking
          .churnCount(0)
          .build());

      weekStart = weekStart.plusWeeks(1);
    }

    return summary;
  }

  private double round(double value, int places) {
    double scale = Math.pow(10, places);
    return Math.round(value * scale) / scale;
  }

}

