package com.commercepal.apiservice.settings.foreign_exchange;

import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ForeignExchangeHistoryRepository extends
    JpaRepository<ForeignExchangeHistory, Long> {

  @Query("""
          SELECT e FROM ForeignExchangeHistory e
          WHERE (:baseCurrency IS NULL OR e.baseCurrency = :baseCurrency)
            AND (:targetCurrency IS NULL OR e.targetCurrency = :targetCurrency)
      """)
  Page<ForeignExchangeHistory> findFilteredHistory(
      @Param("baseCurrency") SupportedCurrency baseCurrency,
      @Param("targetCurrency") SupportedCurrency targetCurrency,
      Pageable pageable
  );

  /**
   * Find all history records for a specific ForeignExchange entity. Results are ordered by
   * changedAt descending (most recent first).
   */
  Page<ForeignExchangeHistory> findByForeignExchangeOrderByChangedAtDesc(
      ForeignExchange foreignExchange,
      Pageable pageable
  );
}

