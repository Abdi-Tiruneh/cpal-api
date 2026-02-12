package com.commercepal.apiservice.promotions.affiliate.user;

import com.commercepal.apiservice.promotions.affiliate.user.dto.AffiliateResponse;

public class AffiliateMapper {

    AffiliateMapper() {
    }

    public static AffiliateResponse toResponse(Affiliate affiliate) {
        if (affiliate == null) {
            return null;
        }

        String fullName = affiliate.getFirstName() + " " + affiliate.getLastName();

        return new AffiliateResponse(
            affiliate.getReferralCode(),
            fullName.toUpperCase(),
            affiliate.getEmail(),
            affiliate.getPhoneNumber(),
            affiliate.getCommissionType(),
            affiliate.getCommissionRate(),
            affiliate.getIsActive(),
            affiliate.getCreatedAt(),
            affiliate.getUpdatedAt()
        );
    }
}
