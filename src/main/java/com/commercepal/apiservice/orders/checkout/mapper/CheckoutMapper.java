package com.commercepal.apiservice.orders.checkout.mapper;

import com.commercepal.apiservice.orders.checkout.dto.CheckoutItem;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutRequest;
import com.commercepal.apiservice.orders.core.model.OrderItem;
import com.commercepal.apiservice.products.dto.ProductDetailResponse;
import com.commercepal.apiservice.products.dto.ProductVariantView;
import com.commercepal.apiservice.shared.enums.Channel;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for checkout-related conversions.
 * <p>
 * Handles mapping between checkout DTOs and entities, including:
 * - CheckoutItem to OrderItem conversions
 * - CheckoutRequest field extraction
 * - Product detail extraction and formatting
 */
@Slf4j
@Component
public class CheckoutMapper {

  // ==================== CheckoutItem Mapping ====================

  /**
   * Maps CheckoutItem DTO to OrderItem entity builder.
   * This is a partial mapping - additional fields are set in the service layer
   * after fetching product details and calculating financials.
   *
   * @param checkoutItem the checkout item DTO
   * @param productDetail the product details fetched from product service
   * @param variant the selected variant (if applicable)
   * @return OrderItem builder with basic fields populated
   */
  public OrderItem.OrderItemBuilder toOrderItemBuilder(
      CheckoutItem checkoutItem,
      ProductDetailResponse productDetail,
      ProductVariantView variant) {
    if (checkoutItem == null || productDetail == null) {
      log.debug("Cannot map checkout item | CheckoutMapper | toOrderItemBuilder | checkoutItem={}, productDetail={}",
          checkoutItem != null, productDetail != null);
      return null;
    }

    log.debug("Mapping checkout item to order item | CheckoutMapper | toOrderItemBuilder | itemId={}, configId={}, quantity={}",
        checkoutItem.itemId(), checkoutItem.configId(), checkoutItem.quantity());

    OrderItem.OrderItemBuilder builder = OrderItem.builder()
        .providerProductId(checkoutItem.itemId())
        .quantity(checkoutItem.quantity());

    // Set config ID if variant is selected
    if (variant != null && variant.configId() != null) {
      builder.configId(variant.configId());
      log.debug("Variant config ID set | CheckoutMapper | toOrderItemBuilder | configId={}", variant.configId());
    } else if (checkoutItem.configId() != null && !checkoutItem.configId().isBlank()) {
      builder.configId(checkoutItem.configId());
      log.debug("Config ID from checkout item set | CheckoutMapper | toOrderItemBuilder | configId={}", checkoutItem.configId());
    }

    return builder;
  }

  // ==================== Product Detail Extraction ====================

  /**
   * Extracts product name from product detail.
   *
   * @param productDetail the product detail response
   * @return product name or null
   */
  public String extractProductName(ProductDetailResponse productDetail) {
    if (productDetail == null) {
      return null;
    }
    return productDetail.title();
  }

  /**
   * Extracts product description from product detail.
   * Formats description list into a single string.
   *
   * @param productDetail the product detail response
   * @param maxLength maximum length for description
   * @return formatted product description
   */
  public String extractProductDescription(ProductDetailResponse productDetail, int maxLength) {
    if (productDetail == null || productDetail.description() == null
        || productDetail.description().isEmpty()) {
      return "";
    }

    String desc = String.join(", ", productDetail.description());
    if (desc.length() > maxLength) {
      return desc.substring(0, maxLength - 3) + "...";
    }
    return desc;
  }

  /**
   * Extracts product image URL from product detail.
   *
   * @param productDetail the product detail response
   * @return image URL or null
   */
  public String extractProductImageUrl(ProductDetailResponse productDetail) {
    if (productDetail == null || productDetail.mainImage() == null) {
      return null;
    }
    return productDetail.mainImage().main();
  }

  /**
   * Formats variant configurators into a readable string.
   *
   * @param variant the product variant view
   * @return formatted configuration string (e.g., "Size: L, Color: Blue")
   */
  public String formatVariantConfigurators(ProductVariantView variant) {
    if (variant == null || variant.configurators() == null
        || variant.configurators().isEmpty()) {
      return "";
    }

    return variant.configurators().stream()
        .map(c -> c.propertyName() + ": " + c.value())
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  // ==================== CheckoutRequest Extraction ====================

  /**
   * Extracts channel/platform from checkout request.
   *
   * @param request the checkout request
   * @return channel/platform
   */
  public Channel extractChannel(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.channel();
  }

  /**
   * Extracts currency from checkout request.
   *
   * @param request the checkout request
   * @return currency
   */
  public SupportedCurrency extractCurrency(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.currency();
  }

  /**
   * Extracts delivery address ID from checkout request.
   *
   * @param request the checkout request
   * @return delivery address ID
   */
  public Long extractDeliveryAddressId(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.deliveryAddressId();
  }

  /**
   * Extracts checkout items from checkout request.
   *
   * @param request the checkout request
   * @return list of checkout items
   */
  public List<CheckoutItem> extractItems(CheckoutRequest request) {
    if (request == null) {
      return List.of();
    }
    return request.items();
  }

  /**
   * Extracts payment provider code from checkout request.
   *
   * @param request the checkout request
   * @return payment provider code
   */
  public String extractPaymentProviderCode(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.paymentProviderCode();
  }

  /**
   * Extracts payment provider variant code from checkout request.
   *
   * @param request the checkout request
   * @return payment provider variant code or null
   */
  public String extractPaymentProviderVariantCode(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.paymentProviderVariantCode();
  }

  /**
   * Extracts payment account from checkout request.
   *
   * @param request the checkout request
   * @return payment account identifier or null
   */
  public String extractPaymentAccount(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.paymentAccount();
  }

  /**
   * Extracts promo code from checkout request.
   *
   * @param request the checkout request
   * @return promo code or null
   */
  public String extractPromoCode(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.promoCode();
  }

  /**
   * Extracts referral code from checkout request.
   *
   * @param request the checkout request
   * @return referral code or null
   */
  public String extractReferralCode(CheckoutRequest request) {
    if (request == null) {
      return null;
    }
    return request.referralCode();
  }

  /**
   * Validates that checkout request has at least one item.
   *
   * @param request the checkout request
   * @return true if request has items
   */
  public boolean hasItems(CheckoutRequest request) {
    if (request == null || request.items() == null) {
      return false;
    }
    return !request.items().isEmpty();
  }

  /**
   * Gets the count of items in checkout request.
   *
   * @param request the checkout request
   * @return item count
   */
  public int getItemCount(CheckoutRequest request) {
    if (request == null || request.items() == null) {
      return 0;
    }
    return request.items().size();
  }
}
