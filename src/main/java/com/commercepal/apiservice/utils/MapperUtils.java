package com.commercepal.apiservice.utils;

import java.util.function.Consumer;

/**
 * Common helper methods for mapper classes across the service layer.
 */
public final class MapperUtils {

  private MapperUtils() {
    // Utility class
  }

  public static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  public static void applyIfNotBlank(String value, Consumer<String> consumer) {
    if (consumer == null || value == null) {
      return;
    }
    String trimmed = value.trim();
    if (!trimmed.isEmpty()) {
      consumer.accept(trimmed);
    }
  }

  public static <T> void applyIfNotNull(T value, Consumer<T> consumer) {
    if (consumer != null && value != null) {
      consumer.accept(value);
    }
  }
}

