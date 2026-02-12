package com.commercepal.apiservice.promotions.affiliate.user;// ReferralCodeGenerator.java (utility)

import java.security.SecureRandom;

public class ReferralCodeGenerator
{

    ReferralCodeGenerator()
    {
    }

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String randomAlphaNumeric(int length)
    {
        if (length <= 0) throw new IllegalArgumentException("length must be > 0");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
