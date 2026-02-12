package com.commercepal.apiservice.promotions.affiliate.withdrawal;

import com.commercepal.apiservice.promotions.affiliate.withdrawal.dto.WithdrawalResponseDto;

public class AffiliateWithdrawalMapper {

    public AffiliateWithdrawalMapper() {
    }

    /**
     * Convert entity to response DTO
     */
    public static WithdrawalResponseDto convertToResponseDto(AffiliateWithdrawal withdrawal) {
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

}
