//package com.commercepal.apiservice.promotions.affiliate.withdrawal;
//
//import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalResponseDto;
//import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalSearchDto;
//import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalStatsDto;
//import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalUpdateDto;
//import com.commercepal.apiservice.utils.response.ResponseWrapper;
//import com.commercepal.apiservice.utils.response.PagedResponse;
//import com.commercepal.apiservice.utils.CurrentUserService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/admin/withdrawals")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Admin Withdrawal Management", description = "APIs for admin withdrawal operations")
//public class AdminWithdrawalController {
//
//    private static final String AUTH_HEADER = "Authorization";
//
//    private final AffiliateWithdrawalService withdrawalService;
//    private final CurrentUserService currentUserService;
//
//    @GetMapping
//    @Operation(summary = "Get all withdrawals",
//        description = "Retrieve all withdrawal requests with pagination, sorting, and filtering for admin")
//    public ResponseEntity<ResponseWrapper<PagedResponse<WithdrawalResponseDto>>> getAllWithdrawals(
//        @Parameter(description = "Affiliate ID filter") @RequestParam(required = false) Long affiliateId,
//        @Parameter(description = "Withdrawal status filter") @RequestParam(required = false) String status,
//        @Parameter(description = "Payment method filter") @RequestParam(required = false) String paymentMethod,
//        @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
//        @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String endDate,
//        @Parameter(description = "Minimum amount") @RequestParam(required = false) String minAmount,
//        @Parameter(description = "Maximum amount") @RequestParam(required = false) String maxAmount,
//        @Parameter(description = "Sort by field") @RequestParam(defaultValue = "requestedAt") String sortBy,
//        @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDirection,
//        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
//        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
//        HttpServletRequest httpRequest) {
//
//        WithdrawalSearchDto searchDto = buildSearchDto(affiliateId, status, paymentMethod,
//                                                       startDate, endDate,
//                                                       minAmount, maxAmount, sortBy, sortDirection,
//                                                       page, size);
//
//        log.info("Retrieving all withdrawals | AdminWithdrawalController | getAllWithdrawals | adminUser={}",
//                 currentUserService.getAdminUserName(httpRequest.getHeader(AUTH_HEADER)));
//        return withdrawalService.getAllWithdrawals(searchDto);
//    }
//
//    @GetMapping("/stats")
//    @Operation(summary = "Get withdrawal statistics",
//        description = "Get global withdrawal statistics for admin dashboard")
//    public ResponseEntity<ResponseWrapper<WithdrawalStatsDto>> getWithdrawalStats(
//        HttpServletRequest httpRequest) {
//
//        log.info("Retrieving withdrawal statistics | AdminWithdrawalController | getWithdrawalStats | adminUser={}",
//                 currentUserService.getAdminUserName(httpRequest.getHeader(AUTH_HEADER)));
//        WithdrawalStatsDto response = withdrawalService.getWithdrawalStats(null);
//        return ResponseWrapper.success(response);
//    }
//
//    @GetMapping("/{withdrawalId}")
//    @Operation(summary = "Get withdrawal details",
//        description = "Get details of a specific withdrawal request for admin review")
//    public ResponseEntity<ResponseWrapper<WithdrawalResponseDto>> getWithdrawalDetails(
//        @Parameter(description = "Withdrawal ID") @PathVariable Long withdrawalId,
//        HttpServletRequest httpRequest) {
//
//        log.info("Retrieving withdrawal details | AdminWithdrawalController | getWithdrawalDetails | adminUser={}, withdrawalId={}",
//                 currentUserService.getAdminUserName(httpRequest.getHeader(AUTH_HEADER)), withdrawalId);
//        WithdrawalResponseDto response = withdrawalService.getWithdrawalById(withdrawalId);
//        return ResponseWrapper.success(response);
//    }
//
//    @PutMapping("/{withdrawalId}/status")
//    @Operation(summary = "Update withdrawal status",
//        description = "Update the status of a withdrawal request (approve, reject, mark as paid)")
//    public ResponseEntity<ResponseWrapper<WithdrawalResponseDto>> updateWithdrawalStatus(
//        @Parameter(description = "Withdrawal ID") @PathVariable Long withdrawalId,
//        @Valid @RequestBody WithdrawalUpdateDto updateDto,
//        HttpServletRequest httpRequest) {
//
//        String adminUser = currentUserService.getAdminUserName(
//            httpRequest.getHeader(AUTH_HEADER));
//
//        log.info("Updating withdrawal status | AdminWithdrawalController | updateWithdrawalStatus | adminUser={}, withdrawalId={}, status={}",
//                 adminUser, withdrawalId, updateDto.getStatus());
//        WithdrawalResponseDto response = withdrawalService.updateWithdrawalStatus(withdrawalId,
//                                                                                  updateDto,
//                                                                                  adminUser);
//        return ResponseWrapper.success(response);
//    }
//
//    @GetMapping("/pending")
//    @Operation(summary = "Get pending withdrawals",
//        description = "Get all pending withdrawal requests for admin review")
//    public ResponseEntity<ResponseWrapper<PagedResponse<WithdrawalResponseDto>>> getPendingWithdrawals(
//        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
//        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
//        HttpServletRequest httpRequest) {
//
//        WithdrawalSearchDto searchDto = new WithdrawalSearchDto();
//        searchDto.setStatus(AffiliateWithdrawal.WithdrawalStatus.PENDING);
//        searchDto.setSortBy("requestedAt");
//        searchDto.setSortDirection("ASC"); // Oldest first for processing
//        searchDto.setPage(page);
//        searchDto.setSize(size);
//
//        log.info("Retrieving pending withdrawals | AdminWithdrawalController | getPendingWithdrawals | adminUser={}",
//                 currentUserService.getAdminUserName(httpRequest.getHeader(AUTH_HEADER)));
//        return withdrawalService.getAllWithdrawals(searchDto);
//    }
//
//    @GetMapping("/by-status/{status}")
//    @Operation(summary = "Get withdrawals by status",
//        description = "Get all withdrawals filtered by specific status")
//    public ResponseEntity<ResponseWrapper<PagedResponse<WithdrawalResponseDto>>> getWithdrawalsByStatus(
//        @Parameter(description = "Withdrawal status") @PathVariable String status,
//        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
//        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
//        HttpServletRequest httpRequest) {
//
//        try {
//            AffiliateWithdrawal.WithdrawalStatus withdrawalStatus =
//                AffiliateWithdrawal.WithdrawalStatus.valueOf(status.toUpperCase());
//
//            WithdrawalSearchDto searchDto = new WithdrawalSearchDto();
//            searchDto.setStatus(withdrawalStatus);
//            searchDto.setSortBy("requestedAt");
//            searchDto.setSortDirection("DESC");
//            searchDto.setPage(page);
//            searchDto.setSize(size);
//
//            log.info("Retrieving withdrawals by status | AdminWithdrawalController | getWithdrawalsByStatus | adminUser={}, status={}",
//                     currentUserService.getAdminUserName(httpRequest.getHeader(AUTH_HEADER)), status);
//            return withdrawalService.getAllWithdrawals(searchDto);
//        } catch (IllegalArgumentException e) {
//            log.warn("Invalid status filter | AdminWithdrawalController | getWithdrawalsByStatus | status={}", status);
//            throw new IllegalArgumentException("Invalid status: " + status);
//        }
//    }
//
//    @GetMapping("/by-affiliate/{affiliateId}")
//    @Operation(summary = "Get withdrawals by affiliate",
//        description = "Get all withdrawals for a specific affiliate")
//    public ResponseEntity<ResponseWrapper<PagedResponse<WithdrawalResponseDto>>> getWithdrawalsByAffiliate(
//        @Parameter(description = "Affiliate ID") @PathVariable Long affiliateId,
//        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
//        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
//        HttpServletRequest httpRequest) {
//
//        WithdrawalSearchDto searchDto = new WithdrawalSearchDto();
//        searchDto.setAffiliateId(affiliateId);
//        searchDto.setSortBy("requestedAt");
//        searchDto.setSortDirection("DESC");
//        searchDto.setPage(page);
//        searchDto.setSize(size);
//
//        log.info("Retrieving withdrawals by affiliate | AdminWithdrawalController | getWithdrawalsByAffiliate | adminUser={}, affiliateId={}",
//                 currentUserService.getAdminUserName(httpRequest.getHeader(AUTH_HEADER)), affiliateId);
//        return withdrawalService.getAffiliateWithdrawals(
//            affiliateId, searchDto);
//    }
//
//    /**
//     * Build search DTO from request parameters
//     */
//    private WithdrawalSearchDto buildSearchDto(Long affiliateId, String status,
//                                               String paymentMethod,
//                                               String startDate, String endDate, String minAmount,
//                                               String maxAmount, String sortBy,
//                                               String sortDirection, int page, int size) {
//
//        WithdrawalSearchDto searchDto = new WithdrawalSearchDto();
//
//        searchDto.setAffiliateId(affiliateId);
//
//        if (status != null && !status.isEmpty()) {
//            try {
//                searchDto.setStatus(
//                    AffiliateWithdrawal.WithdrawalStatus.valueOf(status.toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid status filter | AdminWithdrawalController | buildSearchDto | status={}", status);
//            }
//        }
//
//        if (paymentMethod != null && !paymentMethod.isEmpty()) {
//            try {
//                searchDto.setPaymentMethod(
//                    AffiliateWithdrawal.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid payment method filter | AdminWithdrawalController | buildSearchDto | paymentMethod={}", paymentMethod);
//            }
//        }
//
//        if (startDate != null && !startDate.isEmpty()) {
//            try {
//                searchDto.setStartDate(java.sql.Timestamp.valueOf(startDate + " 00:00:00"));
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid start date | AdminWithdrawalController | buildSearchDto | startDate={}", startDate);
//            }
//        }
//
//        if (endDate != null && !endDate.isEmpty()) {
//            try {
//                searchDto.setEndDate(java.sql.Timestamp.valueOf(endDate + " 23:59:59"));
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid end date | AdminWithdrawalController | buildSearchDto | endDate={}", endDate);
//            }
//        }
//
//        if (minAmount != null && !minAmount.isEmpty()) {
//            try {
//                searchDto.setMinAmount(new java.math.BigDecimal(minAmount));
//            } catch (NumberFormatException e) {
//                log.warn("Invalid min amount | AdminWithdrawalController | buildSearchDto | minAmount={}", minAmount);
//            }
//        }
//
//        if (maxAmount != null && !maxAmount.isEmpty()) {
//            try {
//                searchDto.setMaxAmount(new java.math.BigDecimal(maxAmount));
//            } catch (NumberFormatException e) {
//                log.warn("Invalid max amount | AdminWithdrawalController | buildSearchDto | maxAmount={}", maxAmount);
//            }
//        }
//
//        searchDto.setSortBy(sortBy);
//        searchDto.setSortDirection(sortDirection);
//        searchDto.setPage(page);
//        searchDto.setSize(size);
//
//        return searchDto;
//    }
//}
