package com.commercepal.apiservice.promotions.affiliate.referral.dto;

import jakarta.validation.constraints.*;

public record ReferralSignupConversionRequest(
        @NotBlank(message = "Session ID is required") String sessionId,
        @NotNull(message = "Customer ID is required") Long customerId
) {}
