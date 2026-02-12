package com.commercepal.apiservice.utils;

import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * Utility class for performing conditional updates on object fields.
 * <p>
 * Ensures that setters are only called when the new value is non-null (or non-blank) and actually
 * differs from the old value.
 */
@Component
public class ConditionalUpdateUtils {

  private ConditionalUpdateUtils() {
    // Utility class — prevent instantiation
  }

  public static boolean updateIfChanged(Consumer<String> setter, String oldValue, String newValue) {
    if (!isBlank(newValue) && !Objects.equals(newValue, oldValue)) {
      setter.accept(newValue);
      return true;
    }
    return false;
  }

  public static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  public static boolean isNull(Integer value) {
    return value == null;
  }

  public static <T> boolean updateIfChanged(Consumer<T> setter, T oldValue, T newValue) {
    if (newValue != null && !Objects.equals(newValue, oldValue)) {
      setter.accept(newValue);
      return true;
    }
    return false;
  }
}
