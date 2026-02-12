package com.commercepal.apiservice.shared.api;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object representing monetary amounts. Immutable and type-safe representation of money.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Money {

  private BigDecimal amount;
  private Currency currency;

  public static Money of(BigDecimal amount, String currencyCode) {
    return new Money(amount, Currency.getInstance(currencyCode));
  }

  public static Money usd(BigDecimal amount) {
    return new Money(amount, Currency.getInstance("USD"));
  }

  public Money add(Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException("Cannot add different currencies");
    }
    return new Money(this.amount.add(other.amount), this.currency);
  }

  public Money multiply(BigDecimal factor) {
    return new Money(this.amount.multiply(factor), this.currency);
  }
}

