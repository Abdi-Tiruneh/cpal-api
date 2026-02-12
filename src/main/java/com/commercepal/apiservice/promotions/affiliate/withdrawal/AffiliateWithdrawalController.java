package com.commercepal.apiservice.promotions.affiliate.withdrawal;

import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalRequestDto;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalResponseDto;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalSearchDto;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalStatsDto;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/affiliate/withdrawals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Affiliate Withdrawal Management", description = "APIs for affiliate withdrawal operations")
public class AffiliateWithdrawalController {

    private final AffiliateWithdrawalService withdrawalService;
    private final CurrentUserService currentUserService;

    @PostMapping("/request")
    @Operation(summary = "Create withdrawal request", description = "Allows affiliate to create a new withdrawal request")
    public ResponseEntity<ResponseWrapper<AffiliateWithdrawal>> createWithdrawalRequest(
        @Valid @RequestBody WithdrawalRequestDto requestDto) {
        Affiliate affiliate = currentUserService.getCurrentAffiliate();

        log.info("Creating withdrawal request | AffiliateWithdrawalController | createWithdrawalRequest | affiliateId={}", affiliate.getId());
        AffiliateWithdrawal response = withdrawalService.createWithdrawalRequest(affiliate,
                                                                                 requestDto);
        return ResponseWrapper.success(response);
    }

    @GetMapping("/my-withdrawals")
    @Operation(summary = "Get affiliate withdrawals", description = "Retrieve all withdrawal requests for the authenticated affiliate with pagination and filtering")
    public ResponseEntity<ResponseWrapper<PagedResponse<WithdrawalResponseDto>>> getMyWithdrawals(
        @Parameter(description = "Withdrawal status filter") @RequestParam(required = false) String status,
        @Parameter(description = "Payment method filter") @RequestParam(required = false) String paymentMethod,
        @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
        @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
        @Parameter(description = "Minimum amount") @RequestParam(required = false) String minAmount,
        @Parameter(description = "Maximum amount") @RequestParam(required = false) String maxAmount,
        @Parameter(description = "Sort by field") @RequestParam(defaultValue = "requestedAt") String sortBy,
        @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Affiliate affiliate = currentUserService.getCurrentAffiliate();

        WithdrawalSearchDto searchDto = buildSearchDto(status, paymentMethod, startDate, endDate,
                                                       minAmount, maxAmount, sortBy, sortDirection,
                                                       page, size);

        log.info("Retrieving withdrawals | AffiliateWithdrawalController | getMyWithdrawals | affiliateId={}", affiliate.getId());
        Page<AffiliateWithdrawal> withdrawals = withdrawalService.getAffiliateWithdrawals(affiliate,
                                                                                          searchDto);
        Page<WithdrawalResponseDto> responsePage = withdrawals.map(AffiliateWithdrawalMapper::convertToResponseDto);

        return ResponseWrapper.success(responsePage);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get withdrawal statistics", description = "Get withdrawal statistics for the authenticated affiliate")
    public ResponseEntity<ResponseWrapper<WithdrawalStatsDto>> getWithdrawalStats() {
        Affiliate affiliate = currentUserService.getCurrentAffiliate();

        log.info("Retrieving withdrawal stats | AffiliateWithdrawalController | getWithdrawalStats | affiliateId={}", affiliate.getId());
        WithdrawalStatsDto response = withdrawalService.getWithdrawalStats(affiliate);
        return ResponseWrapper.success(response);
    }

    @GetMapping("/{withdrawalId}")
    @Operation(summary = "Get withdrawal details", description = "Get details of a specific withdrawal request")
    public ResponseEntity<ResponseWrapper<WithdrawalResponseDto>> getWithdrawalDetails(
        @Parameter(description = "Withdrawal ID") @PathVariable Long withdrawalId) {
        Affiliate affiliate = currentUserService.getCurrentAffiliate();

        log.info("Retrieving withdrawal details | AffiliateWithdrawalController | getWithdrawalDetails | withdrawalId={}, affiliateId={}",
                 withdrawalId, affiliate.getId());
        WithdrawalResponseDto response = withdrawalService.getWithdrawalById(withdrawalId);
        return ResponseWrapper.success(response);
    }

    /**
     * Build search DTO from request parameters
     */
    private WithdrawalSearchDto buildSearchDto(String status, String paymentMethod,
                                               String startDate, String endDate, String minAmount,
                                               String maxAmount, String sortBy,
                                               String sortDirection, int page, int size) {

        WithdrawalSearchDto searchDto = new WithdrawalSearchDto();

        if (status != null && !status.isEmpty()) {
            try {
                searchDto.setStatus(
                    AffiliateWithdrawal.WithdrawalStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter | AffiliateWithdrawalController | buildSearchDto | status={}", status);
            }
        }

        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            try {
                searchDto.setPaymentMethod(
                    AffiliateWithdrawal.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid payment method filter | AffiliateWithdrawalController | buildSearchDto | paymentMethod={}", paymentMethod);
            }
        }

        if (startDate != null && !startDate.isEmpty()) {
            try {
                searchDto.setStartDate(java.sql.Timestamp.valueOf(startDate + " 00:00:00"));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid start date | AffiliateWithdrawalController | buildSearchDto | startDate={}", startDate);
            }
        }

        if (endDate != null && !endDate.isEmpty()) {
            try {
                searchDto.setEndDate(java.sql.Timestamp.valueOf(endDate + " 23:59:59"));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid end date | AffiliateWithdrawalController | buildSearchDto | endDate={}", endDate);
            }
        }

        if (minAmount != null && !minAmount.isEmpty()) {
            try {
                searchDto.setMinAmount(new java.math.BigDecimal(minAmount));
            } catch (NumberFormatException e) {
                log.warn("Invalid min amount | AffiliateWithdrawalController | buildSearchDto | minAmount={}", minAmount);
            }
        }

        if (maxAmount != null && !maxAmount.isEmpty()) {
            try {
                searchDto.setMaxAmount(new java.math.BigDecimal(maxAmount));
            } catch (NumberFormatException e) {
                log.warn("Invalid max amount | AffiliateWithdrawalController | buildSearchDto | maxAmount={}", maxAmount);
            }
        }

        searchDto.setSortBy(sortBy);
        searchDto.setSortDirection(sortDirection);
        searchDto.setPage(page);
        searchDto.setSize(size);

        return searchDto;
    }
}
