package com.commercepal.apiservice.promotions.affiliate.referral.dto;

import jakarta.validation.constraints.*;

public record ReferralTrackViewRequest(
        @NotBlank(message = "Affiliate code is required") String affiliateCode,
        @Size(max = 1024) String referredUrl,
        @Size(max = 512) String userAgent
) {}



