package com.commercepal.apiservice.cart.admin;

import com.commercepal.apiservice.cart.admin.dto.CartDashboardStatsDto;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/cart")
@RequiredArgsConstructor
@Tag(name = "Admin: Cart Analytics", description = "Endpoints for cart performance and analytics")
public class AdminCartController {

  private final CartAnalyticsService cartAnalyticsService;

  @GetMapping("/stats")
  @Operation(summary = "Get Cart Dashboard Stats", description = "Returns high-level metrics for cart performance")
  public ResponseEntity<ResponseWrapper<CartDashboardStatsDto>> getDashboardStats() {
    return ResponseWrapper.success(
        "Cart dashboard stats retrieved successfully",
        cartAnalyticsService.getDashboardStats());
  }

}
