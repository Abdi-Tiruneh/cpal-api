package com.commercepal.apiservice.cart.admin;

import com.commercepal.apiservice.cart.admin.dto.CartDashboardStatsDto;
import com.commercepal.apiservice.cart.repository.CartRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Cart Analytics and Admin Reporting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartAnalyticsService {

  private final CartRepository cartRepository;

  /**
   * Get main dashboard statistics for carts.
   */
  @Transactional(readOnly = true)
  public CartDashboardStatsDto getDashboardStats() {
    log.info("Generating cart dashboard stats...");

    long activeCartsCount = cartRepository.countTotalActiveCarts();
    long totalItems = cartRepository.countTotalActiveCartItems();
    BigDecimal totalValue = cartRepository.getTotalActiveCartValue();
    BigDecimal avgValue = cartRepository.getAverageCartValue();
    Double conversionRate = cartRepository.getConversionRate();
    Double abandonmentRate = cartRepository.getAbandonmentRate();

    return CartDashboardStatsDto.builder()
        .activeCartsCount(activeCartsCount)
        .totalActiveItems(totalItems)
        .totalActiveValue(totalValue != null ? totalValue : BigDecimal.ZERO)
        .averageCartValue(avgValue != null ? avgValue.setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO)
        .conversionRate(conversionRate != null
            ? BigDecimal.valueOf(conversionRate).setScale(2, RoundingMode.HALF_UP)
            .doubleValue()
            : 0.0)
        .abandonmentRate(abandonmentRate != null ? BigDecimal.valueOf(abandonmentRate)
            .setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0)
        .build();
  }

  /**
   * Get list of abandoned carts (inactive for > 24 hours).
   */
  @Transactional(readOnly = true)
  public Object getAbandonedCarts() {
    // Defined as inactive for 24 hours?
    LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
    return cartRepository.findAbandonedCarts(cutoff);
  }
}
