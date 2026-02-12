package com.commercepal.apiservice.orders.core.analytics;

import com.commercepal.apiservice.orders.core.analytics.dto.OrderAnalyticsSummary;
import com.commercepal.apiservice.orders.core.analytics.dto.OrderStatusBreakdown;
import com.commercepal.apiservice.orders.core.analytics.dto.SalesTrendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/orders/analytics")
@RequiredArgsConstructor
@Tag(name = "Admin Order Analytics", description = "APIs for Admin Dashboard Analytics & Reports")
public class AdminOrderAnalyticsController {

  private final OrderAnalyticsService orderAnalyticsService;
  private final OrderReportService reportService;

  @Operation(summary = "Get Dashboard Summary", description = "Returns high-level metrics for the admin dashboard")
  @GetMapping("/summary")
  public ResponseEntity<OrderAnalyticsSummary> getDashboardSummary(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return ResponseEntity.ok(orderAnalyticsService.getDashboardSummary(startDate, endDate));
  }

  @Operation(summary = "Get Sales Trend", description = "Returns daily sales trend for time series charts")
  @GetMapping("/sales-trend")
  public ResponseEntity<List<SalesTrendResponse>> getSalesTrend(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return ResponseEntity.ok(orderAnalyticsService.getSalesTrend(startDate, endDate));
  }

  @Operation(summary = "Get Order Status Breakdown", description = "Returns count and percentage of orders by status")
  @GetMapping("/status-breakdown")
  public ResponseEntity<List<OrderStatusBreakdown>> getStatusBreakdown(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    return ResponseEntity.ok(orderAnalyticsService.getStatusBreakdown(startDate, endDate));
  }

  @Operation(summary = "Get Top Selling Products", description = "Returns top selling products by revenue")
  @GetMapping("/top-products")
  public ResponseEntity<List<com.commercepal.apiservice.orders.core.analytics.dto.TopProductResponse>> getTopSellingProducts(
      @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(orderAnalyticsService.getTopSellingProducts(limit));
  }

  @Operation(summary = "Export Sales Report (CSV)", description = "Exports sales trend data as CSV")
  @GetMapping(value = "/export/sales-report", produces = "text/csv")
  public ResponseEntity<byte[]> exportSalesReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    // Note: Ideally inject OrderReportService here, but for simplicity we can use
    // it or add it to constructor
    // I will need to update the constructor to include OrderReportService
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=sales-report.csv")
        .body(reportService.generateSalesReportCsv(startDate, endDate));
  }
}
