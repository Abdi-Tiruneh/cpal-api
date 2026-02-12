package com.commercepal.apiservice.promotions.affiliate.commission;

import com.commercepal.apiservice.promotions.affiliate.common.dto.AffiliateEarningsOverviewResponse;
import com.commercepal.apiservice.promotions.affiliate.common.dto.AffiliateSummaryDTO;
import com.commercepal.apiservice.promotions.affiliate.common.dto.CommissionCreateRequest;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/affiliates")
@RequiredArgsConstructor
public class AffiliateCommissionController {

    private final AffiliateCommissionService commissionService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<AffiliateCommission>> create(
        @Valid @RequestBody CommissionCreateRequest request) {
        return ResponseWrapper.success(commissionService.createCommission(request));
    }

    @GetMapping("/by-affiliate/{affiliateId}")
    public ResponseEntity<ResponseWrapper<List<AffiliateCommission>>> getByAffiliate(
        @PathVariable Long affiliateId) {
        return ResponseWrapper.success(commissionService.getByAffiliateId(affiliateId));
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<ResponseWrapper<String>> markAsPaid(@PathVariable Long id) {
        commissionService.markCommissionAsPaid(id);
        return ResponseWrapper.success("Commission marked as paid.");
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<ResponseWrapper<AffiliateEarningsOverviewResponse>> getEarningsOverview(
        @RequestParam Long affiliateId
    ) {
        return ResponseWrapper.success(commissionService.getLast30DaysOverview(affiliateId));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ResponseWrapper<AffiliateSummaryDTO>> getMonthlySummary(
        @RequestParam Long affiliateId) {
        return ResponseWrapper.success(commissionService.getMonthlySummary(affiliateId));
    }

    @GetMapping("/dashboard/lifetime")
    public ResponseEntity<ResponseWrapper<AffiliateSummaryDTO>> getLifetimeSummary(@RequestParam Long affiliateId) {
        return ResponseWrapper.success(commissionService.getLifetimeSummary(affiliateId));
    }

}
