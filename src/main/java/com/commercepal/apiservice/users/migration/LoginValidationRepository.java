package com.commercepal.apiservice.users.migration;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginValidationRepository extends JpaRepository<LoginValidation, Long> {

  Optional<LoginValidation> findLoginValidationByEmailAddress(String email);

  LoginValidation findByEmailAddress(String email);

  Optional<LoginValidation> findByEmailAddressOrPhoneNumber(String email, String phone);

  Optional<LoginValidation> findLoginValidationByEmailAddressOrPhoneNumber(String email,
      String phone);

  Optional<LoginValidation> findLoginValidationByPhoneNumber(String phone);

  List<LoginValidation> findByPhoneNumber(String phone);

  LoginValidation findByOauthProviderUserId(String providerUserId);

  boolean existsByEmailAddress(String email);

  boolean existsByPhoneNumber(String phoneNumber);
}
