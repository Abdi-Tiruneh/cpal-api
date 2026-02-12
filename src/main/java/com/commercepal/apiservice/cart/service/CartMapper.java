package com.commercepal.apiservice.cart.service;

import com.commercepal.apiservice.cart.dto.CartItemResponse;
import com.commercepal.apiservice.cart.dto.CartResponse;
import com.commercepal.apiservice.cart.model.Cart;
import com.commercepal.apiservice.cart.model.CartItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Cart entities to response DTOs.
 */
@Slf4j
@Component
public class CartMapper {

  /**
   * Convert Cart entity to CartResponse DTO
   */
  public CartResponse toCartResponse(Cart cart) {
    log.debug("Mapping cart {} to response", cart.getId());

    List<CartItemResponse> items = cart.getItems().stream()
        .map(this::toCartItemResponse)
        .collect(Collectors.toList());

    List<CartItemResponse> priceDropItems = cart.getItems().stream()
        .filter(CartItem::hasPriceDropped)
        .map(this::toCartItemResponse)
        .collect(Collectors.toList());

    List<CartItemResponse> unavailableItems = cart.getItems().stream()
        .filter(item -> !item.getIsAvailable())
        .map(this::toCartItemResponse)
        .collect(Collectors.toList());

    BigDecimal totalSavings = cart.getItems().stream()
        .map(CartItem::getSavingsAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return CartResponse.builder()
        .cartId(cart.getId())
        .totalItems(cart.getTotalItems())
        .items(items)
        .subtotal(cart.getSubtotal())
        .estimatedTotal(cart.getEstimatedTotal())
        .currency(cart.getCurrency())
        .lastActivityAt(cart.getLastActivityAt())
        .priceDropItems(priceDropItems)
        .unavailableItems(unavailableItems)
        .totalSavings(totalSavings)
        .build();
  }

  /**
   * Convert CartItem entity to CartItemResponse DTO
   */
  public CartItemResponse toCartItemResponse(CartItem item) {
    return CartItemResponse.builder()
        .id(item.getId())
        .productId(item.getProductId())
        .productName(item.getProductName())
        .productImageUrl(item.getProductImageUrl())
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice())
        .subtotal(item.getSubtotal())
        .currency(item.getCurrency())
        .provider(item.getProvider() != null ? item.getProvider() : null)
        .stockStatus(item.getStockStatus() != null ? item.getStockStatus().name() : null)
        .isAvailable(item.getIsAvailable())
        .priceWhenAdded(item.getPriceWhenAdded())
        .currentPrice(item.getCurrentPrice())
        .priceDropped(item.getPriceDropped())
        .savingsAmount(item.getSavingsAmount())
        .build();
  }
}
