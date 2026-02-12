package com.commercepal.apiservice.promotions.affiliate.referral;

import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.promotions.affiliate.referral.dto.ReferralTrackViewRequest;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.promotions.affiliate.user.AffiliateRepository;
import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateReferralService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliateReferralRepository affiliateReferralRepository;
    private final OrderRepository orderRepository;

    // count at most 1 "view" per 2h per {affiliate, session}
    private static final Duration VIEW_DEDUP_DURATION = Duration.ofHours(2);

    @Transactional
    public AffiliateReferral trackViewAndIssueSession(ReferralTrackViewRequest request,
                                                      String sessionId, String clientIp) {
        final LocalDateTime now = LocalDateTime.now();

        // 1. Validate affiliate
        Affiliate affiliate = affiliateRepository.findByReferralCode(
            request.affiliateCode().toUpperCase()).orElseThrow(() -> new IllegalArgumentException(
            "Invalid affiliate code: " + request.affiliateCode()));

        // 2. Lookup referral by sessionId or IP
        AffiliateReferral referral = findExistingReferral(affiliate.getId(), sessionId, clientIp);

        // 3. Create new referral if none found
        if (referral == null) {
            referral = new AffiliateReferral();
            referral.setAffiliate(affiliate);
            referral.setSessionId(UUID.randomUUID().toString());
            referral.setFirstSeenAt(now);
            referral.setLastSeenAt(now);
            referral.setViewCount(0);
        }

        // 4. Increment view count if outside dedup window
        boolean shouldIncrement = referral.getViewCount() == 0 || referral.getLastSeenAt()
            .isBefore(now.minus(VIEW_DEDUP_DURATION));

        if (shouldIncrement) {
            referral.setViewCount(referral.getViewCount() + 1);
        }

        // 5. Update tracking info
        referral.setIpAddress(clientIp);
        referral.setUserAgent(request.userAgent());
        referral.setReferredUrl(request.referredUrl());
        referral.setLastSeenAt(now);

        // 6. Save
        affiliateReferralRepository.save(referral);

        // 7. Log
        log.info("View tracked successfully | AffiliateReferralService | trackViewAndIssueSession | affiliateId={}, sessionId={}, ip={}, views={}",
                 affiliate.getId(), referral.getSessionId(), clientIp, referral.getViewCount());

        return referral;
    }

    /**
     * Last-touch attribution for signup: pick the most recent referral recorded for this session
     * within the attribution window.
     */
//    @Transactional
//    public void trackSignupConversion(ReferralSignupConversionRequest req)
//    {
//        AffiliateReferral affiliateReferral = mostRecentAttributableReferral(req.sessionId()).orElseThrow(
//                () -> new IllegalArgumentException("No attributable referral found for session"));
//
//        if (!affiliateReferral.getSignupConverted()) {
//            affiliateReferral.setSignupConverted(true);
//            affiliateReferral.setSignupAt(LocalDateTime.now());
//            affiliateReferral.setCustomerId(req.customerId());
//            referralRepository.save(affiliateReferral);
//            log.info("Signup conversion attributed to affiliate={}, session={}",
//                    affiliateReferral.getAffiliate().getId(), affiliateReferral.getSessionId());
//        } else {
//            log.info("Signup conversion already recorded for session={}", affiliateReferral.getSessionId());
//        }
//    }
    @Transactional
    public void trackAffiliateOrder(String sessionId, Order order) {
        Optional<AffiliateReferral> referralOptional = mostRecentAttributableReferral(sessionId);

        if (referralOptional.isEmpty()) {
            log.info("No attributable referral found | AffiliateReferralService | trackAffiliateOrder | sessionId={}", sessionId);
            return;
        }

        AffiliateReferral referral = referralOptional.get();

        if (Boolean.FALSE.equals(referral.getOrderConverted())) {
            referral.setOrderConverted(false);
            referral.setOrderAt(LocalDateTime.now());
            referral.setOrderRef(order.getOrderNumber());
            referral.setOrderAmount(order.getTotalAmount());
            affiliateReferralRepository.save(referral);

            log.info("Affiliate order tracked | AffiliateReferralService | trackAffiliateOrder | affiliateId={}, sessionId={}, orderRef={}",
                     referral.getAffiliate().getId(), referral.getSessionId(), order.getOrderNumber());
        } else {
            log.info("Order already recorded | AffiliateReferralService | trackAffiliateOrder | sessionId={}, existingOrderRef={}",
                     referral.getSessionId(), order.getOrderNumber());
        }
    }

    /**
     * Last-touch attribution for order: pick the most recent referral recorded for this session
     * within the attribution window.
     */
    @Transactional
    public void trackOrderConversion(String orderRef) {
        AffiliateReferral referral = affiliateReferralRepository.findByOrderRef(orderRef)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "No attributable referral found for order: " + orderRef));

        if (Boolean.FALSE.equals(referral.getOrderConverted())) {
            referral.setOrderConverted(true);
            affiliateReferralRepository.save(referral);

            log.info("Order conversion attributed | AffiliateReferralService | trackOrderConversion | affiliateId={}, sessionId={}, orderRef={}",
                     referral.getAffiliate().getId(), referral.getSessionId(), orderRef);
        } else {
            log.info("Order conversion already recorded | AffiliateReferralService | trackOrderConversion | sessionId={}, existingOrderRef={}",
                     referral.getSessionId(), referral.getOrderRef());
        }
    }

    private Optional<AffiliateReferral> mostRecentAttributableReferral(String sessionId) {
        return affiliateReferralRepository.findTopBySessionIdOrderByLastSeenAtDesc(sessionId)
            .filter(referral -> {
                LocalDateTime lastSeen = referral.getLastSeenAt();
                LocalDateTime cutoff = LocalDateTime.now()
                    .minusDays(referral.getAttributionWindowDays());
                return lastSeen.isAfter(cutoff);
            });
    }

    private AffiliateReferral findExistingReferral(Long affiliateId, String sessionId,
                                                   String clientIp) {
        if (sessionId != null && !sessionId.isBlank()) {
            return affiliateReferralRepository.findByAffiliateIdAndSessionId(affiliateId, sessionId)
                .orElse(null);
        }
        if (isValidIp(clientIp)) {
            return affiliateReferralRepository.findByAffiliateIdAndIpAddress(affiliateId, clientIp)
                .orElse(null);
        }
        return null;
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)
            && !"0:0:0:0:0:0:0:1".equalsIgnoreCase(ip);
    }

    public Page<AffiliateReferral> getReferralsByAffiliate(Long affiliateId, Pageable pageable) {
        return affiliateReferralRepository.findAllByAffiliateId(affiliateId, pageable);
    }
}

//public void trackReferral(ReferralTrackRequest request)
//{
//    Affiliate affiliate = affiliateRepository
//            .findByReferralCode(request.getReferralCode().toUpperCase())
//            .orElseThrow(() -> new IllegalArgumentException("Invalid referral code"));
//
//    AffiliateReferral referral = new AffiliateReferral();
//    referral.setAffiliate(affiliate);
//    referral.setSessionId(request.getSessionId());
//    referral.setReferredUrl(request.getReferredUrl());
//    referral.setReferredAt(Timestamp.from(Instant.now()));
//    referral.setConverted(false);
//
//    referralRepository.save(referral);
//}
//
//public void convertReferral(ReferralConvertRequest request)
//{
//    AffiliateReferral referral = referralRepository
//            .findFirstBySessionIdAndConvertedFalseOrderByReferredAtDesc(request.getSessionId())
//            .orElseThrow(() -> new IllegalArgumentException("No unconverted referral found for session"));
//
//    referral.setCustomerId(request.getCustomerId());
//    referral.setConverted(true);
//    referral.setConversionDate(Timestamp.from(Instant.now()));
//
//    referralRepository.save(referral);
//}
//
//public List<AffiliateReferral> getByAffiliateId(Long affiliateId)
//{
//    return referralRepository.findAllByAffiliate_IdOrderByReferredAtDesc(affiliateId);
//}
//}
