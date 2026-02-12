package com.commercepal.apiservice.promotions.affiliate.common.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ReferralTrackRequest {
    @NotBlank
    private String referralCode;

    @NotBlank
    private String sessionId;

    private String referredUrl;
}
