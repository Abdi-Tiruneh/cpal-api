package com.commercepal.apiservice.promotions.affiliate.dashboard;

import com.commercepal.apiservice.promotions.affiliate.commission.AffiliateCommission;
import com.commercepal.apiservice.promotions.affiliate.commission.CommissionType;
import com.commercepal.apiservice.promotions.affiliate.referral.AffiliateReferral;
import com.commercepal.apiservice.promotions.affiliate.user.Affiliate;
import com.commercepal.apiservice.promotions.affiliate.commission.AffiliateCommissionRepository;
import com.commercepal.apiservice.promotions.affiliate.referral.AffiliateReferralRepository;
import com.commercepal.apiservice.promotions.affiliate.user.AffiliateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/affiliate/mock")
@RequiredArgsConstructor
public class AffiliateMockController {

    private final AffiliateRepository affiliateRepository;
    private final AffiliateCommissionRepository commissionRepository;
    private final AffiliateReferralRepository referralRepository;

    private final Random random = new Random();

    @Transactional
    @GetMapping("/seed")
    public ResponseEntity<String> seedMockData() {
        List<Affiliate> affiliates = affiliateRepository.findAll();
        if (affiliates.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("No affiliates found. Please create at least one affiliate.");
        }

        Affiliate affiliate = affiliates.get(0); // Use first affiliate for seeding
        LocalDateTime now = LocalDateTime.now();

        // Fetch max IDs to avoid duplicates
        AtomicLong lastOrderId = new AtomicLong(
            commissionRepository.findMaxOrderIdByAffiliateId(affiliate.getId()).orElse(0L));
        AtomicLong lastCustomerId = new AtomicLong(
            commissionRepository.findMaxCustomerIdByAffiliateId(affiliate.getId()).orElse(0L));

        List<AffiliateCommission> commissionBatch = IntStream.range(0, 500).mapToObj(i -> {
            AffiliateCommission commission = new AffiliateCommission();
            commission.setAffiliate(affiliate);

            CommissionType type =
                random.nextBoolean() ? CommissionType.ORDER : CommissionType.SIGNUP;
            commission.setCommissionType(type);

            // Ensure unique orderId
            long orderId = lastOrderId.incrementAndGet();
            while (commissionRepository.existsByAffiliateIdAndOrderId(affiliate.getId(), orderId)) {
                orderId = lastOrderId.incrementAndGet();
            }
            commission.setOrderId(orderId);

            // Ensure unique customerId for SIGNUP
            long customerId;
            if (type == CommissionType.SIGNUP) {
                customerId = lastCustomerId.incrementAndGet();
                while (commissionRepository.existsByAffiliateIdAndCustomerIdAndCommissionType(
                    affiliate.getId(), customerId, CommissionType.SIGNUP)) {
                    customerId = lastCustomerId.incrementAndGet();
                }
            } else {
                customerId = lastCustomerId.incrementAndGet(); // ORDER commissions just get next ID
            }
            commission.setCustomerId(customerId);

            // Base amount and commission amount
            BigDecimal baseAmount = BigDecimal.valueOf(random.nextDouble() * 1000)
                .setScale(4, RoundingMode.HALF_UP);
            commission.setBaseAmount(baseAmount);

            BigDecimal commissionAmount = baseAmount.multiply(
                    BigDecimal.valueOf(0.05 + (0.1 * random.nextDouble())))
                .setScale(4, RoundingMode.HALF_UP);
            commission.setCommissionAmount(commissionAmount);

            // Random createdAt in last 90 days
            LocalDateTime randomDateTime = now
                .minusDays(random.nextInt(90))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60))
                .truncatedTo(ChronoUnit.MINUTES);
            Timestamp ts = Timestamp.valueOf(randomDateTime);
            commission.setCreatedAt(ts);
            commission.setUpdatedAt(ts);

            commission.setPaid(random.nextBoolean());

            return commission;
        }).collect(Collectors.toList());

        // Batch save commissions
        commissionRepository.saveAll(commissionBatch);

        // Seed referrals
        List<AffiliateReferral> referralBatch = IntStream.range(0, 500).mapToObj(i -> {
            AffiliateReferral referral = new AffiliateReferral();
            referral.setAffiliate(affiliate);

            AtomicLong sessionCounter = new AtomicLong(1);
            referral.setSessionId(UUID.randomUUID().toString());
            referral.setIpAddress("192.168." + random.nextInt(256) + "." + random.nextInt(256));
            referral.setUserAgent("MockAgent/" + random.nextInt(100));
            referral.setReferredUrl("https://example.com/product/" + random.nextInt(100));
            referral.setAttributionWindowDays(30);

            LocalDateTime firstSeen = now
                .minusDays(random.nextInt(90))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60))
                .truncatedTo(ChronoUnit.MINUTES);
            referral.setFirstSeenAt(firstSeen);

            LocalDateTime lastSeen = firstSeen.plusHours(random.nextInt(48));
            referral.setLastSeenAt(lastSeen);

            referral.setViewCount(random.nextInt(20) + 1);
            referral.setSignupConverted(random.nextBoolean());
            referral.setSignupAt(
                referral.getSignupConverted() ? firstSeen.plusHours(random.nextInt(24)) : null);

            referral.setOrderConverted(random.nextBoolean());
            referral.setOrderRef(
                referral.getOrderConverted() ? "ORD-" + random.nextInt(500) + 1 : null);
            referral.setOrderAmount(
                referral.getOrderConverted() ? BigDecimal.valueOf(random.nextDouble() * 500)
                    .setScale(2, RoundingMode.HALF_UP) : null);
            referral.setOrderAt(
                referral.getOrderConverted() ? firstSeen.plusHours(random.nextInt(48)) : null);

            referral.setCustomerId((long) random.nextInt(1000) + 1);

            return referral;
        }).collect(Collectors.toList());

        referralRepository.saveAll(referralBatch);

        return ResponseEntity.ok(
            "Seeded 500 AffiliateCommission and 500 AffiliateReferral records with uniqueness enforced.");
    }

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<String> clearMockData() {
        commissionRepository.deleteAll();
        referralRepository.deleteAll();
        return ResponseEntity.ok("Cleared all AffiliateCommission and AffiliateReferral records.");
    }
}
