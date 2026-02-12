package com.commercepal.apiservice.promotions.affiliate.user;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateRepository extends JpaRepository<Affiliate, Long>
{

    boolean existsByReferralCode(String referralCode);

    Optional<Affiliate> findByReferralCode(String referralCode);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmailOrPhoneNumber(String email, String phoneNumber);

    Optional<Affiliate> findByEmailOrPhoneNumber(String email, String phoneNumber);

    Optional<Affiliate> findByEmail(String email);

    Optional<Affiliate> findByPhoneNumber(String phoneNumber);

    List<Affiliate> findByIsActive(Boolean isActive, Sort sort);

    boolean existsByReferralCodeAndIdNot(String normalized, Long affiliateId);

    Optional<Affiliate> findByCredential_Id(Long credentialId);
}
