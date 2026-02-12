package com.commercepal.apiservice.products.ot;

import com.commercepal.apiservice.products.dto.ProductCardResponse;
import com.commercepal.apiservice.products.dto.ProductPageRequestDto;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.shared.exceptions.service.ProviderServiceException;
import com.commercepal.apiservice.utils.HttpProcessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTProductListService {

  private static final String SERVICE_NAME = "OT Product List Service";

  private final HttpProcessor httpProcessor;
  private final OTProductTransformServiceOT otProductTransformService;

  @Value(value = "${marketplace.provider.url}")
  private String apiUrl;

  /**
   * Builds search request with optimized parameter handling.
   */
  private static JSONObject buildSearchRequest(ProductPageRequestDto requestDto) {
    JSONObject methodStringFilter = buildStringFilters(requestDto.getPageOrDefault(),
        requestDto.getSizeOrDefault());
    JSONObject methodObjectFilter = requestDto.toJsonObject();

    if (!methodObjectFilter.has("CategoryId")
        && !methodObjectFilter.has("Provider")
        && !methodObjectFilter.has("ItemTitle")
        && !methodObjectFilter.has("BrandId")
        && !methodObjectFilter.has("Configurators")) {
      throw new IllegalArgumentException("At least one of the following fields must be provided: "
          + "CategoryId, VendorName, VendorId, ItemTitle, BrandId, Configurators.");
    }

    return new JSONObject()
        .put("method", "SearchItemsFrame")
        .put("methodStringFilter", methodStringFilter)
        .put("methodObjectFilter", methodObjectFilter);
  }

  /**
   * Builds string filters for pagination.
   */
  private static JSONObject buildStringFilters(int page, int size) {
    return new JSONObject()
        .put("frameSize", String.valueOf(size))
        .put("framePosition", String.valueOf(page));
  }

  public List<ProductCardResponse> getOtProducts(ProductPageRequestDto requestDto,
      SupportedCountry userCountry, SupportedCurrency targetCurrency) {
    JSONObject requestBody = buildSearchRequest(requestDto);

    JSONObject response;
    try {
      response = httpProcessor
          .executeStructuredRequest(apiUrl, "POST", requestBody.toString(), null)
          .join();
    } catch (Exception ex) {
      log.error("Error communicating with {}: {}", SERVICE_NAME, ex.getMessage(), ex);
      throw new ProviderServiceException(SERVICE_NAME, "Failed to connect to provider", ex);
    }

    String statusCode = response.optString("StatusCode", "0");
    if (!"200".equals(statusCode)) {
      log.error("Unexpected status code from {}: {}", SERVICE_NAME, statusCode);
      throw new ProviderServiceException(SERVICE_NAME,
          String.format("Unexpected status code: %s", statusCode));
    }

    JSONObject responseBody = new JSONObject(response.optString("ResponseBody", "{}"));
    int resultCode = responseBody.optInt("resultCode", -1);
    if (resultCode != 0) {
      log.warn("{} responded with error resultCode: {}", SERVICE_NAME, resultCode);
      throw new ProviderServiceException(SERVICE_NAME,
          String.format("Provider returned error resultCode: %d", resultCode));
    }

    List<ProductCardResponse> productList = new ArrayList<>();
    Set<String> seenProductIds = new HashSet<>();

    JSONObject items = responseBody.getJSONObject("body").getJSONObject("Result")
        .getJSONObject("Items");
    items.getJSONArray("Content")
        .forEach(item -> {
          JSONObject itemInfo = (JSONObject) item;

          double originalPrice = itemInfo.optJSONObject("Price").optDouble("OriginalPrice", 0.0);
          if (originalPrice != 0.0) {
            ProductCardResponse detail = otProductTransformService.transform(itemInfo, userCountry,
                targetCurrency);
            String productId = detail.id();

            if (seenProductIds.add(productId)) {
              productList.add(detail);
            }
          }
        });

    return productList;
  }

}
