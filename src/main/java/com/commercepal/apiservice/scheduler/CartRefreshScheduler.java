package com.commercepal.apiservice.scheduler;

import com.commercepal.apiservice.cart.model.Cart;
import com.commercepal.apiservice.cart.model.CartItem;
import com.commercepal.apiservice.cart.model.StockStatus;
import com.commercepal.apiservice.cart.repository.CartRepository;
import com.commercepal.apiservice.products.dto.ProductDetailResponse;
import com.commercepal.apiservice.products.ot.OTProductDetailService;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler to refresh cart item details (price, stock, etc.) asynchronously.
 * <p>
 * Ensures eventual consistency for items added via "Fast Add".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartRefreshScheduler {

  private final CartRepository cartRepository;
  private final OTProductDetailService productDetailService;

  /**
   * Refresh active carts every 5 minutes. Looks for carts updated in the last 24 hours but ignoring
   * very recent ones (last 30s) to avoid race conditions with ongoing user sessions.
   */
  @Scheduled(fixedDelay = 300000) // 5 minutes
  @Transactional
  public void refreshActiveCarts() {
    log.info("Starting scheduled cart refresh job...");

    LocalDateTime end = LocalDateTime.now().minusSeconds(30);
    LocalDateTime start = end.minusHours(24);

    List<Cart> activeCarts = cartRepository.findActiveCartsModifiedBetween(start, end);

    if (activeCarts.isEmpty()) {
      log.info("No active carts to refresh.");
      return;
    }

    log.info("Found {} active carts to refresh.", activeCarts.size());

    for (Cart cart : activeCarts) {
      refreshCart(cart);
    }

    log.info("Cart refresh job completed.");
  }

  private void refreshCart(Cart cart) {
    log.debug("Refreshing cart ID: {}", cart.getId());
    boolean cartChanged = false;

    for (CartItem item : cart.getItems()) {
      // optimized: if item was updated very recently (e.g. via validation), skip?
      // For now, refresh all items in the cart.

      // Skip ad-hoc items that don't have a real product ID
      if (item.getProductId() != null && item.getProductId().startsWith("adhoc-")) {
        continue;
      }

      try {
        // Fetch fresh details
        ProductDetailResponse product = productDetailService.getProductDetailForOrder(
            item.getProductId(),
            SupportedCountry.fromCode(cart.getCountry()),
            cart.getCurrency());

        // Update Price
        BigDecimal currentPrice = product.pricing().currentPrice();
        if (currentPrice != null && currentPrice.compareTo(item.getCurrentPrice()) != 0) {
          log.info("Updating price for item {} in cart {}: {} -> {}",
              item.getProductId(), cart.getId(), item.getCurrentPrice(), currentPrice);
          item.setCurrentPrice(currentPrice);

          // Check for price drop
          if (item.getPriceWhenAdded() != null
              && currentPrice.compareTo(item.getPriceWhenAdded()) < 0) {
            item.setPriceDropped(true);
          }
          cartChanged = true;
        }

        // Update Name (if missing or potentially outdated)
        if (product.title() != null && !product.title().equals(item.getProductName())) {
          item.setProductName(product.title());
          cartChanged = true;
        }

        // Update Image if missing
        if (item.getProductImageUrl() == null && product.mainImage() != null) {
          item.setProductImageUrl(product.mainImage().main());
          cartChanged = true;
        }

        // Update Provider if missing
        if (item.getProvider() == null && product.provider() != null) {
          item.setProvider(product.provider());
          cartChanged = true;
        }

        // Update Stock Status
        // Assuming fetching details implies it's available, otherwise service throws?
        // Or check explicit availability flag if DTO has it.
        // Re-setting to IN_STOCK if successful fetch.
        if (item.getStockStatus() != StockStatus.IN_STOCK) {
          item.setStockStatus(StockStatus.IN_STOCK);
          item.setIsAvailable(true);
          cartChanged = true;
        }

      } catch (Exception e) {
        log.warn("Failed to refresh item {} in cart {}: {}", item.getProductId(), cart.getId(),
            e.getMessage());
        // Mark unavailable if persistent error?
        // For now, just log.
        // Could set stock status to UNKNOWN or OUT_OF_STOCK safely.
        if (item.getStockStatus() != StockStatus.UNKNOWN) {
          item.setStockStatus(StockStatus.UNKNOWN);
          cartChanged = true;
        }
      }
    }

    if (cartChanged) {
      calculateAndUpdateTotals(cart);
      cartRepository.save(cart);
      log.debug("Saved updates for cart ID: {}", cart.getId());
    }
  }

  private void calculateAndUpdateTotals(Cart cart) {
    BigDecimal subtotal = cart.getItems().stream()
        .map(CartItem::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int totalItems = cart.getItems().stream()
        .mapToInt(CartItem::getQuantity)
        .sum();

    cart.setSubtotal(subtotal);
    cart.setTotalItems(totalItems);
    cart.setEstimatedTotal(subtotal);
  }
}
