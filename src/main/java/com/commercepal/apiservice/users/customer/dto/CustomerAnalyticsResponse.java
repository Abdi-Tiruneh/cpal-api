package com.commercepal.apiservice.users.customer.dto;

import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.users.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Comprehensive customer analytics response DTO.
 * <p>
 * Provides Amazon-level analytics including overview metrics, growth trends, geographic
 * distribution, engagement metrics, and security insights.
 */
@Builder
@Schema(
    name = "CustomerAnalyticsResponse",
    description = """
        Comprehensive customer analytics dashboard data.
        Includes overview metrics, growth trends, geographic distribution,
        engagement metrics, verification status, and security insights.
        """
)
public record CustomerAnalyticsResponse(

    @Schema(description = "Timestamp when analytics were generated")
    LocalDateTime generatedAt,

    @Schema(description = "Date range for the analytics")
    DateRange dateRange,

    @Schema(description = "Overview metrics and KPIs")
    OverviewMetrics overview,

    @Schema(description = "Customer growth trends and statistics")
    GrowthMetrics growth,

    @Schema(description = "Geographic distribution of customers")
    GeographicMetrics geographic,

    @Schema(description = "Registration channel breakdown")
    ChannelMetrics channels,

    @Schema(description = "Customer engagement and activity metrics")
    EngagementMetrics engagement,

    @Schema(description = "Verification and compliance metrics")
    VerificationMetrics verification,

    @Schema(description = "Security and risk metrics")
    SecurityMetrics security,

    @Schema(description = "Customer status distribution")
    StatusMetrics status,

    @Schema(description = "Time-series data for charts")
    TimeSeriesData timeSeries

) {

  // ========== DATE RANGE ==========

  @Builder
  @Schema(description = "Date range for analytics")
  public record DateRange(

      @Schema(description = "Start date of the analysis period", example = "2025-01-01")
      LocalDate startDate,

      @Schema(description = "End date of the analysis period", example = "2025-12-31")
      LocalDate endDate,

      @Schema(description = "Number of days in the analysis period", example = "365")
      int periodDays,

      @Schema(description = "Comparison period start date", example = "2024-01-01")
      LocalDate comparisonStartDate,

      @Schema(description = "Comparison period end date", example = "2024-12-31")
      LocalDate comparisonEndDate

  ) {

  }

  // ========== OVERVIEW METRICS ==========

  @Builder
  @Schema(description = "Overview metrics and key performance indicators")
  public record OverviewMetrics(

      @Schema(description = "Total number of customers", example = "15000")
      long totalCustomers,

      @Schema(description = "Number of active customers", example = "12500")
      long activeCustomers,

      @Schema(description = "Number of new customers in the period", example = "1500")
      long newCustomers,

      @Schema(description = "Number of churned/inactive customers", example = "200")
      long churnedCustomers,

      @Schema(description = "Customer retention rate as percentage", example = "92.5")
      double retentionRate,

      @Schema(description = "Customer churn rate as percentage", example = "7.5")
      double churnRate,

      @Schema(description = "Net customer growth (new - churned)", example = "1300")
      long netGrowth,

      @Schema(description = "Net growth rate as percentage", example = "8.67")
      double netGrowthRate,

      @Schema(description = "Average customers per day in period", example = "4.1")
      double avgNewCustomersPerDay,

      @Schema(description = "Customers with verified email", example = "11000")
      long emailVerifiedCustomers,

      @Schema(description = "Customers with verified phone", example = "13500")
      long phoneVerifiedCustomers,

      @Schema(description = "Fully verified customers (email + phone)", example = "10500")
      long fullyVerifiedCustomers

  ) {

  }

  // ========== GROWTH METRICS ==========

  @Builder
  @Schema(description = "Customer growth trends and statistics")
  public record GrowthMetrics(

      @Schema(description = "Daily average new registrations", example = "45.5")
      double dailyAverage,

      @Schema(description = "Weekly average new registrations", example = "318.5")
      double weeklyAverage,

      @Schema(description = "Monthly average new registrations", example = "1250.0")
      double monthlyAverage,

      @Schema(description = "Growth rate compared to previous period (%)", example = "15.3")
      double growthRateVsPreviousPeriod,

      @Schema(description = "Growth rate compared to same period last year (%)", example = "42.7")
      double growthRateYoY,

      @Schema(description = "Peak registration day", example = "2025-06-15")
      LocalDate peakDay,

      @Schema(description = "Peak day registrations count", example = "125")
      long peakDayCount,

      @Schema(description = "Lowest registration day", example = "2025-01-01")
      LocalDate lowestDay,

      @Schema(description = "Lowest day registrations count", example = "5")
      long lowestDayCount,

      @Schema(description = "Growth trend (ACCELERATING, STABLE, DECELERATING)", example = "ACCELERATING")
      String growthTrend,

      @Schema(description = "Projected customers by end of year", example = "20000")
      long projectedYearEndTotal,

      @Schema(description = "Monthly growth breakdown")
      List<MonthlyGrowth> monthlyBreakdown

  ) {

  }

  @Builder
  @Schema(description = "Monthly growth data")
  public record MonthlyGrowth(

      @Schema(description = "Month (YYYY-MM)", example = "2025-06")
      String month,

      @Schema(description = "New customers in this month", example = "1500")
      long newCustomers,

      @Schema(description = "Cumulative total at end of month", example = "15000")
      long cumulativeTotal,

      @Schema(description = "Growth rate vs previous month (%)", example = "12.5")
      double growthRate

  ) {

  }

  // ========== GEOGRAPHIC METRICS ==========

  @Builder
  @Schema(description = "Geographic distribution of customers")
  public record GeographicMetrics(

      @Schema(description = "Number of countries with customers", example = "45")
      int totalCountries,

      @Schema(description = "Top country by customer count")
      CountryMetric topCountry,

      @Schema(description = "Top 10 countries by customer count")
      List<CountryMetric> topCountries,

      @Schema(description = "Regional breakdown")
      List<RegionMetric> regions,

      @Schema(description = "Geographic concentration (% in top 5 countries)", example = "78.5")
      double geographicConcentration

  ) {

  }

  @Builder
  @Schema(description = "Country-level metrics")
  public record CountryMetric(

      @Schema(description = "Country code", example = "ET")
      SupportedCountry country,

      @Schema(description = "Country display name", example = "Ethiopia")
      String countryName,

      @Schema(description = "Number of customers", example = "8500")
      long customerCount,

      @Schema(description = "Percentage of total customers", example = "56.7")
      double percentage,

      @Schema(description = "Growth rate in this country (%)", example = "25.3")
      double growthRate,

      @Schema(description = "Rank by customer count", example = "1")
      int rank

  ) {

  }

  @Builder
  @Schema(description = "Regional metrics")
  public record RegionMetric(

      @Schema(description = "Region name", example = "Africa")
      String region,

      @Schema(description = "Number of customers in region", example = "9500")
      long customerCount,

      @Schema(description = "Percentage of total customers", example = "63.3")
      double percentage,

      @Schema(description = "Number of countries in region", example = "15")
      int countryCount

  ) {

  }

  // ========== CHANNEL METRICS ==========

  @Builder
  @Schema(description = "Registration channel breakdown")
  public record ChannelMetrics(

      @Schema(description = "Channel breakdown")
      List<ChannelMetric> channels,

      @Schema(description = "Most popular registration channel")
      Channel topChannel,

      @Schema(description = "Mobile vs Web vs API distribution")
      ChannelCategoryBreakdown categoryBreakdown

  ) {

  }

  @Builder
  @Schema(description = "Channel-level metrics")
  public record ChannelMetric(

      @Schema(description = "Registration channel", example = "MOBILE_APP_ANDROID")
      Channel channel,

      @Schema(description = "Channel display name", example = "Android App")
      String channelName,

      @Schema(description = "Number of customers", example = "6500")
      long customerCount,

      @Schema(description = "Percentage of total customers", example = "43.3")
      double percentage,

      @Schema(description = "Growth rate for this channel (%)", example = "35.2")
      double growthRate

  ) {

  }

  @Builder
  @Schema(description = "Channel category breakdown")
  public record ChannelCategoryBreakdown(

      @Schema(description = "Mobile app customers", example = "9000")
      long mobileCustomers,

      @Schema(description = "Mobile percentage", example = "60.0")
      double mobilePercentage,

      @Schema(description = "Web customers", example = "5500")
      long webCustomers,

      @Schema(description = "Web percentage", example = "36.7")
      double webPercentage,

      @Schema(description = "API customers", example = "500")
      long apiCustomers,

      @Schema(description = "API percentage", example = "3.3")
      double apiPercentage

  ) {

  }

  // ========== ENGAGEMENT METRICS ==========

  @Builder
  @Schema(description = "Customer engagement and activity metrics")
  public record EngagementMetrics(

      @Schema(description = "Customers who signed in within last 24 hours", example = "2500")
      long activeToday,

      @Schema(description = "Customers who signed in within last 7 days", example = "8500")
      long activeThisWeek,

      @Schema(description = "Customers who signed in within last 30 days", example = "11000")
      long activeThisMonth,

      @Schema(description = "Customers who signed in within last 90 days", example = "12500")
      long activeThisQuarter,

      @Schema(description = "Customers who never signed in after registration", example = "500")
      long neverSignedIn,

      @Schema(description = "Daily active users (DAU) average", example = "2350")
      double dauAverage,

      @Schema(description = "Weekly active users (WAU) average", example = "8200")
      double wauAverage,

      @Schema(description = "Monthly active users (MAU) average", example = "10800")
      double mauAverage,

      @Schema(description = "DAU/MAU ratio (stickiness)", example = "0.22")
      double stickiness,

      @Schema(description = "Average days between sign-ins", example = "3.5")
      double avgDaysBetweenSignIns,

      @Schema(description = "Customers with MFA enabled", example = "3500")
      long mfaEnabledCustomers,

      @Schema(description = "MFA adoption rate (%)", example = "23.3")
      double mfaAdoptionRate

  ) {

  }

  // ========== VERIFICATION METRICS ==========

  @Builder
  @Schema(description = "Verification and compliance metrics")
  public record VerificationMetrics(

      @Schema(description = "Email verification rate (%)", example = "73.3")
      double emailVerificationRate,

      @Schema(description = "Phone verification rate (%)", example = "90.0")
      double phoneVerificationRate,

      @Schema(description = "Full verification rate (both email + phone) (%)", example = "70.0")
      double fullVerificationRate,

      @Schema(description = "Pending email verification", example = "2500")
      long pendingEmailVerification,

      @Schema(description = "Pending phone verification", example = "1000")
      long pendingPhoneVerification,

      @Schema(description = "Average time to verify email (hours)", example = "2.5")
      double avgEmailVerificationTime,

      @Schema(description = "Average time to verify phone (hours)", example = "0.5")
      double avgPhoneVerificationTime,

      @Schema(description = "Verification funnel")
      VerificationFunnel funnel

  ) {

  }

  @Builder
  @Schema(description = "Verification funnel metrics")
  public record VerificationFunnel(

      @Schema(description = "Total registered", example = "15000")
      long registered,

      @Schema(description = "Email verified", example = "11000")
      long emailVerified,

      @Schema(description = "Phone verified", example = "13500")
      long phoneVerified,

      @Schema(description = "Fully verified", example = "10500")
      long fullyVerified,

      @Schema(description = "Drop-off at email verification (%)", example = "26.7")
      double emailDropOffRate,

      @Schema(description = "Drop-off at phone verification (%)", example = "10.0")
      double phoneDropOffRate

  ) {

  }

  // ========== SECURITY METRICS ==========

  @Builder
  @Schema(description = "Security and risk metrics")
  public record SecurityMetrics(

      @Schema(description = "Currently locked accounts", example = "25")
      long lockedAccounts,

      @Schema(description = "Accounts locked in period", example = "150")
      long accountsLockedInPeriod,

      @Schema(description = "Accounts with failed sign-in attempts > 0", example = "350")
      long accountsWithFailedAttempts,

      @Schema(description = "Total failed sign-in attempts in period", example = "2500")
      long totalFailedAttempts,

      @Schema(description = "Average failed attempts per locked account", example = "5.2")
      double avgFailedAttemptsPerLock,

      @Schema(description = "Password reset requests in period", example = "450")
      long passwordResetRequests,

      @Schema(description = "Successful password resets", example = "420")
      long successfulPasswordResets,

      @Schema(description = "Password reset success rate (%)", example = "93.3")
      double passwordResetSuccessRate,

      @Schema(description = "Accounts requiring password change", example = "75")
      long requiresPasswordChange,

      @Schema(description = "Suspicious activity flags", example = "12")
      long suspiciousActivityFlags

  ) {

  }

  // ========== STATUS METRICS ==========

  @Builder
  @Schema(description = "Customer status distribution")
  public record StatusMetrics(

      @Schema(description = "Status breakdown")
      List<StatusBreakdown> breakdown,

      @Schema(description = "Active rate (%)", example = "83.3")
      double activeRate,

      @Schema(description = "Inactive rate (%)", example = "10.0")
      double inactiveRate,

      @Schema(description = "Suspended rate (%)", example = "2.0")
      double suspendedRate,

      @Schema(description = "Locked rate (%)", example = "0.17")
      double lockedRate,

      @Schema(description = "Pending verification rate (%)", example = "3.33")
      double pendingVerificationRate,

      @Schema(description = "Deactivated rate (%)", example = "1.2")
      double deactivatedRate

  ) {

  }

  @Builder
  @Schema(description = "Status breakdown")
  public record StatusBreakdown(

      @Schema(description = "Account status", example = "ACTIVE")
      UserStatus status,

      @Schema(description = "Number of customers", example = "12500")
      long count,

      @Schema(description = "Percentage of total", example = "83.3")
      double percentage

  ) {

  }

  // ========== TIME SERIES DATA ==========

  @Builder
  @Schema(description = "Time-series data for charts")
  public record TimeSeriesData(

      @Schema(description = "Daily registration counts")
      List<DailyDataPoint> dailyRegistrations,

      @Schema(description = "Daily active users")
      List<DailyDataPoint> dailyActiveUsers,

      @Schema(description = "Cumulative customer growth")
      List<DailyDataPoint> cumulativeGrowth,

      @Schema(description = "Weekly summary data")
      List<WeeklyDataPoint> weeklySummary

  ) {

  }

  @Builder
  @Schema(description = "Daily data point")
  public record DailyDataPoint(

      @Schema(description = "Date", example = "2025-06-15")
      LocalDate date,

      @Schema(description = "Value", example = "125")
      long value

  ) {

  }

  @Builder
  @Schema(description = "Weekly data point")
  public record WeeklyDataPoint(

      @Schema(description = "Week start date", example = "2025-06-09")
      LocalDate weekStart,

      @Schema(description = "Week end date", example = "2025-06-15")
      LocalDate weekEnd,

      @Schema(description = "New registrations", example = "350")
      long newRegistrations,

      @Schema(description = "Active users", example = "8500")
      long activeUsers,

      @Schema(description = "Churn count", example = "25")
      long churnCount

  ) {

  }

}

