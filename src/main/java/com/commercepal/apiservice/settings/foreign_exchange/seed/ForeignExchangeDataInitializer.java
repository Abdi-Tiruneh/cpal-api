package com.commercepal.apiservice.settings.foreign_exchange.seed;

import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchange;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeHistory;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeHistoryRepository;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeRepository;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class ForeignExchangeDataInitializer implements CommandLineRunner {

  private final ForeignExchangeRepository rateRepository;
  private final ForeignExchangeHistoryRepository historyRepository;

  @Override
  @Transactional
  public void run(String... args) {

    log.info("==========================================================");
    log.info("Starting Foreign Exchange Data Initialization...");
    log.info("==========================================================");

    try {
      seedForeignExchangeRates();
      log.info("==========================================================");
      log.info("Foreign Exchange Data Initialization Completed Successfully!");
      log.info("==========================================================");
    } catch (Exception e) {
      log.error("[FOREIGN_EXCHANGE_SEEDER] Error during foreign exchange data initialization", e);
      throw new RuntimeException("Failed to initialize foreign exchange data", e);
    }
  }

  private void seedForeignExchangeRates() {
    // Check if any foreign exchange rates already exist - if so, skip entire initialization
    if (rateRepository.count() > 0) {
      log.info("[FOREIGN_EXCHANGE_SEEDER] Foreign exchange rates already exist in database - skipping initialization");
      return;
    }

    List<RateData> ratesToSeed = List.of(
        new RateData(SupportedCurrency.USD, SupportedCurrency.ETB, new BigDecimal("180.000000")),
        new RateData(SupportedCurrency.USD, SupportedCurrency.SOS, new BigDecimal("570.000000")),
        new RateData(SupportedCurrency.USD, SupportedCurrency.KES, new BigDecimal("130.000000")),
        new RateData(SupportedCurrency.USD, SupportedCurrency.AED, new BigDecimal("3.672500"))
    );

    int createdCount = 0;
    int skippedCount = 0;

    for (RateData rateData : ratesToSeed) {
      if (rateRepository.findByBaseCurrencyAndTargetCurrency(rateData.base(), rateData.target())
          .isPresent()) {
        log.debug("[FOREIGN_EXCHANGE_SEEDER] Rate already exists: {} -> {}, skipping",
            rateData.base(), rateData.target());
        skippedCount++;
        continue;
      }

      log.info("[FOREIGN_EXCHANGE_SEEDER] Creating rate: {} -> {} = {}",
          rateData.base(), rateData.target(), rateData.rate());

      ForeignExchange foreignExchange = ForeignExchange.builder()
          .baseCurrency(rateData.base())
          .targetCurrency(rateData.target())
          .rate(rateData.rate())
          .build();

      ForeignExchange saved = rateRepository.save(foreignExchange);
      log.info("[FOREIGN_EXCHANGE_SEEDER] Created rate: ID={}, {} -> {} = {}",
          saved.getId(), saved.getBaseCurrency(), saved.getTargetCurrency(), saved.getRate());

      // Use UTC time slightly in the past to ensure it passes the constraint (changed_at <= GETUTCDATE())
      // This accounts for potential clock differences between application and database servers
      LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
      ForeignExchangeHistory history = ForeignExchangeHistory.builder()
          .foreignExchange(saved)
          .baseCurrency(saved.getBaseCurrency())
          .targetCurrency(saved.getTargetCurrency())
          .rate(saved.getRate())
          .changedAt(now)
          .changedBy("System")
          .changedByFullName("System Seeder")
          .build();

      historyRepository.save(history);
      log.info("[FOREIGN_EXCHANGE_SEEDER] Created history record: ID={}", history.getId());

      createdCount++;
    }

    log.info("[FOREIGN_EXCHANGE_SEEDER] Seeding complete: {} created, {} skipped",
        createdCount, skippedCount);
  }

  private record RateData(SupportedCurrency base, SupportedCurrency target, BigDecimal rate) {

  }
}

