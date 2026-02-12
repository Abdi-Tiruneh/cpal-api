package com.commercepal.apiservice.promotions.affiliate.user.dto;

import com.commercepal.apiservice.promotions.affiliate.commission.Commission;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AffiliatePortalConfigRequest(

        @NotBlank(message = "Referral code is required")
        @Size(min = 4, max = 8, message = "Referral code must be between 4 and 8 characters")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Referral code must be alphanumeric only")
        String referralCode,

        @NotNull(message = "Commission type is required") Commission commissionType,

        @NotNull(message = "Commission rate is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Commission rate must be greater than or equal to 0")
        BigDecimal commissionRate
) {}
