package com.commercepal.apiservice.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd HH:mm:ss");

  DateTimeUtil() {
  }

  public static String formatLocalDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(FORMATTER);
  }

  public static LocalDateTime parseLocalDateTime(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }
    return LocalDateTime.parse(dateString, FORMATTER);
  }
}