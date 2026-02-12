package com.commercepal.apiservice.promotions.affiliate.user.dto;

import com.commercepal.apiservice.promotions.affiliate.commission.Commission;
import com.commercepal.apiservice.shared.enums.Channel;
import jakarta.validation.constraints.*;

public record AffiliateMinAddRequest(

        // Affiliate Program Details
        @NotNull(message = "Commission type is required") Commission commissionType,

        @Size(min = 4, max = 8, message = "Referral code must be between 4 and 8 characters")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Referral code must be alphanumeric only")
        String referralCode,

        @NotNull(message = "Registration channel is required")
        Channel registrationChannel,

        // Optional Fields
        String deviceId
) {}
