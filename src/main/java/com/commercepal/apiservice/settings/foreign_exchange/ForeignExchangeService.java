package com.commercepal.apiservice.settings.foreign_exchange;

import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeHistoryResponse;
import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeRequest;
import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeResponse;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.utils.CurrentUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForeignExchangeService {

  private static final Set<SupportedCurrency> SUPPORTED_TARGET_CURRENCIES = Set.of(
      SupportedCurrency.ETB, SupportedCurrency.KES, SupportedCurrency.SOS, SupportedCurrency.AED);

  private final ForeignExchangeRepository rateRepository;
  private final ForeignExchangeHistoryRepository historyRepository;
  private final CurrentUserService currentUserService;

  public Set<SupportedCurrency> getSupportedTargetCurrency() {
    log.debug("[FOREIGN_EXCHANGE] Getting supported target currencies");
    log.info("[FOREIGN_EXCHANGE] Retrieved {} supported target currencies",
        SUPPORTED_TARGET_CURRENCIES.size());
    return SUPPORTED_TARGET_CURRENCIES;
  }

  /**
   * Returns the exchange rate between USD and the target currency.
   *
   * @param target          The target currency (relative to USD).
   * @param fromTargetToUSD If true, converts FROM target currency TO USD. If false, returns USD TO
   *                        target currency.
   * @return BigDecimal rate with scale(6), HALF_UP rounding.
   */
  public BigDecimal getRate(SupportedCurrency target, boolean fromTargetToUSD) {
    log.debug("[FOREIGN_EXCHANGE] Getting rate for target: {}, fromTargetToUSD: {}", target,
        fromTargetToUSD);

    if (target == SupportedCurrency.USD) {
      log.debug("[FOREIGN_EXCHANGE] USD to USD conversion requested, returning 1.0");
      return BigDecimal.ONE;
    }

    ForeignExchange foreignExchange = rateRepository
        .findByBaseCurrencyAndTargetCurrency(SupportedCurrency.USD, target)
        .orElseThrow(() -> {
          log.error("[FOREIGN_EXCHANGE] Rate not found for USD -> {}", target);
          return new IllegalStateException("Foreign Exchange rate not found for USD -> " + target);
        });

    BigDecimal rate = foreignExchange.getRate();

    if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
      log.error("[FOREIGN_EXCHANGE] Invalid rate value {} for USD -> {}", rate, target);
      throw new IllegalStateException(
          "Invalid foreign exchange rate for USD -> " + target + ": " + rate);
    }

    BigDecimal result = fromTargetToUSD
        ? BigDecimal.ONE.divide(rate, 6, RoundingMode.HALF_UP)
        : rate;

    log.info("[FOREIGN_EXCHANGE] Retrieved rate {} for {} -> USD: {}",
        result, target.getCode(), fromTargetToUSD);
    return result;
  }

  /**
   * Gets the exchange rate from USD to the specified target currency.
   * <p>
   * This method always uses USD as the base currency. If the target currency is USD, it returns
   * 1.0. For other currencies, it retrieves the rate from the database.
   * </p>
   *
   * @param targetCurrency The target currency to convert to (e.g., ETB, KES, AED, SOS)
   * @return The exchange rate representing how many units of target currency equal one USD. Returns
   * BigDecimal.ONE if target is USD.
   * @throws IllegalStateException if the exchange rate is not found or is invalid
   */
  public BigDecimal getUsdToTargetRate(SupportedCurrency targetCurrency) {
    log.debug("[FOREIGN_EXCHANGE] Getting USD to target rate for: {}", targetCurrency);

    // Handle USD to USD conversion
    if (targetCurrency == SupportedCurrency.USD) {
      log.debug("[FOREIGN_EXCHANGE] USD to USD conversion, returning 1.0");
      return BigDecimal.ONE;
    }

    // Retrieve exchange rate from database
    ForeignExchange foreignExchange = rateRepository
        .findByBaseCurrencyAndTargetCurrency(SupportedCurrency.USD, targetCurrency)
        .orElseThrow(() -> {
          log.error("[FOREIGN_EXCHANGE] Exchange rate not found for USD -> {}", targetCurrency);
          return new IllegalStateException(
              String.format("Foreign exchange rate not found for USD -> %s", targetCurrency));
        });

    BigDecimal rate = foreignExchange.getRate();

    // Validate rate is positive
    if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
      log.error("[FOREIGN_EXCHANGE] Invalid exchange rate value {} for USD -> {}", rate,
          targetCurrency);
      throw new IllegalStateException(
          String.format("Invalid foreign exchange rate for USD -> %s: %s", targetCurrency, rate));
    }

    log.info("[FOREIGN_EXCHANGE] Retrieved exchange rate: 1 USD = {} {}", rate,
        targetCurrency.getCode());
    return rate;
  }

  /**
   * Gets the exchange rate from base currency to target currency.
   * <p>
   * This method handles conversions between any two currencies. If both currencies are the same, it
   * returns 1.0. If USD is involved, it uses direct lookups. For non-USD pairs, it calculates the
   * rate via USD (base -> USD -> target).
   * </p>
   *
   * @param baseCurrency   The base currency to convert from
   * @param targetCurrency The target currency to convert to
   * @return The exchange rate representing how many units of target currency equal one unit of base
   * currency. Returns BigDecimal.ONE if base and target are the same.
   * @throws IllegalStateException if the required exchange rate is not found or is invalid
   */
  public BigDecimal getExchangeRate(SupportedCurrency baseCurrency,
      SupportedCurrency targetCurrency) {
    log.debug("[FOREIGN_EXCHANGE] Getting exchange rate: {} -> {}", baseCurrency, targetCurrency);

    // Same currency conversion
    if (baseCurrency == targetCurrency) {
      log.debug("[FOREIGN_EXCHANGE] Same currency conversion {} -> {}, returning 1.0",
          baseCurrency, targetCurrency);
      return BigDecimal.ONE;
    }

    // Base is USD: direct lookup
    if (baseCurrency == SupportedCurrency.USD) {
      return getUsdToTargetRate(targetCurrency);
    }

    // Target is USD: get inverse of USD -> base
    if (targetCurrency == SupportedCurrency.USD) {
      BigDecimal baseToUsdRate = getUsdToTargetRate(baseCurrency);
      BigDecimal usdToBaseRate = BigDecimal.ONE.divide(baseToUsdRate, 6, RoundingMode.HALF_UP);
      log.info("[FOREIGN_EXCHANGE] Retrieved exchange rate: 1 {} = {} USD",
          baseCurrency.getCode(), usdToBaseRate);
      return usdToBaseRate;
    }

    // Both are non-USD: convert via USD (base -> USD -> target)
    BigDecimal baseToUsdRate = getUsdToTargetRate(baseCurrency);
    BigDecimal usdToTargetRate = getUsdToTargetRate(targetCurrency);

    // If 1 base = baseToUsdRate USD, and 1 USD = usdToTargetRate target,
    // then 1 base = usdToTargetRate / baseToUsdRate target
    BigDecimal baseToTargetRate = usdToTargetRate.divide(baseToUsdRate, 6, RoundingMode.HALF_UP);
    log.info("[FOREIGN_EXCHANGE] Retrieved exchange rate via USD: 1 {} = {} {}",
        baseCurrency.getCode(), baseToTargetRate, targetCurrency.getCode());
    return baseToTargetRate;
  }

  public ForeignExchangeResponse setRate(ForeignExchangeRequest dto) {
    log.info("[FOREIGN_EXCHANGE] Setting rate request received: {} -> {} = {}",
        dto.baseCurrency(), dto.targetCurrency(), dto.rate());

    ensureAuthorized();
    log.debug("[FOREIGN_EXCHANGE] Authorization check passed");

    validateCurrency(dto);
    log.debug("[FOREIGN_EXCHANGE] Currency validation passed");

    LocalDateTime now = LocalDateTime.now();
    String updatedBy = currentUserService.getCurrentUser().getUsername();
    log.debug("[FOREIGN_EXCHANGE] Updated by user: {}", updatedBy);

    Optional<ForeignExchange> existingOptional = rateRepository.findByBaseCurrencyAndTargetCurrency(
        dto.baseCurrency(), dto.targetCurrency());

    ForeignExchange rate;
    boolean isUpdate = existingOptional.isPresent();

    if (existingOptional.isPresent()) {
      ForeignExchange existing = existingOptional.get();
      log.debug("[FOREIGN_EXCHANGE] Existing rate found: ID={}, Current rate={}",
          existing.getId(), existing.getRate());

      if (existing.getRate().compareTo(dto.rate()) == 0) {
        log.info("[FOREIGN_EXCHANGE] Rate unchanged for {} -> {}, returning existing rate",
            dto.baseCurrency(), dto.targetCurrency());
        return ForeignExchangeMapper.toResponse(existing);
      }

      log.info("[FOREIGN_EXCHANGE] Updating rate {} -> {}: {} -> {}",
          dto.baseCurrency(), dto.targetCurrency(),
          existing.getRate(), dto.rate());
      existing.setRate(dto.rate());
      rate = existing;
    } else {
      log.info("[FOREIGN_EXCHANGE] Creating new rate: {} -> {} = {}",
          dto.baseCurrency(), dto.targetCurrency(), dto.rate());
      rate = ForeignExchange
          .builder()
          .baseCurrency(dto.baseCurrency())
          .targetCurrency(dto.targetCurrency())
          .rate(dto.rate())
          .build();
    }

    ForeignExchange saved = rateRepository.save(rate);
    log.info("[FOREIGN_EXCHANGE] Rate {} saved successfully: ID={}, {} -> {} = {}",
        isUpdate ? "updated" : "created", saved.getId(),
        saved.getBaseCurrency(), saved.getTargetCurrency(), saved.getRate());

    ForeignExchangeHistory history = ForeignExchangeHistory
        .builder()
        .foreignExchange(saved)
        .baseCurrency(saved.getBaseCurrency())
        .targetCurrency(saved.getTargetCurrency())
        .rate(saved.getRate())
        .changedAt(now)
        .changedBy(updatedBy)
        .changedByFullName(updatedBy)
        .build();

    historyRepository.save(history);
    log.info("[FOREIGN_EXCHANGE] History record created: ID={}, Changed by: {}",
        history.getId(), updatedBy);

    return ForeignExchangeMapper.toResponse(saved);
  }

  public void validateCurrency(ForeignExchangeRequest dto) {
    log.debug("[FOREIGN_EXCHANGE] Validating currency request");

    if (dto == null) {
      log.error("[FOREIGN_EXCHANGE] Validation failed: Request DTO is null");
      throw new IllegalArgumentException("ForeignExchangeRequest cannot be null");
    }

    if (dto.baseCurrency() == dto.targetCurrency()) {
      log.warn("[FOREIGN_EXCHANGE] Validation failed: Base and target currencies are the same: {}",
          dto.baseCurrency());
      throw new IllegalArgumentException("Base currency and target currency cannot be the same");
    }

    if (dto.baseCurrency() != SupportedCurrency.USD) {
      log.warn("[FOREIGN_EXCHANGE] Validation failed: Base currency is not USD: {}",
          dto.baseCurrency());
      throw new IllegalArgumentException("Base currency must be USD");
    }

    if (!SUPPORTED_TARGET_CURRENCIES.contains(dto.targetCurrency())) {
      log.warn("[FOREIGN_EXCHANGE] Validation failed: Target currency {} not in supported list: {}",
          dto.targetCurrency(), SUPPORTED_TARGET_CURRENCIES);
      throw new IllegalArgumentException(
          "Target currency must be one of: " + SUPPORTED_TARGET_CURRENCIES
              .stream()
              .map(Enum::name)
              .collect(Collectors.joining(", ")));
    }

    log.debug("[FOREIGN_EXCHANGE] Currency validation passed");
  }

  public List<ForeignExchangeResponse> listAll() {
    log.debug("[FOREIGN_EXCHANGE] Listing all foreign exchange rates");
    List<ForeignExchange> rates = rateRepository.findAll();
    log.info("[FOREIGN_EXCHANGE] Retrieved {} foreign exchange rates", rates.size());
    return ForeignExchangeMapper.toResponseList(rates);
  }

  public Page<ForeignExchangeHistoryResponse> getHistory(Long foreignExchangeId,
      Pageable pageable) {
    log.debug("[FOREIGN_EXCHANGE] Getting history - ForeignExchangeId: {}, Page: {}, Size: {}",
        foreignExchangeId, pageable.getPageNumber(), pageable.getPageSize());

    ForeignExchange foreignExchange = rateRepository.findById(foreignExchangeId)
        .orElseThrow(() -> {
          log.error("[FOREIGN_EXCHANGE] ForeignExchange not found with ID: {}", foreignExchangeId);
          return new IllegalArgumentException(
              "Foreign Exchange not found with ID: " + foreignExchangeId);
        });

    log.debug("[FOREIGN_EXCHANGE] Found ForeignExchange: {} -> {}",
        foreignExchange.getBaseCurrency(), foreignExchange.getTargetCurrency());

    Page<ForeignExchangeHistory> history = historyRepository.findByForeignExchangeOrderByChangedAtDesc(
        foreignExchange, pageable);
    log.info("[FOREIGN_EXCHANGE] Retrieved {} history records for ForeignExchangeId {} (Total: {})",
        history.getNumberOfElements(), foreignExchangeId, history.getTotalElements());
    return ForeignExchangeMapper.toHistoryResponsePage(history);
  }

  public Map<SupportedCurrency, BigDecimal> getUsdBasedExchangeRates() {
    log.debug("[FOREIGN_EXCHANGE] Getting all USD-based exchange rates");

    Map<SupportedCurrency, BigDecimal> rates = new HashMap<>();

    rates.put(SupportedCurrency.USD, BigDecimal.ONE);
    log.debug("[FOREIGN_EXCHANGE] Added USD base rate: 1.0");

    List<ForeignExchange> usdRates = rateRepository.findByBaseCurrency(SupportedCurrency.USD);
    log.debug("[FOREIGN_EXCHANGE] Found {} USD-based rates in database", usdRates.size());

    for (ForeignExchange rate : usdRates) {
      rates.put(rate.getTargetCurrency(), rate.getRate());
      log.trace("[FOREIGN_EXCHANGE] Added rate: USD -> {} = {}",
          rate.getTargetCurrency(), rate.getRate());
    }

    log.info("[FOREIGN_EXCHANGE] Retrieved {} USD-based exchange rates", rates.size());
    return rates;
  }

  private void ensureAuthorized() {
    currentUserService.ensureHasAnyRole(
        RoleCode.ROLE_SUPER_ADMIN,
        RoleCode.ROLE_CEO,
        RoleCode.ROLE_FINANCE
    );
  }
}

