package com.commercepal.apiservice.settings.foreign_exchange;

import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeHistoryResponse;
import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

/**
 * Mapper utility for converting ForeignExchange and ForeignExchangeHistory entities to DTOs.
 * <p>
 * This utility class provides static methods to map entities to response DTOs using the builder
 * pattern for flexible object construction.
 */
public final class ForeignExchangeMapper {

  private ForeignExchangeMapper() {
    // Utility class - prevent instantiation
  }

  /**
   * Maps a ForeignExchange entity to ForeignExchangeResponse DTO.
   *
   * @param foreignExchange the foreign exchange entity
   * @return the foreign exchange response DTO, or null if entity is null
   */
  public static ForeignExchangeResponse toResponse(ForeignExchange foreignExchange) {
    if (foreignExchange == null) {
      return null;
    }

    return ForeignExchangeResponse.builder()
        .id(foreignExchange.getId())
        .baseCurrency(foreignExchange.getBaseCurrency())
        .targetCurrency(foreignExchange.getTargetCurrency())
        .rate(foreignExchange.getRate())
        .createdAt(foreignExchange.getCreatedAt())
        .updatedAt(foreignExchange.getUpdatedAt())
        .build();
  }

  /**
   * Maps a list of ForeignExchange entities to a list of ForeignExchangeResponse DTOs.
   *
   * @param foreignExchanges the list of foreign exchange entities
   * @return the list of foreign exchange response DTOs
   */
  public static List<ForeignExchangeResponse> toResponseList(
      List<ForeignExchange> foreignExchanges) {
    if (foreignExchanges == null) {
      return List.of();
    }

    return foreignExchanges.stream()
        .map(ForeignExchangeMapper::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Maps a ForeignExchangeHistory entity to ForeignExchangeHistoryResponse DTO.
   *
   * @param history the foreign exchange history entity
   * @return the foreign exchange history response DTO, or null if entity is null
   */
  public static ForeignExchangeHistoryResponse toHistoryResponse(ForeignExchangeHistory history) {
    if (history == null) {
      return null;
    }

    return ForeignExchangeHistoryResponse.builder()
        .id(history.getId())
        .foreignExchangeId(
            history.getForeignExchange() != null ? history.getForeignExchange().getId() : null)
        .baseCurrency(history.getBaseCurrency())
        .targetCurrency(history.getTargetCurrency())
        .rate(history.getRate())
        .changedAt(history.getChangedAt())
        .changedBy(history.getChangedBy())
        .changedByFullName(history.getChangedByFullName())
        .build();
  }

  /**
   * Maps a Page of ForeignExchangeHistory entities to a Page of ForeignExchangeHistoryResponse
   * DTOs.
   *
   * @param historyPage the page of foreign exchange history entities
   * @return the page of foreign exchange history response DTOs
   */
  public static Page<ForeignExchangeHistoryResponse> toHistoryResponsePage(
      Page<ForeignExchangeHistory> historyPage) {
    if (historyPage == null) {
      return Page.empty();
    }

    return historyPage.map(ForeignExchangeMapper::toHistoryResponse);
  }
}

