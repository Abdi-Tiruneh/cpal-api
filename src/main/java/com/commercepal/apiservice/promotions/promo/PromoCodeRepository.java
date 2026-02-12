package com.commercepal.apiservice.promotions.promo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCode(String code);

    @Query("SELECT p FROM PromoCode p WHERE p.isActive = true AND (p.startDate IS NULL OR p.startDate <= CURRENT_TIMESTAMP) AND (p.endDate IS NULL OR p.endDate >= CURRENT_TIMESTAMP)")
    List<PromoCode> findAllActive();

    List<PromoCode> findAllByOrderByUpdatedAtDesc();

    boolean existsByCode(String code);
}
