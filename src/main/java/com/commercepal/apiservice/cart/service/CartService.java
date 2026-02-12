package com.commercepal.apiservice.cart.service;

import com.commercepal.apiservice.cart.dto.AddToCartRequest;
import com.commercepal.apiservice.cart.dto.CartItemRequest;
import com.commercepal.apiservice.cart.dto.CartResponse;
import com.commercepal.apiservice.cart.dto.UpdateCartItemRequest;
import com.commercepal.apiservice.cart.model.Cart;
import com.commercepal.apiservice.cart.model.CartItem;
import com.commercepal.apiservice.cart.model.CartStatus;
import com.commercepal.apiservice.cart.model.StockStatus;
import com.commercepal.apiservice.cart.repository.CartItemRepository;
import com.commercepal.apiservice.cart.repository.CartRepository;
import com.commercepal.apiservice.products.dto.PricingView;
import com.commercepal.apiservice.products.dto.ProductDetailResponse;
import com.commercepal.apiservice.products.dto.ProductVariantView;
import com.commercepal.apiservice.products.ot.OTProductDetailService;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeService;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.users.customer.Customer;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Professional shopping cart service with comprehensive features.
 * <p>
 * Features: - Cart persistence across sessions - Guest and authenticated user support - Price
 * tracking and updates - Stock validation - Item management (add, update, remove) - Automatic total
 * calculations
 *
 * @author CommercePal
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

  private static final String BASE_CONFIG = "__BASE__";
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final OTProductDetailService productDetailService;
  private final CartMapper cartMapper;
  private final ForeignExchangeService foreignExchangeService;

  /**
   * Get or create cart for customer
   */
  @Transactional
  public Cart getOrCreateCart(Customer customer) {
    log.debug("Getting or creating cart for customer: {}", customer.getId());

    Optional<Cart> existingCart = cartRepository.findActiveCartByCustomerId(customer.getId());

    if (existingCart.isPresent()) {
      log.debug("Found existing active cart: {}", existingCart.get().getId());
      return existingCart.get();
    }

    // Create new cart
    SupportedCurrency currency = customer.getPreferredCurrency() != null
        ? customer.getPreferredCurrency()
        : SupportedCurrency.ETB;

    String country = customer.getCountry() != null
        ? customer.getCountry()
        : SupportedCountry.ETHIOPIA.getCode();

    Cart newCart = Cart.builder()
        .customer(customer)
        .status(CartStatus.ACTIVE)
        .currency(currency)
        .country(country)
        .totalItems(0)
        .subtotal(BigDecimal.ZERO)
        .estimatedTotal(BigDecimal.ZERO)
        .abandonedNotificationSent(false)
        .build();

    Cart saved = cartRepository.save(newCart);
    log.info("Created new cart for customer: {}, ID: {}", customer.getId(), saved.getId());

    return saved;
  }

  /**
   * Add item to cart
   */
  @Transactional
  public CartResponse addToCart(Customer customer, AddToCartRequest request) {
    log.info("=== ADD TO CART ===");
    log.info("Customer: {}", customer.getId());

    Cart cart = getOrCreateCart(customer);

    if (request.items() == null || request.items().isEmpty()) {
      throw new BadRequestException("Request must contain at least one item");
    }

    // Set cart country from the first item that has a country
    String cartCountry = request.items().stream()
        .filter(item -> item.country() != null && !item.country().trim().isEmpty())
        .findFirst()
        .map(CartItemRequest::country)
        .orElse(SupportedCountry.ETHIOPIA.getCode());

    cart.setCountry(cartCountry);

    log.info("Processing bulk add to cart request with {} items", request.items().size());
    // Deduplicate items based on ProductID + ConfigID (Keep the first occurrence)
    var uniqueItems = request.items().stream()
        .collect(Collectors.toMap(
            item -> item.productId() + "::" + item.configId(), // Key definition
            item -> item, // Value
            (existing, replacement) -> existing // Merge function: Keep existing
        )).values();

    for (CartItemRequest itemRequest : uniqueItems) {
      processSingleItemAdd(cart, itemRequest);
    }

    // Recalculate totals
    calculateAndUpdateTotals(cart);

    Cart savedCart = cartRepository.save(cart);
    log.info("=== ADD TO CART COMPLETE === Total items: {}", savedCart.getTotalItems());

    return cartMapper.toCartResponse(savedCart);
  }

  @Transactional
  protected void processSingleItemAdd(Cart cart, CartItemRequest request) {

    // ─────────────────────────────────────────────────────────────
    // 1. Validate input
    // ─────────────────────────────────────────────────────────────
    if (request.productId() == null || request.productId().isBlank()) {
      throw new BadRequestException("Product ID is required");
    }

    int quantity = request.quantity() != null && request.quantity() > 0
        ? request.quantity()
        : 1;

    // Normalize configId (CRITICAL)
    String normalizedConfigId = request.configId() != null
        ? request.configId()
        : BASE_CONFIG;

    // ─────────────────────────────────────────────────────────────
    // 2. Resolve context (currency & country)
    // ─────────────────────────────────────────────────────────────
    SupportedCurrency currency = request.currency() != null
        ? SupportedCurrency.fromCode(request.currency())
        : cart.getCurrency();

    SupportedCountry country = request.country() != null
        ? SupportedCountry.fromCode(request.country())
        : SupportedCountry.fromCode(cart.getCountry());

    // ─────────────────────────────────────────────────────────────
    // 3. Fetch authoritative product data
    // ─────────────────────────────────────────────────────────────
    ProductDetailResponse product =
        fetchProductDetails(request.productId(), country, currency);

    PricingView pricing = resolvePricing(product, normalizedConfigId);

    BigDecimal currentPrice = pricing.currentPrice();

    BigDecimal basePriceUsd = pricing.pricingViewProvider() != null
        ? pricing.pricingViewProvider().providerUnitPrice()
        : null;

    BigDecimal exchangeRate = safeExchangeRate(currency);

    // ─────────────────────────────────────────────────────────────
    // 4. Find existing item (DB is the source of truth)
    // ─────────────────────────────────────────────────────────────
    Optional<CartItem> existingOpt =
        cartItemRepository.findByCartIdAndProductIdAndConfigId(
            cart.getId(),
            request.productId(),
            normalizedConfigId
        );

    if (existingOpt.isPresent()) {
      updateExistingItem(
          existingOpt.get(),
          quantity,
          currentPrice,
          basePriceUsd,
          exchangeRate,
          product
      );
      return;
    }

    // ─────────────────────────────────────────────────────────────
    // 5. Create new item (single persistence path)

    CartItem newItem = CartItem.builder()
        .cart(cart)                                   // NOT NULL
        .productId(request.productId())              // NOT NULL
        .productName(product.title())                     // NOT NULL
        .productImageUrl(product.mainImage().main())                    // nullable OK
        .configId(normalizedConfigId)                 // NOT NULL (CRITICAL)
        .quantity(quantity)                       // NOT NULL
        .unitPrice(currentPrice)                         // NOT NULL
        .priceWhenAdded(currentPrice)                    // NOT NULL
        .currentPrice(currentPrice)
        .basePriceInUSD(basePriceUsd)                    // nullable OK
        .exchangeRate(exchangeRate)
        .currency(currency)
        .country(request.country() != null ? request.country()
            : cart.getCountry())             // NOT NULL
        .provider(product.provider())                           // NOT NULL
        .stockStatus(StockStatus.IN_STOCK)
        .isAvailable(true)
        .priceDropped(false)
        .priceDropNotified(false)
        .build();

    // Important: ONLY add to cart, rely on cascade
    cart.addItem(newItem);
  }

  /**
   * Get cart for customer
   */
  @Transactional(readOnly = true)
  public CartResponse getCart(Customer customer) {
    log.debug("Getting cart for customer: {}", customer.getId());

    Cart cart = getOrCreateCart(customer);
    return cartMapper.toCartResponse(cart);
  }

  /**
   * Update cart item
   */
  @Transactional
  public CartResponse updateCartItem(
      Customer customer,
      Long itemId,
      UpdateCartItemRequest request
  ) {
    Cart cart = getOrCreateCart(customer);

    CartItem item = cartItemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    if (!item.getCart().getId().equals(cart.getId())) {
      throw new BadRequestException("Cart item does not belong to customer");
    }

    // Normalize
    String targetConfigId = request.replaceConfigId() != null
        ? request.replaceConfigId()
        : item.getConfigId();

    int targetQuantity = request.quantity() != null
        ? request.quantity()
        : item.getQuantity();

    // 🔥 CRITICAL PART — check collision
    Optional<CartItem> collision =
        cartItemRepository.findByCartIdAndProductIdAndConfigId(
            cart.getId(),
            item.getProductId(),
            targetConfigId
        );

    if (collision.isPresent() && !collision.get().getId().equals(item.getId())) {
      // 👉 MERGE
      CartItem existing = collision.get();

      existing.setQuantity(existing.getQuantity() + targetQuantity);

      // Soft delete old item
      item.softDelete("customer");

      cartItemRepository.save(existing);
      cartItemRepository.save(item);

    } else {
      // 👉 SAFE UPDATE
      item.setQuantity(targetQuantity);
      item.setConfigId(targetConfigId);

      if (request.replaceConfigId() != null) {
        ProductDetailResponse product = fetchProductDetails(
            item.getProductId(),
            SupportedCountry.fromCode(cart.getCountry()),
            cart.getCurrency());

        item.setCurrentPrice(product.pricing().currentPrice());
        item.setUnitPrice(product.pricing().currentPrice());
      }

      cartItemRepository.save(item);
    }

    calculateAndUpdateTotals(cart);
    return cartMapper.toCartResponse(cartRepository.save(cart));
  }

  /**
   * Remove item from cart
   */
  @Transactional
  public CartResponse removeCartItem(Customer customer, Long itemId) {
    log.info("Removing cart item: {}", itemId);

    Cart cart = getOrCreateCart(customer);

    CartItem item = cartItemRepository.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

    if (!item.getCart().getId().equals(cart.getId())) {
      throw new BadRequestException("Cart item does not belong to customer");
    }

    cart.removeItem(item);
    cartItemRepository.delete(item);

    calculateAndUpdateTotals(cart);
    Cart savedCart = cartRepository.save(cart);

    log.info("Cart item removed successfully");
    return cartMapper.toCartResponse(savedCart);
  }

  /**
   * Clear all items from cart
   */
  @Transactional
  public void clearCart(Customer customer) {
    log.info("Clearing cart for customer: {}", customer.getId());

    Cart cart = getOrCreateCart(customer);

    cartItemRepository.deleteByCartId(cart.getId());
    cart.clearItems();
    cart.setTotalItems(0);
    cart.setSubtotal(BigDecimal.ZERO);
    cart.setEstimatedTotal(BigDecimal.ZERO);

    cartRepository.save(cart);
    log.info("Cart cleared successfully");
  }

  /**
   * Validate cart and get response
   */
  @Transactional
  public CartResponse validateAndGetCart(Customer customer) {
    log.info("Validating cart for customer: {}", customer.getId());

    Cart cart = getOrCreateCart(customer);

    // Validate all items (check stock, prices, availability)
    for (CartItem item : cart.getItems()) {
      try {
        ProductDetailResponse product = fetchProductDetails(
            item.getProductId(),
            SupportedCountry.fromCode(cart.getCountry()),
            cart.getCurrency());

        // Update current price
        item.setCurrentPrice(product.pricing().currentPrice());

        // Check if price dropped
        if (item.hasPriceDropped()) {
          item.setPriceDropped(true);
          log.info("Price drop detected for item: {}, Savings: {}",
              item.getProductId(), item.getSavingsAmount());
        }

        // Update availability
        item.setIsAvailable(true);
        item.setStockStatus(StockStatus.IN_STOCK);

      } catch (Exception e) {
        log.warn("Item validation failed for: {}, Error: {}",
            item.getProductId(), e.getMessage());
        item.setIsAvailable(false);
        item.setStockStatus(StockStatus.UNKNOWN);
      }

      cartItemRepository.save(item);
    }

    calculateAndUpdateTotals(cart);
    Cart savedCart = cartRepository.save(cart);

    log.info("Cart validation complete");
    return cartMapper.toCartResponse(savedCart);
  }

  // ==================== Private Helper Methods ====================

  /**
   * Fetch product details with error handling
   */
  private ProductDetailResponse fetchProductDetails(String productId,
      SupportedCountry country,
      SupportedCurrency currency) {
    log.debug("Fetching product details - ID: {}, Country: {}, Currency: {}",
        productId, country, currency);

    try {
      return productDetailService.getProductDetailForOrder(productId, country, currency);
    } catch (Exception e) {
      log.error("Failed to fetch product details for: {}", productId, e);
      throw new BadRequestException("Product not found or unavailable: " + productId);
    }
  }

  /**
   * Calculate and update cart totals
   */
  private void calculateAndUpdateTotals(Cart cart) {
    log.debug("Calculating cart totals");

    BigDecimal subtotal = cart.getItems().stream()
        .map(CartItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int totalItems = cart.getItems().stream()
        .mapToInt(CartItem::getQuantity)
        .sum();

    cart.setSubtotal(subtotal);
    cart.setTotalItems(totalItems);
    cart.setEstimatedTotal(subtotal); // Can add delivery fees later

    log.debug("Totals calculated - Items: {}, Subtotal: {}", totalItems, subtotal);
  }

  private BigDecimal safeExchangeRate(SupportedCurrency currency) {
    try {
      return foreignExchangeService.getUsdToTargetRate(currency);
    } catch (Exception e) {
      log.warn("FX lookup failed for {}", currency);
      return null;
    }
  }

  private PricingView resolvePricing(ProductDetailResponse product, String configId) {

    if (product.variants() == null || BASE_CONFIG.equals(configId)) {
      return product.pricing();
    }

    return product.variants().stream()
        .filter(v -> configId.equals(v.configId()))
        .map(ProductVariantView::pricing)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(product.pricing());
  }

  private void updateExistingItem(
      CartItem item,
      int quantity,
      BigDecimal currentPrice,
      BigDecimal basePriceUsd,
      BigDecimal exchangeRate,
      ProductDetailResponse product
  ) {
    item.setQuantity(quantity);
    item.setCurrentPrice(currentPrice);
    item.setProductName(product.title());

    if (product.mainImage() != null) {
      item.setProductImageUrl(product.mainImage().thumbnail());
    }

    if (basePriceUsd != null) {
      item.setBasePriceInUSD(basePriceUsd);
    }

    if (exchangeRate != null) {
      item.setExchangeRate(exchangeRate);
    }
  }
}
