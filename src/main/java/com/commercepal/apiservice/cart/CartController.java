package com.commercepal.apiservice.cart;

import com.commercepal.apiservice.cart.dto.AddToCartRequest;
import com.commercepal.apiservice.cart.dto.CartResponse;
import com.commercepal.apiservice.cart.dto.UpdateCartItemRequest;
import com.commercepal.apiservice.cart.service.CartService;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.CurrentUserService;
import com.commercepal.apiservice.utils.response.PagedResponse;
import com.commercepal.apiservice.utils.response.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Professional shopping cart controller with comprehensive REST API.
 * <p>
 * Features: - Add, update, remove cart items - Get cart details - Clear cart - Validate cart items
 *
 * @author CommercePal
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Professional shopping cart management with persistence, price tracking, and stock validation")
public class CartController {

  private final CartService cartService;
  private final CurrentUserService currentUserService;

  @PostMapping("/items")
  @Operation(summary = "Add Item to Cart", description = """
      Add a product to the shopping cart.
      
      **Features:**
      - If item already exists with same variant, quantities are merged
      - Product details are fetched and cached
      - Prices are tracked for price drop notifications
      - Stock status is validated
      
      **Response:**
      Returns complete cart with all items and totals.
      """, security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Item added successfully", content = @Content(schema = @Schema(implementation = CartResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request or product not found"),
      @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<ResponseWrapper<CartResponse>> addToCart(
      @Valid @RequestBody AddToCartRequest request) {
    log.info("=== ADD TO CART API ===");

    if (request.items() != null && !request.items().isEmpty()) {
      log.info("Processing bulk request with {} items", request.items().size());
    } else {
      log.warn("Received empty cart request");
    }

    Customer customer = currentUserService.getCurrentCustomer();
    CartResponse cart = cartService.addToCart(customer, request);

    log.info("Items added - Total items in cart: {}", cart.totalItems());
    return ResponseWrapper.success("Item added to cart successfully", cart);
  }

  @GetMapping
  @Operation(summary = "Get Cart", description = """
      Retrieve current shopping cart with all items.
      
      **Response includes:**
      - All cart items with details
      - Subtotal and estimated total
      - Items with price drops
      - Unavailable items
      - Total savings
      """, security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cart retrieved successfully", content = @Content(schema = @Schema(implementation = CartResponse.class))),
      @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<ResponseWrapper<CartResponse>> getCart() {
    log.debug("Getting cart");

    Customer customer = currentUserService.getCurrentCustomer();
    CartResponse cart = cartService.getCart(customer);

    return ResponseWrapper.success("Cart retrieved successfully", cart);
  }

  @PutMapping("/items/{itemId}")
  @Operation(summary = "Update Cart Item", description = """
      Update quantity or variant of a cart item.
      
      **Options:**
      - Update quantity only
      - Update variant only
      - Update both
      
      Provide null for fields you don't want to update.
      """, security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cart item updated successfully", content = @Content(schema = @Schema(implementation = CartResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid request"),
      @ApiResponse(responseCode = "404", description = "Cart item not found")
  })
  public ResponseEntity<ResponseWrapper<CartResponse>> updateCartItem(
      @PathVariable Long itemId,
      @Valid @RequestBody UpdateCartItemRequest request) {
    log.info("Updating cart item {} - Quantity: {}, New Config: {}",
        itemId, request.quantity(), request.replaceConfigId());

    Customer customer = currentUserService.getCurrentCustomer();
    CartResponse cart = cartService.updateCartItem(customer, itemId, request);

    return ResponseWrapper.success("Cart item updated successfully", cart);
  }

  @DeleteMapping("/items/{itemId}")
  @Operation(summary = "Remove Cart Item", description = "Remove an item from the cart", security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Item removed successfully", content = @Content(schema = @Schema(implementation = CartResponse.class))),
      @ApiResponse(responseCode = "404", description = "Cart item not found")
  })
  public ResponseEntity<ResponseWrapper<CartResponse>> removeCartItem(@PathVariable Long itemId) {
    log.info("Removing cart item: {}", itemId);

    Customer customer = currentUserService.getCurrentCustomer();
    CartResponse cart = cartService.removeCartItem(customer, itemId);

    return ResponseWrapper.success("Item removed from cart", cart);
  }

  @DeleteMapping
  @Operation(summary = "Clear Cart", description = "Remove all items from the cart", security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
  })
  public ResponseEntity<ResponseWrapper<PagedResponse<Object>>> clearCart() {
    log.info("Clearing cart");

    Customer customer = currentUserService.getCurrentCustomer();
    cartService.clearCart(customer);

    return ResponseWrapper.success("Cart cleared successfully");
  }

  @PostMapping("/validate")
  @Operation(summary = "Validate Cart", description = """
      Validate all cart items for stock availability and pricing.
      
      **Validation includes:**
      - Check product availability
      - Update current prices
      - Detect price drops
      - Check stock status
      - Mark unavailable items
      """, security = @SecurityRequirement(name = "Bearer Authentication"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Cart validated", content = @Content(schema = @Schema(implementation = CartResponse.class)))
  })
  public ResponseEntity<ResponseWrapper<CartResponse>> validateCart() {
    log.info("Validating cart");

    Customer customer = currentUserService.getCurrentCustomer();
    CartResponse cart = cartService.validateAndGetCart(customer);

    return ResponseWrapper.success("Cart validated successfully", cart);
  }
}
