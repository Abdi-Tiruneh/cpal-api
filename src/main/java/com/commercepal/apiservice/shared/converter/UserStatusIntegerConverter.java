package com.commercepal.apiservice.shared.converter;

import com.commercepal.apiservice.users.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts between UserStatus enum and Integer (ordinal value). Used when database stores status as
 * integers instead of strings.
 */
@Converter(autoApply = false)
public class UserStatusIntegerConverter implements AttributeConverter<UserStatus, Integer> {

  @Override
  public Integer convertToDatabaseColumn(UserStatus status) {
    if (status == null) {
      return null;
    }
    // Convert enum to its ordinal value
    return status.ordinal();
  }

  @Override
  public UserStatus convertToEntityAttribute(Integer dbData) {
    if (dbData == null) {
      return null;
    }
    // Convert integer to enum by ordinal
    UserStatus[] values = UserStatus.values();
    if (dbData >= 0 && dbData < values.length) {
      return values[dbData];
    }
    // If value is out of range, default to INACTIVE as a safe fallback
    // You might want to log a warning here in production
    return UserStatus.INACTIVE;
  }
}




