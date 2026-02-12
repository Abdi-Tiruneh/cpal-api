package com.commercepal.apiservice.promotions.affiliate.withdrawal.dto;

import com.commercepal.apiservice.promotions.affiliate.withdrawal.AffiliateWithdrawal;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class WithdrawalResponseDto {

    private Long id;
    private BigDecimal amount;
    private AffiliateWithdrawal.PaymentMethod paymentMethod;
    private String accountNumber;
    private String bankName;
    private AffiliateWithdrawal.WithdrawalStatus status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp requestedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp processedAt;
    
    private String notes;
    private String rejectionReason;
    private String adminNotes;
    private String processedBy;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updatedAt;
    
    // Affiliate information
    private String affiliateName;
    private String affiliateEmail;
    private String affiliatePhone;
    private String affiliateReferralCode;
}
