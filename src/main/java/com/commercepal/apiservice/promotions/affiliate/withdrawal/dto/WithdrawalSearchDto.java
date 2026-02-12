package com.commercepal.apiservice.promotions.affiliate.withdrawal.dto;

import com.commercepal.apiservice.promotions.affiliate.withdrawal.AffiliateWithdrawal;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class WithdrawalSearchDto {

    private Long affiliateId;
    private AffiliateWithdrawal.WithdrawalStatus status;
    private AffiliateWithdrawal.PaymentMethod paymentMethod;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Timestamp startDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Timestamp endDate;
    
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    
    private String sortBy = "requestedAt"; // requestedAt, amount, status
    private String sortDirection = "DESC"; // ASC, DESC
    
    private int page = 0;
    private int size = 20;
}
