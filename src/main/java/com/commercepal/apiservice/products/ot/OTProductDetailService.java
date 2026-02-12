package com.commercepal.apiservice.products.ot;

import com.commercepal.apiservice.products.dto.ProductDetailResponse;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.shared.exceptions.service.ProviderServiceException;
import com.commercepal.apiservice.utils.HttpProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Professional service for fetching and transforming product details from OT provider. Implements
 * caching, DRY principles, and efficient error handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OTProductDetailService {

  private static final String SERVICE_NAME = "OT Product Detail Service";
  private static final String METHOD_NAME = "BatchGetItemFullInfo";
  private static final String STATUS_CODE_SUCCESS = "200";
  private static final int RESULT_CODE_SUCCESS = 0;
  private final HttpProcessor httpProcessor;
  private final OTProductDetailTransformService otProductDetailTransformService;
  @Value(value = "${marketplace.provider.url}")
  private String apiUrl;

  /**
   * Fetch complete product details for customer display (includes reviews and recommendations).
   * Results are cached for improved performance.
   *
   * @param itemId      Product item identifier
   * @param userCountry User's country for pricing calculation
   * @return Complete product detail response
   */
  @Cacheable(value = "productDetails", key = "#itemId + '_' +#userCountry.name() + '_' + #targetCurrency.code")
  public ProductDetailResponse getProductDetailForDisplay(String itemId,
      SupportedCountry userCountry, SupportedCurrency targetCurrency) {
    log.debug("Fetching product detail for display: itemId={}, country={}, currency={}", itemId,
        userCountry,
        targetCurrency);
    String blockList = buildBlockListForDisplay(itemId);
    return fetchAndTransformProductDetail(itemId, blockList, userCountry, targetCurrency);
  }

  /**
   * Fetch essential product details for order placement (optimized, no reviews/recommendations).
   * Results are cached for improved performance.
   *
   * @param itemId      Product item identifier
   * @param userCountry User's country for pricing calculation
   * @return Essential product detail response for order processing
   */
  @Cacheable(value = "productDetails", key = "#itemId + '_order_' +#userCountry.name() + '_' + #targetCurrency.code")
  public ProductDetailResponse getProductDetailForOrder(String itemId,
      SupportedCountry userCountry, SupportedCurrency targetCurrency) {
    log.debug("Fetching product detail for order: itemId={}, country={}", itemId, userCountry);
    String blockList = "Promotions";
    return fetchAndTransformProductDetail(itemId, blockList, userCountry, targetCurrency);
  }

  /**
   * Core method to fetch and transform product details. Implements DRY principle by consolidating
   * common logic.
   *
   * @param itemId      Product item identifier
   * @param blockList   Comma-separated list of data blocks to fetch
   * @param userCountry User's country for pricing calculation
   * @return Transformed product detail response
   */
  private ProductDetailResponse fetchAndTransformProductDetail(
      String itemId,
      String blockList,
      SupportedCountry userCountry,
      SupportedCurrency targetCurrency) {

    // Build request
    JSONObject requestBody = buildRequestBody(itemId, blockList);

    // Execute API call
    JSONObject response = executeProviderRequest(requestBody);
    System.err.println(response.toString());

    // Validate response
    validateResponse(response);

    // Parse response body
    JSONObject responseBody = parseResponseBody(response, itemId);

    // Extract result data
    JSONObject resultData = extractResultData(responseBody);

    // Extract individual components
    JSONObject item = resultData.optJSONObject("Item");
    JSONObject providerReviews = resultData.optJSONObject("ProviderReviews");
    JSONObject recommendedItems = resultData.optJSONObject("RecommendedItems");

    // Validate required item data
    if (item == null || item.isEmpty()) {
      throw new ProviderServiceException(SERVICE_NAME, "Item data not found in provider response");
    }

    // Transform and return
    return otProductDetailTransformService.transform(item, providerReviews, recommendedItems,
        userCountry, targetCurrency);
  }

  /**
   * Build block list for customer display based on item type. AliExpress items don't support
   * RecommendedItems block.
   *
   * @param itemId Product item identifier
   * @return Comma-separated block list
   */
  private String buildBlockListForDisplay(String itemId) {
    boolean isAliExpress = itemId.startsWith("az-") || itemId.startsWith("aesg-");
    return isAliExpress
        ? "Promotions,ProviderReviews"
        : "Promotions,RecommendedItems,ProviderReviews";
  }

  /**
   * Build JSON request body for BatchGetItemFullInfo API call.
   *
   * @param itemId    Product item identifier
   * @param blockList Comma-separated list of data blocks to fetch
   * @return JSON request body
   */
  private JSONObject buildRequestBody(String itemId, String blockList) {
    JSONObject methodStringFilter = new JSONObject();
    methodStringFilter.put("itemId", itemId);
    methodStringFilter.put("blockList", blockList);

    JSONObject requestBody = new JSONObject();
    requestBody.put("method", METHOD_NAME);
    requestBody.put("methodStringFilter", methodStringFilter);

    return requestBody;
  }

  /**
   * Execute HTTP request to provider API.
   *
   * @param requestBody JSON request body
   * @return JSON response from provider
   * @throws ProviderServiceException if communication fails
   */
  private JSONObject executeProviderRequest(JSONObject requestBody) {
    try {
      return httpProcessor
          .executeStructuredRequest(apiUrl, "POST", requestBody.toString(), null)
          .join();
    } catch (Exception ex) {
      log.error("Error communicating with {}: {}", SERVICE_NAME, ex.getMessage(), ex);
      throw new ProviderServiceException(SERVICE_NAME, "Failed to connect to provider", ex);
    }
  }

  /**
   * Validate HTTP response status code.
   *
   * @param response JSON response from provider
   * @throws ProviderServiceException if status code is not 200
   */
  private void validateResponse(JSONObject response) {
    String statusCode = response.optString("StatusCode", "0");
    System.err.println(response);

    if (!STATUS_CODE_SUCCESS.equals(statusCode)) {
      log.error("Unexpected status code from {}: {}", SERVICE_NAME, statusCode);
      throw new ProviderServiceException(SERVICE_NAME,
          String.format("Unexpected status code: %s", statusCode));
    }
  }

  /**
   * Parse response body from HTTP response.
   *
   * @param response JSON response from provider
   * @return Parsed response body
   * @throws ProviderServiceException if result code indicates error
   */
  private JSONObject parseResponseBody(JSONObject response, String itemId) {
    JSONObject responseBody = new JSONObject(response.optString("ResponseBody", "{}"));
    int resultCode = responseBody.optInt("resultCode", -1);

    if (resultCode != RESULT_CODE_SUCCESS) {
      String resultDesc = responseBody.optString("resultDesc", "");
      if (resultDesc.toLowerCase().contains("not found")) {
        throw new ProviderServiceException(String.format("Product with id '%s' not found", itemId));
      }

      log.warn("{} responded with error resultCode: {}", SERVICE_NAME, resultCode);
      throw new ProviderServiceException(
          String.format("Provider returned error resultCode: %d", resultCode));
    }

    return responseBody;
  }

  /**
   * Extract result data from response body.
   *
   * @param responseBody Parsed response body
   * @return Result data containing Item, ProviderReviews, and RecommendedItems
   * @throws ProviderServiceException if result data is missing
   */
  private JSONObject extractResultData(JSONObject responseBody) {
    try {
      return responseBody
          .getJSONObject("body")
          .getJSONObject("Result");
    } catch (Exception e) {
      log.error("Failed to extract result data from response: {}", e.getMessage(), e);
      throw new ProviderServiceException(SERVICE_NAME, "Invalid response structure from provider",
          e);
    }
  }
}
