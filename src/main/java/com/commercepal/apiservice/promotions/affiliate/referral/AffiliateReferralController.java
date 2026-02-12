package com.commercepal.apiservice.promotions.affiliate.referral;

import com.commercepal.apiservice.promotions.affiliate.referral.dto.AffiliateReferralResponse;
import com.commercepal.apiservice.promotions.affiliate.referral.dto.ReferralOrderConversionRequest;
import com.commercepal.apiservice.promotions.affiliate.referral.dto.ReferralTrackViewRequest;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/affiliate-referrals")
@RequiredArgsConstructor
public class AffiliateReferralController {

    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final String SESSION_HEADER_NAME = "X-Session-Id";

    private final AffiliateReferralService referralService;
    private final CurrentUserService currentUserService;

    /**
     * Tracks a new referral view and issues a session cookie (used for later signup/order
     * attribution).
     */
    @PostMapping("/track-view")
    public ResponseEntity<ResponseWrapper<String>> trackViewAndIssueSession(
        @Valid @RequestBody ReferralTrackViewRequest referralTrackViewRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // Resolve session & IP
        String sessionId = resolveSessionId(request);
        String clientIp = resolveClientIp(request);

        AffiliateReferral referral = referralService.trackViewAndIssueSession(
            referralTrackViewRequest, sessionId,
            clientIp);

        // Send back session cookie (30-day window)
        ResponseCookie cookie = ResponseCookie
            .from("affiliate_session", referral.getSessionId())
            .path("/")
            .maxAge(Duration.ofDays(30))
            .httpOnly(false)
            .sameSite("Lax")  // secure enough for referrals but allows redirect flows
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseWrapper.success(
            String.format(
                "Referral successfully tracked for affiliate '%s'. Total view count: %d.",
                referral.getAffiliate().getReferralCode(),
                referral.getViewCount()
            )
        );
    }

    /**
     * Extracts sessionId from header if present.
     */
    private String resolveSessionId(HttpServletRequest request) {
        String headerValue = request.getHeader(SESSION_HEADER_NAME);
        return (headerValue != null && !headerValue.isBlank()) ? headerValue : null;
    }

    /**
     * Extracts the best possible client IP, ignoring "unknown".
     */
    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


    @GetMapping("/my-referrals")
    public ResponseEntity<ResponseWrapper<PagedResponse<AffiliateReferralResponse>>> getReferralsByAffiliate(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Affiliate affiliate = currentUserService.getCurrentAffiliate();
        Long affiliateId = affiliate.getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastSeenAt"));
        Page<AffiliateReferral> referralsPage = referralService.getReferralsByAffiliate(
            affiliateId, pageable);

        Page<AffiliateReferralResponse> responsePage = referralsPage.map(
            AffiliateReferralMapper::toResponse);

        return ResponseWrapper.success(responsePage);
    }

    @PostMapping("/order-conversion")
    public ResponseEntity<ResponseWrapper<String>> trackOrderConversion(
        @RequestBody ReferralOrderConversionRequest request) {
        referralService.trackOrderConversion(request.orderRef());
        return ResponseWrapper.success(
            "Order conversion tracked successfully for orderRef: " + request.orderRef());
    }
}
