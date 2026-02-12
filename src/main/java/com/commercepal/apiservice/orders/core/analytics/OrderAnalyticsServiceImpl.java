package com.commercepal.apiservice.orders.core.analytics;

import com.commercepal.apiservice.orders.core.analytics.dto.OrderAnalyticsSummary;
import com.commercepal.apiservice.orders.core.analytics.dto.OrderStatusBreakdown;
import com.commercepal.apiservice.orders.core.analytics.dto.SalesTrendResponse;
import com.commercepal.apiservice.orders.core.analytics.dto.TopProductResponse;
import com.commercepal.apiservice.orders.core.repository.OrderItemRepository;
import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import com.commercepal.apiservice.orders.enums.OrderStage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderAnalyticsServiceImpl implements OrderAnalyticsService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  public OrderAnalyticsSummary getDashboardSummary(LocalDate startDate, LocalDate endDate) {
    // Default to last 30 days if null
    if (startDate == null) {
      startDate = LocalDate.now().minusDays(30);
    }
    if (endDate == null) {
      endDate = LocalDate.now();
    }

    BigDecimal totalRevenue = orderRepository.sumTotalRevenueBetween(startDate.atStartOfDay(),
        endDate.plusDays(1).atStartOfDay().minusNanos(1));
    long totalOrders = orderRepository.countOrdersBetween(startDate.atStartOfDay(),
        endDate.plusDays(1).atStartOfDay().minusNanos(1));

    // Get counts by stage for specific buckets
    List<Object[]> stageCounts = orderRepository.countOrdersByStageBetween(startDate.atStartOfDay(),
        endDate.plusDays(1).atStartOfDay().minusNanos(1));

    long pending = 0;
    long completed = 0;
    long cancelled = 0;

    for (Object[] row : stageCounts) {
      OrderStage stage = (OrderStage) row[0];
      Long count = (Long) row[1];

      if (stage == OrderStage.DELIVERED) {
        completed += count;
      } else if (stage == OrderStage.CANCELLED || stage == OrderStage.FAILED) {
        cancelled += count;
      } else if (stage.isActive()) {
        // All other active stages are considered pending/in-progress
        pending += count;
      }
    }

    BigDecimal averageOrderValue = totalOrders > 0
        ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    return OrderAnalyticsSummary.builder()
        .totalRevenue(totalRevenue)
        .totalOrders(totalOrders)
        .averageOrderValue(averageOrderValue)
        .pendingOrders(pending)
        .completedOrders(completed)
        .cancelledOrders(cancelled)
        .build();
  }

  @Override
  public List<SalesTrendResponse> getSalesTrend(LocalDate startDate, LocalDate endDate) {
    if (startDate == null) {
      startDate = LocalDate.now().minusDays(30);
    }
    if (endDate == null) {
      endDate = LocalDate.now();
    }

    List<Object[]> results = orderRepository.findDailySalesStats(startDate.atStartOfDay(),
        endDate.plusDays(1).atStartOfDay().minusNanos(1));

    // Map results to DTO
    // Warning: The type of Date object returned by function('DATE', ...) depends on
    // DB dialect.
    // It might be java.sql.Date, java.time.LocalDate, or String.
    // We will robustly handle it.

    return results.stream().map(row -> {
      Object dateObj = row[0];
      String dateStr = dateObj.toString();
      Long count = ((Number) row[1]).longValue();
      BigDecimal amount = (BigDecimal) row[2];

      return SalesTrendResponse.builder()
          .date(dateStr)
          .orderCount(count)
          .totalRevenue(amount)
          .build();
    }).collect(Collectors.toList());
  }

  @Override
  public List<OrderStatusBreakdown> getStatusBreakdown(LocalDate startDate, LocalDate endDate) {
    if (startDate == null) {
      startDate = LocalDate.now().minusDays(30);
    }
    if (endDate == null) {
      endDate = LocalDate.now();
    }

    List<Object[]> results = orderRepository.countOrdersByStageBetween(startDate.atStartOfDay(),
        endDate.plusDays(1).atStartOfDay().minusNanos(1));
    long totalOrders = results.stream().mapToLong(row -> (Long) row[1]).sum();

    return results.stream().map(row -> {
      OrderStage stage = (OrderStage) row[0];
      Long count = (Long) row[1];
      Double percentage = totalOrders > 0 ? (count * 100.0) / totalOrders : 0.0;

      return OrderStatusBreakdown.builder()
          .stage(stage)
          .count(count)
          .percentage(Math.round(percentage * 10.0) / 10.0) // Round to 1 decimal
          .build();
    }).collect(Collectors.toList());
  }

  @Override
  public List<TopProductResponse> getTopSellingProducts(int limit) {
    List<Object[]> results = orderItemRepository.findTopSellingProducts(PageRequest.of(0, limit));

    return results.stream().map(row -> {
      String productName = (String) row[0];
      Long units = ((Number) row[1]).longValue();
      BigDecimal revenue = (BigDecimal) row[2];

      return TopProductResponse.builder()
          .productName(productName)
          .totalUnitsSold(units)
          .totalRevenue(revenue)
          .build();
    }).collect(Collectors.toList());
  }
}
