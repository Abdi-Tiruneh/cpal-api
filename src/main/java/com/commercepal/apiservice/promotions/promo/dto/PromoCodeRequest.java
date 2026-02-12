package com.commercepal.apiservice.promotions.promo.dto;

import com.commercepal.apiservice.promotions.promo.DiscountType;
import com.commercepal.apiservice.promotions.promo.PromoCodeScope;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class PromoCodeRequest {

    @NotBlank
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull
    private BigDecimal discountValue;

    private BigDecimal minimumOrderAmount;

    private PromoCodeScope scope;

    private Long applicableProductId;
    private Long applicableCategoryId;
    private Long applicableCustomerId;

    private Timestamp startDate;

    private Timestamp endDate;

    private Integer totalUsageLimit;
    private Integer perCustomerUsageLimit;

    private Boolean isActive;
}
