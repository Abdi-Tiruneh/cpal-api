package com.commercepal.apiservice.promotions.affiliate.referral;

import com.commercepal.apiservice.promotions.affiliate.referral.dto.AffiliateReferralResponse;

public class AffiliateReferralMapper {

    AffiliateReferralMapper() {
    }

    public static AffiliateReferralResponse toResponse(AffiliateReferral referral) {
        if (referral == null) {
            return null;
        }

        return new AffiliateReferralResponse(
            referral.getIpAddress(),
            referral.getUserAgent(),
            referral.getReferredUrl(),
            referral.getAttributionWindowDays(),
            referral.getFirstSeenAt(),
            referral.getLastSeenAt(),
            referral.getViewCount(),
            referral.getCustomerId(),
            referral.getSignupConverted(),
            referral.getSignupAt(),
            referral.getOrderRef(),
            referral.getOrderAmount(),
            referral.getOrderConverted(),
            referral.getOrderAt()
        );
    }
}
