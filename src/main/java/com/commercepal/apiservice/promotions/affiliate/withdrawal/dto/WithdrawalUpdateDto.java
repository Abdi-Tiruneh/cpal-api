package com.commercepal.apiservice.promotions.affiliate.withdrawal.dto;

import com.commercepal.apiservice.promotions.affiliate.withdrawal.AffiliateWithdrawal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawalUpdateDto {

    @NotNull(message = "Status is required")
    private AffiliateWithdrawal.WithdrawalStatus status;

    @Size(max = 500, message = "Admin notes must not exceed 500 characters")
    private String adminNotes;

    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason;
}
