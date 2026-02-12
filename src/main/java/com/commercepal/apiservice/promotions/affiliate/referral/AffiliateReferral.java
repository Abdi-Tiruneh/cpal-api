package com.commercepal.apiservice.promotions.affiliate.referral;

import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_referrals",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_affiliate_referrals_session", columnNames = {"affiliate_id",
            "session_id"})},
    indexes = {@Index(name = "idx_affiliate_referrals_session", columnList = "session_id"),
        @Index(name = "idx_affiliate_referrals_affiliate", columnList = "affiliate_id"),
        @Index(name = "idx_affiliate_referrals_customer", columnList = "customer_id"),
        @Index(name = "idx_affiliate_referrals_last_seen", columnList = "last_seen_at")})
@Getter
@Setter
public class AffiliateReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who gets the credit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private Affiliate affiliate;

    // identifies the browser/app session (cookie, localStorage id, device fingerprint, etc.)
    @Column(name = "session_id", nullable = false, length = 128)
    private String sessionId;

    // analytics / fraud review
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "referred_url", length = 1024)
    private String referredUrl;

    // window & dedup
    @Column(name = "attribution_window_days", nullable = false)
    private Integer attributionWindowDays = 30;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt = LocalDateTime.now();

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt = LocalDateTime.now();

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0; // only incremented once per 24h per session

    // signup conversion
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "signup_converted", nullable = false)
    private Boolean signupConverted = false;

    @Column(name = "signup_at")
    private LocalDateTime signupAt;

    // order conversion (last-touch per session)
    @Column(name = "order_ref")
    private String orderRef;

    @Column(name = "order_amount")
    private BigDecimal orderAmount;

    @Column(name = "order_converted", nullable = false)
    private Boolean orderConverted = false;

    @Column(name = "order_at")
    private LocalDateTime orderAt;
}
