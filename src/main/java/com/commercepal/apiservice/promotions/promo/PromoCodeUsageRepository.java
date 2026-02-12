package com.commercepal.apiservice.promotions.promo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {
    Optional<PromoCodeUsage> findByPromoCodeIdAndCustomerId(Long promoCodeId, Long customerId);

    // Count total usage of a promo code (regardless of customer)
    Integer countByPromoCodeId(Long promoCodeId);

    // Count usage of a promo code by a specific customer
    Integer countByPromoCodeIdAndCustomerId(Long promoCodeId, Long customerId);
}
