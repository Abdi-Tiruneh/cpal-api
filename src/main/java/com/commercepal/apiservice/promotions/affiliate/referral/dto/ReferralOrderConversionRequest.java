package com.commercepal.apiservice.promotions.affiliate.referral.dto;

import jakarta.validation.constraints.NotBlank;

public record ReferralOrderConversionRequest(
    @NotBlank(message = "Order reference is required") String orderRef
) {

}
