package com.commercepal.apiservice.promotions.affiliate.common.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReferralConvertRequest {

    @NotBlank
    private String sessionId;

    @NotNull
    private Long customerId;
}
