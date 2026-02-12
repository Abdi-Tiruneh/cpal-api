package com.commercepal.apiservice.promotions.affiliate.user.dto;

import com.commercepal.apiservice.promotions.affiliate.commission.Commission;
import java.math.BigDecimal;
import java.sql.Timestamp;

public record AffiliateResponse(
    String referralCode,
    String fullName,
    String email,
    String phoneNumber,
    Commission commissionType,
    BigDecimal commissionRate,
    Boolean isActive,
    Timestamp createdAt,
    Timestamp updatedAt
) {

}
