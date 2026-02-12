package com.commercepal.apiservice.users.customer;

import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerAnalyticsResponse.OverviewMetrics;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerDetailsResponse;
import com.commercepal.apiservice.users.customer.dto.CustomerPageRequestDto;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import com.commercepal.apiservice.users.data.CustomerSeedService;
import com.commercepal.apiservice.users.data.CustomerSeedService.SeedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST controller for customer management and analytics.
 * <p>
 * Provides comprehensive administrative APIs for:
 * <ul>
 *   <li>Customer listing with advanced filtering, pagination, and sorting</li>
 *   <li>Detailed customer information retrieval</li>
 *   <li>Comprehensive analytics and metrics</li>
 *   <li>Growth trends and projections</li>
 *   <li>Geographic and channel distribution</li>
 *   <li>Engagement and security insights</li>
 * </ul>
 * <p>
 * All endpoints require admin authentication and appropriate role permissions.
 */
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Customer Management (Admin)",
    description = """
        Comprehensive administrative APIs for customer management and analytics.
        
        **Customer Management:**
        - List customers with advanced filtering and multi-level sorting
        - Get detailed customer profiles with account credentials
        - Search by ID, account number, or referral code
        
        **Analytics Dashboard:**
        - Overview metrics and KPIs
        - Growth trends and projections
        - Geographic and channel distribution
        - Engagement and activity metrics
        - Verification funnel analysis
        - Security and risk metrics
        - Time-series data for charts
        - Period-over-period comparisons
        
        Requires ADMIN or SUPER_ADMIN role.
        """
)
public class CustomerAdminController {

  private final CustomerAdminService customerAdminService;
  private final CustomerAnalyticsService analyticsService;
  private final CustomerSeedService customerSeedService;

  // ==================== CUSTOMER MANAGEMENT ENDPOINTS ====================

  /**
   * Get paginated list of customers with advanced filtering and sorting.
   */
  @GetMapping
  @Operation(
      summary = "Get list of customers",
      description = """
          Retrieves a paginated list of customers with advanced filtering and sorting capabilities.
          
          **Supported Filters:**
          - Customer: country, city, stateProvince, registrationChannel, preferredLanguage, preferredCurrency
          - Account: status, isEmailVerified, isPhoneVerified, mfaEnabled, isLocked
          - Dates: createdAfter/Before, updatedAfter/Before, lastSignedInAfter/Before
          - Audit: isDeleted
          
          **Sorting:**
          - Supports up to 3 levels of sorting
          - Customer fields: firstName, lastName, accountNumber, country, city, createdAt, updatedAt
          - Credential fields: status, emailAddress, phoneNumber, lastSignedInAt
          
          **Keyword Search:**
          - Searches across firstName, lastName, email, phone, accountNumber
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved customer list"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<CustomerDetailResponse>>> getCustomers(
      @Valid CustomerPageRequestDto requestDto) {

    log.info("[CUSTOMER-ADMIN] GET /customers - filters: {}, page: {}, size: {}",
        requestDto.getActiveFilterFields(), requestDto.page(), requestDto.size());

    Page<CustomerDetailResponse> customersPage = customerAdminService.getCustomers(requestDto);

    log.info("[CUSTOMER-ADMIN] Returning {} customers (page {} of {})",
        customersPage.getNumberOfElements(),
        customersPage.getNumber() + 1,
        customersPage.getTotalPages());

    return ResponseWrapper.success("Customers retrieved successfully", customersPage);
  }

  /**
   * Get detailed customer information by ID.
   */
  @GetMapping("/{customerId}")
  @Operation(
      summary = "Get customer details by ID",
      description = """
          Retrieves comprehensive customer details including:
          
          **Customer Info:** Profile, account numbers, location, preferences, notes
          **Account Credential Info:** Contact, verification status, security, roles
          **Audit Info:** Timestamps, users, IP addresses, versions
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved customer details"),
      @ApiResponse(responseCode = "404", description = "Customer not found"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<CustomerDetailsResponse>> getCustomerById(
      @Parameter(description = "Customer ID", required = true, example = "1")
      @PathVariable Long customerId) {

    log.info("[CUSTOMER-ADMIN] GET /customers/{}", customerId);

    return customerAdminService.getCustomerById(customerId)
        .map(customer -> {
          log.info("[CUSTOMER-ADMIN] Customer found: ID={}, accountNumber={}",
              customer.customerInfo().id(), customer.customerInfo().accountNumber());
          return ResponseWrapper.success("Customer retrieved successfully", customer);
        })
        .orElseGet(() -> {
          log.warn("[CUSTOMER-ADMIN] Customer not found: ID={}", customerId);
          return ResponseWrapper.notFound("Customer not found with ID: " + customerId);
        });
  }

  /**
   * Get count of customers matching filter criteria.
   */
  @GetMapping("/count")
  @Operation(
      summary = "Get customer count",
      description = "Returns the total count of customers matching the provided filter criteria."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved customer count")
  })
  public ResponseEntity<ResponseWrapper<CustomerCountResponse>> getCustomerCount(
      @Valid CustomerPageRequestDto requestDto) {

    log.info("[CUSTOMER-ADMIN] GET /customers/count - filters: {}",
        requestDto.getActiveFilterFields());

    long count = customerAdminService.getCustomerCount(requestDto);

    log.info("[CUSTOMER-ADMIN] Customer count: {}", count);

    CustomerCountResponse response = new CustomerCountResponse(
        count,
        requestDto.getActiveFilterFields().size(),
        requestDto.hasFilters()
    );

    return ResponseWrapper.success("Customer count retrieved successfully", response);
  }

  // ==================== SEED ENDPOINTS ====================

  /**
   * Seed test customers with random data.
   */
  @PostMapping("/seed")
  @Operation(
      summary = "Seed test customers",
      description = """
          Seeds test customers with random Ethiopian names, phone numbers, and email addresses.
          Each customer is created with:
          - Random Ethiopian first and last name
          - Unique email (format: firstname.lastname.index@testcustomer.com)
          - Unique Ethiopian phone number (+251 9XX XXX XXXX)
          - Default address with Ethiopian location data
          
          If a customer already exists (by email or phone), it will be skipped but 
          an address will be created if missing.
          
          **WARNING:** This endpoint is for testing/development purposes only.
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully seeded customers"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
  })
  public ResponseEntity<ResponseWrapper<SeedResponse>> seedCustomers(
      @Parameter(description = "Number of customers to seed (1-10000)", example = "1000")
      @RequestParam(defaultValue = "1000")
      @Min(value = 1, message = "Count must be at least 1")
      @Max(value = 10000, message = "Count must not exceed 10000")
      int count,

      @Parameter(description = "Password for all seeded customers", example = "123456")
      @RequestParam(defaultValue = "123456")
      String password) {

    log.info("[CUSTOMER-ADMIN] POST /seed - count: {}", count);

    SeedResult result = customerSeedService.seedCustomers(
        CustomerSeedService.SeedRequest.builder()
            .count(count)
            .password(password)
            .build()
    );

    SeedResponse response = new SeedResponse(
        result.totalRequested(),
        result.created(),
        result.existing(),
        result.failed(),
        result.durationMs(),
        result.password(),
        "Seeding completed successfully"
    );

    log.info("[CUSTOMER-ADMIN] Seed complete - created: {}, existing: {}, failed: {}",
        result.created(), result.existing(), result.failed());

    return ResponseWrapper.success("Customer seeding completed", response);
  }

  // ==================== ANALYTICS ENDPOINTS ====================

  /**
   * Get comprehensive customer analytics for the specified date range.
   */
  @GetMapping("/analytics")
  @Operation(
      summary = "Get comprehensive customer analytics",
      description = """
          Retrieves comprehensive customer analytics for the specified date range.
          
          **Includes:**
          - Overview metrics (total, active, new, churned customers)
          - Growth metrics (daily/weekly/monthly averages, YoY, projections)
          - Geographic metrics (country/regional distribution)
          - Channel metrics (registration channel breakdown)
          - Engagement metrics (DAU/WAU/MAU, stickiness)
          - Verification metrics (funnel, drop-off rates)
          - Security metrics (locked accounts, failed attempts)
          - Status distribution
          - Time-series data for charts
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved analytics"),
      @ApiResponse(responseCode = "400", description = "Invalid date range"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<ResponseWrapper<CustomerAnalyticsResponse>> getAnalytics(
      @Parameter(description = "Start date (YYYY-MM-DD). Defaults to 30 days ago.", example = "2025-01-01")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate startDate,

      @Parameter(description = "End date (YYYY-MM-DD). Defaults to today.", example = "2025-12-31")
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate endDate) {

    if (endDate == null) {
      endDate = LocalDate.now();
    }
    if (startDate == null) {
      startDate = endDate.minusDays(30);
    }

    if (startDate.isAfter(endDate)) {
      log.warn("[CUSTOMER-ADMIN] Invalid date range: {} to {}", startDate, endDate);
      return ResponseWrapper.badRequest("Start date must be before or equal to end date");
    }

    log.info("[CUSTOMER-ADMIN] GET /analytics - period: {} to {}", startDate, endDate);

    CustomerAnalyticsResponse analytics = analyticsService.generateAnalytics(startDate, endDate);

    log.info("[CUSTOMER-ADMIN] Analytics generated - total: {}, new: {}",
        analytics.overview().totalCustomers(), analytics.overview().newCustomers());

    return ResponseWrapper.success("Customer analytics retrieved successfully", analytics);
  }

  /**
   * Get quick overview metrics (lightweight endpoint).
   */
  @GetMapping("/analytics/overview")
  @Operation(
      summary = "Get quick overview metrics",
      description = """
          Retrieves lightweight overview metrics for the last 30 days.
          Faster alternative to full analytics when you only need key metrics.
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved overview metrics")
  })
  public ResponseEntity<ResponseWrapper<OverviewMetrics>> getOverview() {
    log.info("[CUSTOMER-ADMIN] GET /analytics/overview");

    OverviewMetrics overview = analyticsService.generateQuickOverview();

    log.info("[CUSTOMER-ADMIN] Overview - total: {}, active: {}",
        overview.totalCustomers(), overview.activeCustomers());

    return ResponseWrapper.success("Overview metrics retrieved successfully", overview);
  }

  /**
   * Get analytics for today.
   */
  @GetMapping("/analytics/today")
  @Operation(summary = "Get today's analytics", description = "Retrieves analytics for the current day only.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved today's analytics")
  })
  public ResponseEntity<ResponseWrapper<CustomerAnalyticsResponse>> getTodayAnalytics() {
    log.info("[CUSTOMER-ADMIN] GET /analytics/today");
    LocalDate today = LocalDate.now();
    return ResponseWrapper.success("Today's analytics retrieved successfully",
        analyticsService.generateAnalytics(today, today));
  }

  /**
   * Get analytics for this week.
   */
  @GetMapping("/analytics/week")
  @Operation(summary = "Get this week's analytics", description = "Retrieves analytics for the last 7 days.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved this week's analytics")
  })
  public ResponseEntity<ResponseWrapper<CustomerAnalyticsResponse>> getWeekAnalytics() {
    log.info("[CUSTOMER-ADMIN] GET /analytics/week");
    LocalDate today = LocalDate.now();
    return ResponseWrapper.success("This week's analytics retrieved successfully",
        analyticsService.generateAnalytics(today.minusDays(6), today));
  }

  /**
   * Get analytics for this month.
   */
  @GetMapping("/analytics/month")
  @Operation(summary = "Get this month's analytics", description = "Retrieves analytics for the last 30 days.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved this month's analytics")
  })
  public ResponseEntity<ResponseWrapper<CustomerAnalyticsResponse>> getMonthAnalytics() {
    log.info("[CUSTOMER-ADMIN] GET /analytics/month");
    LocalDate today = LocalDate.now();
    return ResponseWrapper.success("This month's analytics retrieved successfully",
        analyticsService.generateAnalytics(today.minusDays(29), today));
  }

  /**
   * Get analytics for this quarter.
   */
  @GetMapping("/analytics/quarter")
  @Operation(summary = "Get this quarter's analytics", description = "Retrieves analytics for the last 90 days.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved this quarter's analytics")
  })
  public ResponseEntity<ResponseWrapper<CustomerAnalyticsResponse>> getQuarterAnalytics() {
    log.info("[CUSTOMER-ADMIN] GET /analytics/quarter");
    LocalDate today = LocalDate.now();
    return ResponseWrapper.success("This quarter's analytics retrieved successfully",
        analyticsService.generateAnalytics(today.minusDays(89), today));
  }

  /**
   * Get analytics for this year.
   */
  @GetMapping("/analytics/year")
  @Operation(summary = "Get this year's analytics", description = "Retrieves analytics from January 1st to today.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved this year's analytics")
  })
  public ResponseEntity<ResponseWrapper<CustomerAnalyticsResponse>> getYearAnalytics() {
    log.info("[CUSTOMER-ADMIN] GET /analytics/year");
    LocalDate today = LocalDate.now();
    return ResponseWrapper.success("This year's analytics retrieved successfully",
        analyticsService.generateAnalytics(today.withDayOfYear(1), today));
  }

  /**
   * Compare analytics between two periods.
   */
  @GetMapping("/analytics/compare")
  @Operation(
      summary = "Compare analytics between two periods",
      description = """
          Compares customer analytics between two date ranges.
          Useful for period-over-period comparisons (month vs month, year vs year).
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved comparison analytics"),
      @ApiResponse(responseCode = "400", description = "Invalid date ranges")
  })
  public ResponseEntity<ResponseWrapper<AnalyticsComparison>> compareAnalytics(
      @Parameter(description = "First period start date", example = "2025-01-01", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period1Start,

      @Parameter(description = "First period end date", example = "2025-01-31", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period1End,

      @Parameter(description = "Second period start date", example = "2024-01-01", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period2Start,

      @Parameter(description = "Second period end date", example = "2024-01-31", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period2End) {

    log.info("[CUSTOMER-ADMIN] GET /analytics/compare - period1: {} to {}, period2: {} to {}",
        period1Start, period1End, period2Start, period2End);

    if (period1Start.isAfter(period1End) || period2Start.isAfter(period2End)) {
      return ResponseWrapper.badRequest("Start dates must be before or equal to end dates");
    }

    CustomerAnalyticsResponse p1 = analyticsService.generateAnalytics(period1Start, period1End);
    CustomerAnalyticsResponse p2 = analyticsService.generateAnalytics(period2Start, period2End);

    OverviewMetrics o1 = p1.overview();
    OverviewMetrics o2 = p2.overview();

    double customerChange = o2.totalCustomers() > 0
        ? ((double) (o1.totalCustomers() - o2.totalCustomers()) / o2.totalCustomers()) * 100 : 0;
    double newCustomerChange = o2.newCustomers() > 0
        ? ((double) (o1.newCustomers() - o2.newCustomers()) / o2.newCustomers()) * 100 : 0;
    double activeChange = o2.activeCustomers() > 0
        ? ((double) (o1.activeCustomers() - o2.activeCustomers()) / o2.activeCustomers()) * 100 : 0;

    AnalyticsComparison comparison = new AnalyticsComparison(
        p1, p2,
        new ComparisonSummary(
            round(customerChange),
            round(newCustomerChange),
            round(activeChange),
            round(o1.retentionRate() - o2.retentionRate()),
            round(o1.churnRate() - o2.churnRate())
        )
    );

    return ResponseWrapper.success("Analytics comparison retrieved successfully", comparison);
  }

  // ==================== HELPER METHODS ====================

  private double round(double value) {
    double scale = Math.pow(10, 2);
    return Math.round(value * scale) / scale;
  }

  // ==================== RESPONSE RECORDS ====================

  @Schema(description = "Customer count response")
  public record CustomerCountResponse(
      @Schema(description = "Total count of customers matching criteria", example = "150")
      long count,
      @Schema(description = "Number of active filters applied", example = "3")
      int activeFiltersCount,
      @Schema(description = "Whether any filters are applied", example = "true")
      boolean hasFilters
  ) {

  }

  @Schema(description = "Analytics comparison between two periods")
  public record AnalyticsComparison(
      @Schema(description = "First period analytics")
      CustomerAnalyticsResponse period1,
      @Schema(description = "Second period analytics")
      CustomerAnalyticsResponse period2,
      @Schema(description = "Summary of changes between periods")
      ComparisonSummary summary
  ) {

  }

  @Schema(description = "Summary of changes between two periods")
  public record ComparisonSummary(
      @Schema(description = "Total customer change (%)", example = "15.5")
      double totalCustomerChange,
      @Schema(description = "New customer change (%)", example = "25.3")
      double newCustomerChange,
      @Schema(description = "Active customer change (%)", example = "12.1")
      double activeCustomerChange,
      @Schema(description = "Retention rate change (percentage points)", example = "2.5")
      double retentionRateChange,
      @Schema(description = "Churn rate change (percentage points)", example = "-2.5")
      double churnRateChange
  ) {

  }

  @Schema(description = "Response for customer seeding operation")
  public record SeedResponse(
      @Schema(description = "Total number of customers requested to seed", example = "1000")
      int totalRequested,
      @Schema(description = "Number of new customers created", example = "950")
      int created,
      @Schema(description = "Number of customers that already existed (skipped)", example = "45")
      int existing,
      @Schema(description = "Number of customers that failed to create", example = "5")
      int failed,
      @Schema(description = "Duration of the seeding operation in milliseconds", example = "15234")
      long durationMs,
      @Schema(description = "Password used for all seeded customers", example = "123456")
      String password,
      @Schema(description = "Status message", example = "Seeding completed successfully")
      String message
  ) {

  }

}
