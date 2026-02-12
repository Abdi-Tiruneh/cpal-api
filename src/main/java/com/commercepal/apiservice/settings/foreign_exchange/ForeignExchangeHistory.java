package com.commercepal.apiservice.settings.foreign_exchange;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "foreign_exchange_history", indexes = {
    @Index(name = "idx_foreign_exchange_id", columnList = "foreign_exchange_id"),
    @Index(name = "idx_base_currency", columnList = "base_currency"),
    @Index(name = "idx_target_currency", columnList = "target_currency"),
    @Index(name = "idx_changed_at", columnList = "changed_at"),
    @Index(name = "idx_changed_by_full_name", columnList = "changed_by_full_name"),
    @Index(name = "idx_foreign_exchange_changed_at", columnList = "foreign_exchange_id, changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForeignExchangeHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "foreign_exchange_id", nullable = false)
  private ForeignExchange foreignExchange;

  @Column(name = "base_currency", length = 3, nullable = false)
  @Enumerated(EnumType.STRING)
  private SupportedCurrency baseCurrency;

  @Column(name = "target_currency", length = 3, nullable = false)
  @Enumerated(EnumType.STRING)
  private SupportedCurrency targetCurrency;

  @Column(nullable = false, precision = 18, scale = 4)
  private BigDecimal rate;

  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;

  @Column(name = "changed_by")
  private String changedBy;

  @Column(name = "changed_by_full_name", length = 200)
  private String changedByFullName;
}

