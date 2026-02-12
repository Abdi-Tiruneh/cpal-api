package com.commercepal.apiservice.users.referral;

import com.commercepal.apiservice.users.customer.CustomerRepository;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReferralCodeUtils {


  private static final String REFERRAL_CODE_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // avoiding 'I', 'O', '1', '0'
  private static final int REFERRAL_CODE_LENGTH = 8;
  private static final String CUSTOMER_PREFIX = "CU";

  private final CustomerRepository customerRepository;


  public String generateCustomerReferralCode() {
    String referralCode = generateReferralCode(CUSTOMER_PREFIX);

    while (customerRepository.existsByReferralCode(referralCode)) {
      referralCode = generateReferralCode(CUSTOMER_PREFIX);
    }

    return referralCode;
  }


  private String generateReferralCode(String prefix) {
    int prefixLength = prefix.length();
    int codeLength = REFERRAL_CODE_LENGTH - prefixLength;
    char[] code = new char[REFERRAL_CODE_LENGTH];

    for (int i = 0; i < prefixLength; i++) {
      code[i] = prefix.charAt(i);
    }

    for (int i = 0; i < codeLength; i++) {
      int index = ThreadLocalRandom.current().nextInt(REFERRAL_CODE_CHARACTERS.length());
      code[prefixLength + i] = REFERRAL_CODE_CHARACTERS.charAt(index);
    }

    return new String(code);
  }


}
