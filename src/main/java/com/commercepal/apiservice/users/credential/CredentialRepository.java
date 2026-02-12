package com.commercepal.apiservice.users.credential;

import com.commercepal.apiservice.users.enums.IdentityProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

  Optional<Credential> findByEmailAddress(String emailAddress);

  Optional<Credential> findByPhoneNumber(String phoneNumber);

  Optional<Credential> findByEmailAddressOrPhoneNumber(String emailAddress, String phoneNumber);

  Optional<Credential> findByIdentityProviderAndIdentityProviderUserId(
      IdentityProvider identityProvider, String identityProviderUserId);

  boolean existsByEmailAddress(String emailAddress);

  boolean existsByPhoneNumber(String phoneNumber);

  boolean existsByEmailAddressAndDeletedFalse(String emailAddress);

  boolean existsByPhoneNumberAndDeletedFalse(String phoneNumber);

  boolean existsByEmailAddressAndDeletedFalseAndIdNot(String emailAddress, Long id);

  boolean existsByPhoneNumberAndDeletedFalseAndIdNot(String phoneNumber, Long id);

  default Optional<Credential> findByIdentifier(String identifier) {
    return findByEmailAddressOrPhoneNumber(identifier, identifier);
  }

}
