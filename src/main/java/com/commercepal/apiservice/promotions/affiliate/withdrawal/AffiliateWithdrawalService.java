package com.commercepal.apiservice.promotions.affiliate.withdrawal;

import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.*;
import com.commercepal.apiservice.promotions.affiliate.withdrawal.validation.WithdrawalValidator;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.promotions.affiliate.user.AffiliateService;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import com.commercepal.apiservice.utils.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
@Slf4j
public class AffiliateWithdrawalService {

    private final AffiliateWithdrawalRepository withdrawalRepository;
    private final WithdrawalValidator withdrawalValidator;
    private final AffiliateService affiliateService;

    /**
     * Create a new withdrawal request by affiliate
     */
    @Transactional
    public AffiliateWithdrawal createWithdrawalRequest(Affiliate affiliate,
                                                       WithdrawalRequestDto requestDto) {

        if (affiliate.getIsActive() == null || !affiliate.getIsActive()) {
            throw new BadRequestException("Affiliate account is not active");
        }

        // Check if affiliate has pending withdrawal
        if (withdrawalRepository.existsByAffiliateIdAndStatus(affiliate.getId(),
                                                              AffiliateWithdrawal.WithdrawalStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending withdrawal request");
        }

        // Validate withdrawal request
        withdrawalValidator.validateWithdrawalRequest(requestDto, affiliate);

        // Create withdrawal request
        AffiliateWithdrawal withdrawal = new AffiliateWithdrawal();
        withdrawal.setAffiliate(affiliate);
        withdrawal.setAmount(requestDto.getAmount());
        withdrawal.setPaymentMethod(requestDto.getPaymentMethod());
        withdrawal.setAccountNumber(requestDto.getAccountNumber());
        withdrawal.setBankName(requestDto.getBankName());
        withdrawal.setNotes(requestDto.getNotes());
        withdrawal.setStatus(AffiliateWithdrawal.WithdrawalStatus.PENDING);
        withdrawal.setRequestedAt(new Timestamp(System.currentTimeMillis()));
        withdrawal.setCreatedBy(affiliate.getEmail());

        AffiliateWithdrawal savedWithdrawal = withdrawalRepository.save(withdrawal);

        log.info("Withdrawal request created | AffiliateWithdrawalService | createWithdrawalRequest | affiliateId={}, withdrawalId={}",
                 affiliate.getId(), savedWithdrawal.getId());

        return savedWithdrawal;
    }

    /**
     * Get all withdrawals for a specific affiliate with pagination and filtering
     */
    public ResponseEntity<ResponseWrapper<PagedResponse<WithdrawalResponseDto>>> getAffiliateWithdrawals(
        Long affiliateId,
        WithdrawalSearchDto searchDto) {
        Affiliate affiliate = affiliateService.getById(affiliateId);

        Pageable pageable = createPageable(searchDto);
        Page<AffiliateWithdrawal> withdrawalPage;

        // Apply filters
        if (searchDto.getStatus() != null && searchDto.getStartDate() != null
            && searchDto.getEndDate() != null) {
            withdrawalPage = withdrawalRepository.findByAffiliateAndStatusAndRequestedAtBetween(
                affiliate,
                searchDto.getStatus(), searchDto.getStartDate(), searchDto.getEndDate(), pageable);
        } else if (searchDto.getStatus() != null) {
            withdrawalPage = withdrawalRepository.findByAffiliateAndStatus(
                affiliate,
                searchDto.getStatus(), pageable);
        } else if (searchDto.getStartDate() != null && searchDto.getEndDate() != null) {
            withdrawalPage = withdrawalRepository.findByAffiliateAndRequestedAtBetween(affiliate,
                                                                                       searchDto.getStartDate(),
                                                                                       searchDto.getEndDate(),
                                                                                       pageable);
        } else {
            withdrawalPage = withdrawalRepository.findByAffiliate(affiliate,
                                                                  pageable);
        }

        Page<WithdrawalResponseDto> responsePage = withdrawalPage.map(this::convertToResponseDto);

        return ResponseWrapper.success(responsePage);
    }

    /**
     * Get all withdrawals for a specific affiliate with pagination and filtering
     */
    public Page<AffiliateWithdrawal> getAffiliateWithdrawals(Affiliate affiliate,
                                                             WithdrawalSearchDto searchDto) {
        Pageable pageable = createPageable(searchDto);
        Page<AffiliateWithdrawal> withdrawals;

        // Apply filters
        if (searchDto.getStatus() != null && searchDto.getStartDate() != null
            && searchDto.getEndDate() != null) {
            withdrawals = withdrawalRepository.findByAffiliateAndStatusAndRequestedAtBetween(
                affiliate,
                searchDto.getStatus(), searchDto.getStartDate(), searchDto.getEndDate(), pageable);
        } else if (searchDto.getStatus() != null) {
            withdrawals = withdrawalRepository.findByAffiliateAndStatus(
                affiliate,
                searchDto.getStatus(), pageable);
        } else if (searchDto.getStartDate() != null && searchDto.getEndDate() != null) {
            withdrawals = withdrawalRepository.findByAffiliateAndRequestedAtBetween(affiliate,
                                                                                    searchDto.getStartDate(),
                                                                                    searchDto.getEndDate(),
                                                                                    pageable);
        } else {
            withdrawals = withdrawalRepository.findByAffiliate(affiliate,
                                                               pageable);
        }

        return withdrawals;
    }

    /**
     * Get all withdrawals for admin with pagination and filtering
     */
    public ResponseEntity<ResponseWrapper<PagedResponse<WithdrawalResponseDto>>> getAllWithdrawals(
        WithdrawalSearchDto searchDto) {
        Pageable pageable = createPageable(searchDto);
        Page<AffiliateWithdrawal> withdrawalPage;

        // Apply filters
        if (searchDto.getStatus() != null && searchDto.getStartDate() != null
            && searchDto.getEndDate() != null) {
            withdrawalPage = withdrawalRepository.findByStatusAndRequestedAtBetween(
                searchDto.getStatus(),
                searchDto.getStartDate(), searchDto.getEndDate(), pageable);
        } else if (searchDto.getStatus() != null) {
            withdrawalPage = withdrawalRepository.findByStatus(
                searchDto.getStatus(), pageable);
        } else if (searchDto.getStartDate() != null && searchDto.getEndDate() != null) {
            withdrawalPage = withdrawalRepository.findByRequestedAtBetween(
                searchDto.getStartDate(),
                searchDto.getEndDate(), pageable);
        } else {
            withdrawalPage = withdrawalRepository.findAll(pageable);
        }

        Page<WithdrawalResponseDto> responsePage = withdrawalPage.map(this::convertToResponseDto);

        return ResponseWrapper.success(responsePage);
    }

    /**
     * Update withdrawal status by admin
     */
    @Transactional
    public WithdrawalResponseDto updateWithdrawalStatus(Long withdrawalId,
                                                        WithdrawalUpdateDto updateDto,
                                                        String adminUser) {
        AffiliateWithdrawal withdrawal = withdrawalRepository.findById(
            withdrawalId).orElseThrow(() -> new ResourceNotFoundException("Withdrawal not found"));

        // Validate status transition
        withdrawalValidator.validateStatusTransition(withdrawal.getStatus(),
                                                     updateDto.getStatus());

        // Update withdrawal
        withdrawal.setStatus(updateDto.getStatus());
        withdrawal.setProcessedAt(new Timestamp(System.currentTimeMillis()));
        withdrawal.setProcessedBy(adminUser);
        withdrawal.setAdminNotes(updateDto.getAdminNotes());
        withdrawal.setUpdatedBy(adminUser);

        if (updateDto.getStatus() == AffiliateWithdrawal.WithdrawalStatus.REJECTED) {
            withdrawal.setRejectionReason(updateDto.getRejectionReason());
        }

        AffiliateWithdrawal updatedWithdrawal = withdrawalRepository.save(withdrawal);

        log.info("Withdrawal status updated | AffiliateWithdrawalService | updateWithdrawalStatus | withdrawalId={}, status={}, adminUser={}",
                 withdrawalId, updateDto.getStatus(), adminUser);

        return convertToResponseDto(updatedWithdrawal);
    }

    /**
     * Get withdrawal statistics
     */
    public WithdrawalStatsDto getWithdrawalStats(Affiliate affiliate) {
        WithdrawalStatsDto stats = new WithdrawalStatsDto();

        if (affiliate != null) {
            stats.setTotalWithdrawals(withdrawalRepository.countByAffiliate(affiliate));
            stats.setPendingWithdrawals(
                withdrawalRepository.countByAffiliateAndStatus(affiliate,
                                                               AffiliateWithdrawal.WithdrawalStatus.PENDING));
            stats.setApprovedWithdrawals(
                withdrawalRepository.countByAffiliateAndStatus(affiliate,
                                                               AffiliateWithdrawal.WithdrawalStatus.APPROVED));
            stats.setRejectedWithdrawals(
                withdrawalRepository.countByAffiliateAndStatus(affiliate,
                                                               AffiliateWithdrawal.WithdrawalStatus.REJECTED));
            stats.setPaidWithdrawals(withdrawalRepository.countByAffiliateAndStatus(affiliate,
                                                                                    AffiliateWithdrawal.WithdrawalStatus.PAID));

            stats.setTotalAmount(withdrawalRepository.getTotalAmountByAffiliate(affiliate));
            stats.setPendingAmount(
                withdrawalRepository.getTotalAmountByAffiliateAndStatus(affiliate,
                                                                        AffiliateWithdrawal.WithdrawalStatus.PENDING));
            stats.setApprovedAmount(
                withdrawalRepository.getTotalAmountByAffiliateAndStatus(affiliate,
                                                                        AffiliateWithdrawal.WithdrawalStatus.APPROVED));
            stats.setRejectedAmount(
                withdrawalRepository.getTotalAmountByAffiliateAndStatus(affiliate,
                                                                        AffiliateWithdrawal.WithdrawalStatus.REJECTED));
            stats.setPaidAmount(
                withdrawalRepository.getTotalAmountByAffiliateAndStatus(affiliate,
                                                                        AffiliateWithdrawal.WithdrawalStatus.PAID));
        } else {
            // Global stats
            stats.setTotalWithdrawals(withdrawalRepository.count());
            stats.setPendingWithdrawals(
                withdrawalRepository.countByStatus(
                    AffiliateWithdrawal.WithdrawalStatus.PENDING));
            stats.setApprovedWithdrawals(
                withdrawalRepository.countByStatus(
                    AffiliateWithdrawal.WithdrawalStatus.APPROVED));
            stats.setRejectedWithdrawals(
                withdrawalRepository.countByStatus(
                    AffiliateWithdrawal.WithdrawalStatus.REJECTED));
            stats.setPaidWithdrawals(
                withdrawalRepository.countByStatus(AffiliateWithdrawal.WithdrawalStatus.PAID));

            stats.setTotalAmount(withdrawalRepository.getTotalAmount());
            stats.setPendingAmount(
                withdrawalRepository.getTotalAmountByStatus(
                    AffiliateWithdrawal.WithdrawalStatus.PENDING));
            stats.setApprovedAmount(
                withdrawalRepository.getTotalAmountByStatus(
                    AffiliateWithdrawal.WithdrawalStatus.APPROVED));
            stats.setRejectedAmount(
                withdrawalRepository.getTotalAmountByStatus(
                    AffiliateWithdrawal.WithdrawalStatus.REJECTED));
            stats.setPaidAmount(
                withdrawalRepository.getTotalAmountByStatus(
                    AffiliateWithdrawal.WithdrawalStatus.PAID));
        }

        stats.normalizeScale();
        return stats;
    }

    /**
     * Get withdrawal by ID
     */
    public WithdrawalResponseDto getWithdrawalById(Long withdrawalId) {
        AffiliateWithdrawal withdrawal = withdrawalRepository.findById(
            withdrawalId).orElseThrow(() -> new ResourceNotFoundException("Withdrawal not found"));

        return convertToResponseDto(withdrawal);
    }

    /**
     * Convert entity to response DTO
     */
    private WithdrawalResponseDto convertToResponseDto(AffiliateWithdrawal withdrawal) {
        WithdrawalResponseDto dto = new WithdrawalResponseDto();
        dto.setId(withdrawal.getId());
        dto.setAmount(withdrawal.getAmount());
        dto.setPaymentMethod(withdrawal.getPaymentMethod());
        dto.setAccountNumber(withdrawal.getAccountNumber());
        dto.setBankName(withdrawal.getBankName());
        dto.setStatus(withdrawal.getStatus());
        dto.setRequestedAt(withdrawal.getRequestedAt());
        dto.setProcessedAt(withdrawal.getProcessedAt());
        dto.setNotes(withdrawal.getNotes());
        dto.setRejectionReason(withdrawal.getRejectionReason());
        dto.setAdminNotes(withdrawal.getAdminNotes());
        dto.setProcessedBy(withdrawal.getProcessedBy());
        dto.setCreatedAt(withdrawal.getCreatedAt());
        dto.setUpdatedAt(withdrawal.getUpdatedAt());

        // Add affiliate information
        if (withdrawal.getAffiliate() != null) {
            dto.setAffiliateName(
                withdrawal.getAffiliate().getFirstName() + " " + withdrawal.getAffiliate()
                    .getLastName());
            dto.setAffiliateEmail(withdrawal.getAffiliate().getEmail());
            dto.setAffiliatePhone(withdrawal.getAffiliate().getPhoneNumber());
            dto.setAffiliateReferralCode(withdrawal.getAffiliate().getReferralCode());
        }

        return dto;
    }

    /**
     * Create pageable object from search DTO
     */
    private Pageable createPageable(WithdrawalSearchDto searchDto) {
        Sort sort = Sort.by(Sort.Direction.fromString(searchDto.getSortDirection()),
                            searchDto.getSortBy());
        return PageRequest.of(searchDto.getPage(), searchDto.getSize(), sort);
    }

}
