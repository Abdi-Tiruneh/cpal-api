package com.commercepal.apiservice.settings.foreign_exchange;

import com.commercepal.apiservice.shared.BaseAuditEntity;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Foreign Exchange rate between two currencies. This entity is optimized for high-read
 * frequency and fast lookups.
 */
@Entity
@Table(
    name = "foreign_exchange",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_base_target",
            columnNames = {"base_currency", "target_currency"}
        )
    },
    indexes = {
        @Index(
            name = "idx_base_target",
            columnList = "base_currency, target_currency"
        ),
        @Index(
            name = "idx_updated_at",
            columnList = "updated_at"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForeignExchange extends BaseAuditEntity {

  @Column(name = "base_currency", nullable = false, length = 3)
  @Enumerated(EnumType.STRING)
  private SupportedCurrency baseCurrency;

  @Column(name = "target_currency", nullable = false, length = 3)
  @Enumerated(EnumType.STRING)
  private SupportedCurrency targetCurrency;

  /**
   * The exchange rate value (e.g., 1 USD = 181.2000 ETB).
   */
  @Column(nullable = false, precision = 18, scale = 6)
  private BigDecimal rate;
}

