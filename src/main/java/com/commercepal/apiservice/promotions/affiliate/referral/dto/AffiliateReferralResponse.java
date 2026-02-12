package com.commercepal.apiservice.promotions.affiliate.referral.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AffiliateReferralResponse(
    String ipAddress,
    String userAgent,
    String referredUrl,
    Integer attributionWindowDays,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime firstSeenAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastSeenAt,
    Integer viewCount,
    Long customerId,
    Boolean signupConverted,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime signupAt,
    String orderRef,
    BigDecimal orderAmount,
    Boolean orderConverted,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime orderAt
) {

}
