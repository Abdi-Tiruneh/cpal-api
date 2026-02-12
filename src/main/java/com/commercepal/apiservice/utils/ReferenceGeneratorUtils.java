package com.commercepal.apiservice.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReferenceGeneratorUtils {

  private static final String DEFAULT_PREFIX = "CP";
  private static final String UNAMBIGUOUS_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private static final int DEFAULT_RANDOM_LENGTH = 8;
  private static final int MAX_GENERATION_ATTEMPTS = 10;

  private ReferenceGeneratorUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static String generateUniqueReference(String prefix,
      Function<String, Boolean> uniquenessChecker) {
    return generateUniqueReference(prefix, DEFAULT_RANDOM_LENGTH, uniquenessChecker);
  }

  public static String generateUniqueReference(
      String prefix,
      int randomLength,
      Function<String, Boolean> uniquenessChecker
  ) {
    String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    int attempts = 0;

    while (attempts < MAX_GENERATION_ATTEMPTS) {
      String randomPart = generateUnambiguousRandomString(randomLength);
      String candidate = "%s-%s-%s".formatted(prefix, datePart, randomPart);

      if (!uniquenessChecker.apply(candidate)) {
        return candidate;
      }

      attempts++;
      log.debug("Reference collision detected for prefix {}, retrying (attempt {})", prefix,
          attempts);
    }

    String fallback = "%s-%s-%s".formatted(
        prefix,
        datePart,
        UUID.randomUUID().toString().substring(0, 8).toUpperCase()
    );
    log.warn(
        "Could not generate unique reference with prefix {} after {} attempts, using UUID fallback: {}",
        prefix, MAX_GENERATION_ATTEMPTS, fallback);
    return fallback;
  }

  public static String generateUnambiguousRandomString(int length) {
    char[] chars = new char[length];
    for (int i = 0; i < length; i++) {
      int index = ThreadLocalRandom.current().nextInt(UNAMBIGUOUS_CHARS.length());
      chars[i] = UNAMBIGUOUS_CHARS.charAt(index);
    }
    return new String(chars);
  }

  public static String generateDefaultPrefixReference(Function<String, Boolean> uniquenessChecker) {
    return generateUniqueReference(DEFAULT_PREFIX, uniquenessChecker);
  }

  public static String generateDefaultPrefixReference(
      int randomLength,
      Function<String, Boolean> uniquenessChecker
  ) {
    return generateUniqueReference(DEFAULT_PREFIX, randomLength, uniquenessChecker);
  }
}
