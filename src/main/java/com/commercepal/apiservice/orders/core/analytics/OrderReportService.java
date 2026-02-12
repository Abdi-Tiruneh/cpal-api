package com.commercepal.apiservice.orders.core.analytics;

import com.commercepal.apiservice.orders.core.analytics.dto.SalesTrendResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReportService {

  private final OrderAnalyticsService orderAnalyticsService;

  public byte[] generateSalesReportCsv(LocalDate startDate, LocalDate endDate) {
    List<SalesTrendResponse> trends = orderAnalyticsService.getSalesTrend(startDate, endDate);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PrintWriter writer = new PrintWriter(baos)) {
      // Header
      writer.println("Date,Orders,Revenue");

      // Data
      for (SalesTrendResponse trend : trends) {
        writer.printf("%s,%d,%.2f%n",
            trend.getDate(),
            trend.getOrderCount(),
            trend.getTotalRevenue());
      }
    }
    return baos.toByteArray();
  }
}
