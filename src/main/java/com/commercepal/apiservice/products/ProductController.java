package com.commercepal.apiservice.products;

import com.commercepal.apiservice.products.dto.ProductCardResponse;
import com.commercepal.apiservice.products.dto.ProductDetailResponse;
import com.commercepal.apiservice.products.dto.ProductPageRequestDto;
import com.commercepal.apiservice.products.ot.OTProductDetailService;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.utils.response.ProductPagedResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for product search operations.
 * <p>
 * Provides endpoints for searching products across multiple providers.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Search products across providers")
public class ProductController {

  private final ProviderProductService providerProductService;
  private final OTProductDetailService otProductDetailService;

  /**
   * Search for products across multiple providers.
   *
   * @param page  the page number (0-indexed, default: 0)
   * @param size  the number of results per page (default: 36)
   * @param query the search query string
   * @return the product search results
   */
  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Search products", description =
      "Returns provider-aggregated products for the given query with paging. "
          + "Include optional X-Country header (e.g., ET, KE, AE); defaults to INTERNATIONAL. "
          + "Include optional X-Currency header (e.g., USD, ETB, AED); defaults to country's currency.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Products retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")})
  public ResponseEntity<ResponseWrapper<ProductPagedResponse<ProductCardResponse>>> searchProducts(
      @ParameterObject ProductPageRequestDto request,
      @Parameter(in = ParameterIn.HEADER, description = "Country code (e.g., ET, KE, AE)", example = "ET") @RequestHeader(value = "X-Country", required = false) String countryCode,
      @Parameter(in = ParameterIn.HEADER, description = "Currency code (e.g., USD, ETB)", example = "ETB") @RequestHeader(value = "X-Currency", required = false) String currency) {

    SupportedCountry supportedCountry = resolveCountry(countryCode);
    SupportedCurrency supportedCurrency = resolveCurrency(currency, supportedCountry);

    log.info("[PRODUCT-API] GET /search - request: {}, country: {}, currency: {}", request,
        supportedCountry,
        supportedCurrency);

    List<ProductCardResponse> response = providerProductService.getProductsFromProvider(request,
        supportedCountry,
        supportedCurrency);

    log.info("[PRODUCT-API] GET /search - completed successfully");

    return ResponseWrapper.successProducts(request.getPageOrDefault(), request.getSizeOrDefault(),
        response);
  }

  /**
   * Get detailed information for a specific product by item ID.
   *
   * @param itemId      the product item ID
   * @param countryCode optional country code header (e.g., ET, KE, AE)
   * @param currency    optional currency code header (e.g., USD, ETB)
   * @return the product details
   */
  @GetMapping(value = "/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get product details", description =
      "Returns detailed information for a specific product by item ID. "
          + "Include optional X-Country header (e.g., ET, KE, AE); defaults to INTERNATIONAL. "
          + "Include optional X-Currency header (e.g., USD, ETB); defaults to country's currency.")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Product details retrieved"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")})
  public ResponseEntity<ResponseWrapper<ProductDetailResponse>> getProductDetails(
      @Parameter(in = ParameterIn.PATH, description = "Product item ID", example = "sh-15782573", required = true) @PathVariable String itemId,
      @Parameter(in = ParameterIn.HEADER, description = "Country code (e.g., ET, KE, AE)", example = "ET") @RequestHeader(value = "X-Country", required = false) String countryCode,
      @Parameter(in = ParameterIn.HEADER, description = "Currency code (e.g., USD, ETB)", example = "ETB") @RequestHeader(value = "X-Currency", required = false) String currency) {

    log.info("[PRODUCT-API] GET /{itemId} - itemId: {}, countryCode: {}, currency: {}", itemId,
        countryCode, currency);

    SupportedCountry supportedCountry = resolveCountry(countryCode);
    SupportedCurrency supportedCurrency = resolveCurrency(currency, supportedCountry);

    ProductDetailResponse response = otProductDetailService.getProductDetailForDisplay(itemId,
        supportedCountry,
        supportedCurrency);

    log.info("[PRODUCT-API] GET /{itemId} - completed successfully for itemId: {}", itemId);

    return ResponseWrapper.success(response);
  }

  private SupportedCountry resolveCountry(String countryCode) {
    if (countryCode == null || countryCode.isBlank()) {
      return SupportedCountry.INTERNATIONAL;
    }

    try {
      return SupportedCountry.fromCode(countryCode.trim());
    } catch (IllegalArgumentException ex) {
      log.warn("[PRODUCT-API] Invalid country header '{}', defaulting to INTERNATIONAL",
          countryCode);
      return SupportedCountry.INTERNATIONAL;
    }
  }

  private SupportedCurrency resolveCurrency(String currencyCode, SupportedCountry country) {
    if (currencyCode == null || currencyCode.isBlank()) {
      return country.getDefaultCurrency();
    }

    try {
      return SupportedCurrency.fromCode(currencyCode.trim());
    } catch (IllegalArgumentException ex) {
      log.warn("[PRODUCT-API] Invalid currency param '{}', defaulting to country default",
          currencyCode);
      return country.getDefaultCurrency();
    }
  }
}
