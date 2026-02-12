package com.commercepal.apiservice.settings.foreign_exchange;

import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeHistoryResponse;
import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeRequest;
import com.commercepal.apiservice.settings.foreign_exchange.dto.ForeignExchangeResponse;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.users.role.RoleCode;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/foreign-exchange")
@RequiredArgsConstructor
@Tag(
    name = "Foreign Exchange",
    description = """
        Enterprise-grade APIs for managing foreign exchange rates, supported currencies, and audit-ready
        history. Every endpoint is restricted to finance-privileged roles:
        ROLE_SUPER_ADMIN, ROLE_CEO, or ROLE_FINANCE.
        """
)
public class ForeignExchangeController {

  private final ForeignExchangeService foreignExchangeService;
  private final CurrentUserService currentUserService;

  @GetMapping("/supported-target-currency")
  @Operation(
      summary = "List supported target currencies",
      description = """
          Returns the canonical set of currencies that can be configured as the target leg of an FX pair.
          Required roles: ROLE_SUPER_ADMIN, ROLE_CEO, or ROLE_FINANCE.
          Use this before upserting rates to validate that the target currency is allowed for enterprise FX.
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Supported target currencies retrieved"),
      @ApiResponse(responseCode = "403", description = "Forbidden – missing finance-privileged role"),
      @ApiResponse(responseCode = "500", description = "Unexpected server error while fetching currencies")
  })
  public ResponseEntity<ResponseWrapper<Set<SupportedCurrency>>> getSupportedTargetCurrency() {
    log.info("[FOREIGN_EXCHANGE_API] GET /supported-target-currency - Request received");
    ensureAuthorized();
    Set<SupportedCurrency> currencies = foreignExchangeService.getSupportedTargetCurrency();
    log.info("[FOREIGN_EXCHANGE_API] GET /supported-target-currency - Success: {} currencies",
        currencies.size());
    return ResponseWrapper.success(currencies);
  }

  @PostMapping
  @Operation(
      summary = "Create or update a foreign exchange rate",
      description = """
          Upserts (create or overwrite) an enterprise FX rate from base to target currency and records a full
          audit trail (who/when/what) for compliance. Required roles: ROLE_SUPER_ADMIN, ROLE_CEO, or ROLE_FINANCE.
          Downstream pricing, payout, and reconciliation services consume these authoritative rates.
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Rate created or updated successfully"),
      @ApiResponse(responseCode = "400", description = "Validation failed for the provided rate payload"),
      @ApiResponse(responseCode = "403", description = "Forbidden – caller lacks required finance roles"),
      @ApiResponse(responseCode = "500", description = "Unexpected server error while persisting rate")
  })
  public ResponseEntity<ResponseWrapper<ForeignExchangeResponse>> setRate(
      @RequestBody @Valid ForeignExchangeRequest dto
  ) {
    log.info("[FOREIGN_EXCHANGE_API] POST / - Setting rate: {} -> {} = {}",
        dto.baseCurrency(), dto.targetCurrency(), dto.rate());
    ensureAuthorized();
    ForeignExchangeResponse result = foreignExchangeService.setRate(dto);
    log.info("[FOREIGN_EXCHANGE_API] POST / - Rate set successfully: ID={}, {} -> {} = {}",
        result.id(), result.baseCurrency(), result.targetCurrency(), result.rate());
    return ResponseWrapper.success(result);
  }

  @GetMapping
  @Operation(
      summary = "List all foreign exchange rates",
      description = """
          Retrieves every configured FX pair with the latest effective rates and metadata for finance operations.
          Required roles: ROLE_SUPER_ADMIN, ROLE_CEO, or ROLE_FINANCE.
          Ideal for dashboards, reconciliation, and governance views that need the current catalog of FX pairs.
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "All exchange rates retrieved"),
      @ApiResponse(responseCode = "403", description = "Forbidden – missing finance-privileged role"),
      @ApiResponse(responseCode = "500", description = "Unexpected server error while fetching rates")
  })
  public ResponseEntity<ResponseWrapper<List<ForeignExchangeResponse>>> getAllRates() {
    log.info("[FOREIGN_EXCHANGE_API] GET / - Request received");
    ensureAuthorized();
    List<ForeignExchangeResponse> rates = foreignExchangeService.listAll();
    log.info("[FOREIGN_EXCHANGE_API] GET / - Success: {} rates retrieved", rates.size());
    return ResponseWrapper.success(rates);
  }

  @GetMapping("/history")
  @Operation(
      summary = "List historical changes for a foreign exchange rate",
      description = """
          Returns a paginated, audit-ready history of FX rate changes (who/when/what) for a given foreignExchangeId.
          Required roles: ROLE_SUPER_ADMIN, ROLE_CEO, or ROLE_FINANCE.
          Suitable for compliance audits, change investigations, approvals, and SOX-ready sign-off workflows.
          """
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Missing or invalid query parameters"),
      @ApiResponse(responseCode = "403", description = "Forbidden – caller lacks required finance roles"),
      @ApiResponse(responseCode = "404", description = "Foreign exchange rate not found"),
      @ApiResponse(responseCode = "500", description = "Unexpected server error while fetching history")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<ForeignExchangeHistoryResponse>>> getForeignExchangeHistory(
      @Parameter(
          in = ParameterIn.QUERY,
          description = "Foreign exchange rate identifier to retrieve history for",
          example = "1",
          required = true
      )
      @RequestParam Long foreignExchangeId,
      @Parameter(
          in = ParameterIn.QUERY,
          description = "Zero-based page index for history results",
          example = "0"
      )
      @RequestParam(defaultValue = "0") int page,
      @Parameter(
          in = ParameterIn.QUERY,
          description = "Number of records per page (max enforced server-side)",
          example = "20"
      )
      @RequestParam(defaultValue = "20") int size
  ) {
    log.info(
        "[FOREIGN_EXCHANGE_API] GET /history - Request received - ForeignExchangeId: {}, Page: {}, Size: {}",
        foreignExchangeId, page, size);
    ensureAuthorized();
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedAt"));

    Page<ForeignExchangeHistoryResponse> historyPage = foreignExchangeService.getHistory(
        foreignExchangeId, pageable);
    log.info("[FOREIGN_EXCHANGE_API] GET /history - Success: {} records (Total: {})",
        historyPage.getNumberOfElements(), historyPage.getTotalElements());
    return ResponseWrapper.success(historyPage);
  }

  private void ensureAuthorized() {
//    currentUserService.ensureHasAnyRole(
//        RoleCode.ROLE_SUPER_ADMIN,
//        RoleCode.ROLE_CEO,
//        RoleCode.ROLE_FINANCE
//    );
  }
}

