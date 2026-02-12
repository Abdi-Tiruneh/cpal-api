package com.commercepal.apiservice.promotions.affiliate.common.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CommissionCreateRequest {

    @NotNull
    private Long affiliateId;

    @NotNull
    private Long orderId;

    @NotNull
    private Long customerId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal orderAmount;
}
