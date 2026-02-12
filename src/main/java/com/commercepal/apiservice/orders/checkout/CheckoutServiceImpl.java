package com.commercepal.apiservice.orders.checkout;

import com.commercepal.apiservice.cart.repository.CartRepository;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutItem;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutRequest;
import com.commercepal.apiservice.orders.checkout.dto.CheckoutResult;
import com.commercepal.apiservice.orders.checkout.mapper.CheckoutMapper;
import com.commercepal.apiservice.orders.core.model.Order;
import com.commercepal.apiservice.orders.core.model.OrderItem;
import com.commercepal.apiservice.orders.core.repository.OrderRepository;
import com.commercepal.apiservice.orders.enums.OrderPriority;
import com.commercepal.apiservice.orders.enums.OrderStage;
import com.commercepal.apiservice.orders.enums.PaymentStatus;
import com.commercepal.apiservice.payments.oderPayment.OrderPaymentService;
import com.commercepal.apiservice.payments.oderPayment.dto.PaymentInitiationResponse;
import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethodItem;
import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethodItemVariant;
import com.commercepal.apiservice.payments.paymentMethod.repository.PaymentMethodItemRepository;
import com.commercepal.apiservice.payments.paymentMethod.repository.PaymentMethodItemVariantRepository;
import com.commercepal.apiservice.products.dto.ProductDetailResponse;
import com.commercepal.apiservice.products.dto.ProductVariantView;
import com.commercepal.apiservice.products.ot.OTProductDetailService;
import com.commercepal.apiservice.settings.foreign_exchange.ForeignExchangeService;
import com.commercepal.apiservice.shared.enums.Provider;
import com.commercepal.apiservice.shared.enums.SupportedCountry;
import com.commercepal.apiservice.shared.enums.SupportedCurrency;
import com.commercepal.apiservice.shared.exceptions.business.BadRequestException;
import com.commercepal.apiservice.shared.exceptions.business.PaymentMethodNotSupportedException;
import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
import com.commercepal.apiservice.users.customer.address.AddressRepository;
import com.commercepal.apiservice.users.customer.address.CustomerAddress;
import com.commercepal.apiservice.users.customer.Customer;
import com.commercepal.apiservice.utils.BigDecimalUtils;
import com.commercepal.apiservice.utils.OrderUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Professional checkout service implementation handling order creation and validation.
 * <p>
 * This service orchestrates the complete checkout process including:
 * - Customer and address validation
 * - Product details fetching and validation
 * - Stock and quantity validation
 * - Pricing calculations
 * - Order and order item creation
 * - Financial calculations
 *
 * @author CommercePal
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

  private static final int DEFAULT_MIN_ORDER_QUANTITY = 1;
  private static final int DEFAULT_QUANTITY_STEP = 1;
  private static final int PRODUCT_DESCRIPTION_MAX_LENGTH = 500;

  private final OTProductDetailService otProductDetailService;
  private final OrderRepository orderRepository;
  private final AddressRepository addressRepository;
  private final ForeignExchangeService foreignExchangeService;
  private final CartRepository cartRepository;
  private final PaymentMethodItemRepository paymentMethodItemRepository;
  private final PaymentMethodItemVariantRepository paymentMethodItemVariantRepository;
  private final OrderPaymentService orderPaymentService;
  private final CheckoutMapper checkoutMapper;

  /**
   * Process checkout request and create a new order.
   */
  @Override
  @Transactional
  public CheckoutResult checkout(CheckoutRequest request, Customer customer) {
    log.info("Initiating checkout | customerId={}, itemCount={}, currency={}",
        customer.getId(), request.items().size(), request.currency());

    // Step 1: Validate and fetch delivery address
    CustomerAddress address = validateAndFetchAddress(request.deliveryAddressId(), customer);

    // Step 2: Determine country and currency
    SupportedCountry country = resolveCountryFromAddress(address);
    SupportedCurrency currency = request.currency();

    // Step 3: Initialize order
    Order order = initializeOrder(customer, address, request, currency);

    // Step 4: Process all checkout items
    processCheckoutItems(request.items(), order, country, currency);

    // Step 5: Calculate and set order totals
    OrderUtil.calculateOrderTotals(order);

    // Step 6: Validate payment method
    validatePaymentMethod(request, currency);

    // Step 7: Save order to database
    Order savedOrder = saveOrder(order);

    // Step 8: Initialize payment for the order
    PaymentInitiationResponse payment = orderPaymentService.initializePaymentForOrder(savedOrder, customer, request);

    // Step 9: Link and convert cart (if exists)
    linkAndConvertCart(customer, savedOrder);

    log.info("Checkout completed | orderNumber={}, totalAmount={}, currency={}",
        savedOrder.getOrderNumber(), savedOrder.getTotalAmount(),
        savedOrder.getOrderCurrency());

    return new CheckoutResult(savedOrder, payment);
  }

  //  Private Helper Methods

  /**
   * Validates delivery address exists and belongs to the customer.
   */
  private CustomerAddress validateAndFetchAddress(Long addressId, Customer customer) {
    CustomerAddress address = addressRepository.findById(addressId)
        .orElseThrow(() -> {
          log.error("Delivery address not found | addressId={}", addressId);
          return new ResourceNotFoundException(
              "Delivery address not found with ID: " + addressId);
        });

    if (!address.getCustomer().getId().equals(customer.getId())) {
      log.error("Address ownership mismatch | addressId={}, customerId={}",
          addressId, customer.getId());
      throw new BadRequestException("Address does not belong to the customer");
    }

    return address;
  }

  /**
   * Resolves supported country from customer address.
   */
  private SupportedCountry resolveCountryFromAddress(CustomerAddress address) {
    try {
      return SupportedCountry.fromCode(address.getCountry());
    } catch (IllegalArgumentException e) {
      log.error("Unsupported country in delivery address | country={}", address.getCountry());
      throw new BadRequestException("Unsupported country: " + address.getCountry());
    }
  }

  /**
   * Initializes a new order with basic information.
   */
  private Order initializeOrder(Customer customer, CustomerAddress address,
      CheckoutRequest request, SupportedCurrency currency) {
    String orderNumber = OrderUtil.generateOrderNumber(orderRepository::existsByOrderNumber);
    LocalDateTime now = LocalDateTime.now();

    return Order.builder()
        .orderNumber(orderNumber)
        .customer(customer)
        .deliveryAddress(address)
        .platform(request.channel())
        .priority(OrderPriority.NORMAL)
        .orderCurrency(currency)
        .paymentStatus(PaymentStatus.PENDING)
        .currentStage(OrderStage.PENDING)
        .orderedAt(now)
        .build();
  }

  /**
   * Processes all checkout items and adds them to the order.
   */
  private void processCheckoutItems(List<CheckoutItem> items, Order order,
      SupportedCountry country, SupportedCurrency currency) {
    for (int i = 0; i < items.size(); i++) {
      CheckoutItem itemRequest = items.get(i);
      OrderItem orderItem = processOrderItem(itemRequest, country, currency, i + 1,
          order.getOrderNumber());
      order.addOrderItem(orderItem);
    }
  }

  /**
   * Processes a single checkout item and creates an OrderItem.
   */
  private OrderItem processOrderItem(CheckoutItem itemRequest, SupportedCountry country,
      SupportedCurrency targetCurrency, int itemNumber,
      String orderNumber) {
    // Fetch product details
    ProductDetailResponse productDetail = fetchProductDetails(itemRequest.itemId(), country,
        targetCurrency);

    // Get exchange rate
    BigDecimal exchangeRate = fetchExchangeRate(targetCurrency);

    // Determine variant and pricing
    VariantPricingInfo pricingInfo = resolveVariantAndPricing(productDetail, itemRequest);

    // Validate quantity constraints
    validateQuantityConstraints(itemRequest, productDetail, pricingInfo);

    // Validate stock availability
    validateStockAvailability(itemRequest, pricingInfo, productDetail);

    // Calculate financial details
    OrderItemFinancials financials = calculateItemFinancials(itemRequest, productDetail,
        pricingInfo);

    // Create order item

    return buildOrderItem(itemRequest, productDetail, pricingInfo,
        financials, targetCurrency, exchangeRate, itemNumber, orderNumber);
  }

  /**
   * Fetches product details from the product service.
   */
  private ProductDetailResponse fetchProductDetails(String itemId, SupportedCountry country,
      SupportedCurrency currency) {
    return otProductDetailService.getProductDetailForOrder(itemId, country, currency);
  }

  /**
   * Fetches current exchange rate for the currency.
   */
  private BigDecimal fetchExchangeRate(SupportedCurrency currency) {
    return foreignExchangeService.getUsdToTargetRate(currency);
  }

  /**
   * Resolves variant selection and pricing information.
   */
  private VariantPricingInfo resolveVariantAndPricing(ProductDetailResponse product,
      CheckoutItem itemRequest) {
    boolean hasVariants = product.variants() != null && !product.variants().isEmpty();

    if (hasVariants) {
      return resolveVariantProduct(product, itemRequest);
    } else {
      return resolveSimpleProduct(product);
    }
  }

  /**
   * Resolves variant product with config selection.
   */
  private VariantPricingInfo resolveVariantProduct(ProductDetailResponse product,
      CheckoutItem itemRequest) {
    if (itemRequest.configId() == null || itemRequest.configId().isBlank()) {
      log.error("Missing variant selection | productTitle={}, itemId={}",
          product.title(), itemRequest.itemId());
      throw new BadRequestException(
          String.format("Product '%s' requires a variant selection (configId)",
              product.title()));
    }

    ProductVariantView selectedVariant = product.variants().stream()
        .filter(v -> v.configId().equals(itemRequest.configId()))
        .findFirst()
        .orElseThrow(() -> {
          log.error("Invalid variant config | productTitle={}, configId={}",
              product.title(), itemRequest.configId());
          return new BadRequestException(
              String.format("Invalid variant '%s' for product '%s'",
                  itemRequest.configId(), product.title()));
        });

    if (selectedVariant.pricing() == null) {
      log.warn("Variant pricing is null, falling back to base pricing | productTitle={}, configId={}",
          product.title(), itemRequest.configId());
      return new VariantPricingInfo(
          selectedVariant,
          product.pricing().currentPrice(),
          selectedVariant.quantity());
    }

    return new VariantPricingInfo(
        selectedVariant,
        selectedVariant.pricing().currentPrice(),
        selectedVariant.quantity());
  }

  /**
   * Resolves simple product without variants.
   */
  private VariantPricingInfo resolveSimpleProduct(ProductDetailResponse product) {
    return new VariantPricingInfo(
        null,
        product.pricing().currentPrice(),
        product.stockLevel());
  }

  /**
   * Validates quantity against product constraints.
   */
  private void validateQuantityConstraints(CheckoutItem itemRequest,
      ProductDetailResponse product,
      VariantPricingInfo pricingInfo) {
    int requestedQuantity = itemRequest.quantity();
    int minOrder = product.minOrderQuantity() != null
        ? product.minOrderQuantity()
        : DEFAULT_MIN_ORDER_QUANTITY;
    int step = product.quantityStep() != null && product.quantityStep() > 0
        ? product.quantityStep()
        : DEFAULT_QUANTITY_STEP;

    // Validate minimum quantity
    if (requestedQuantity < minOrder) {
      log.error("Quantity below minimum | requested={}, minRequired={}, productTitle={}",
          requestedQuantity, minOrder, product.title());
      throw new BadRequestException(
          String.format("Quantity %d is below minimum order quantity %d for '%s'",
              requestedQuantity, minOrder, product.title()));
    }

    // Validate quantity step
    if ((requestedQuantity - minOrder) % step != 0) {
      log.error("Invalid quantity step | requested={}, min={}, step={}, productTitle={}",
          requestedQuantity, minOrder, step, product.title());
      throw new BadRequestException(
          String.format(
              "Quantity %d is invalid for '%s'. Must increase by steps of %d starting from %d",
              requestedQuantity, product.title(), step, minOrder));
    }
  }

  /**
   * Validates stock availability for the requested quantity.
   */
  private void validateStockAvailability(CheckoutItem itemRequest,
      VariantPricingInfo pricingInfo,
      ProductDetailResponse product) {
    Integer stockLevel = pricingInfo.stockLevel();

    if (stockLevel == null) {
      log.warn("Stock level not available | productTitle={}", product.title());
      return;
    }

    if (itemRequest.quantity() > stockLevel) {
      log.error("Insufficient stock | productTitle={}, requested={}, available={}",
          product.title(), itemRequest.quantity(), stockLevel);
      throw new BadRequestException(
          String.format("Insufficient stock for '%s'. Available: %d, Requested: %d",
              product.title(), stockLevel, itemRequest.quantity()));
    }
  }

  /**
   * Calculates financial details for an order item.
   */
  private OrderItemFinancials calculateItemFinancials(CheckoutItem itemRequest,
      ProductDetailResponse product,
      VariantPricingInfo pricingInfo) {
    int quantity = itemRequest.quantity();
    BigDecimal unitPrice = pricingInfo.unitPrice();

    BigDecimal discountAmount = BigDecimalUtils.multiplyAndRound(
        product.pricing().discountAmount(), quantity);
    BigDecimal totalAmount = BigDecimalUtils.multiplyAndRound(unitPrice, quantity);
    BigDecimal subtotal = totalAmount.add(discountAmount);

    return new OrderItemFinancials(subtotal, discountAmount, totalAmount);
  }

  /**
   * Builds the final OrderItem entity.
   */
  private OrderItem buildOrderItem(CheckoutItem itemRequest,
      ProductDetailResponse product,
      VariantPricingInfo pricingInfo,
      OrderItemFinancials financials,
      SupportedCurrency targetCurrency,
      BigDecimal exchangeRate,
      int itemNumber,
      String orderNumber) {
    // Generate sequential sub-order number: orderNumber_01, orderNumber_02, etc.
    String subOrderNumber = OrderUtil.generateSubOrderNumber(orderNumber, itemNumber);
    String productDescription = checkoutMapper.extractProductDescription(product,
        PRODUCT_DESCRIPTION_MAX_LENGTH);
    String imageUrl = checkoutMapper.extractProductImageUrl(product);

    OrderItem orderItem = OrderItem.builder()
        .productName(product.title())
        .productDescription(productDescription)
        .productImageUrl(imageUrl)
        .providerProductId(product.id())
        .subOrderNumber(subOrderNumber)
        .provider(Provider.fromCode(product.provider()))
        .unitPrice(pricingInfo.unitPrice())
        .quantity(itemRequest.quantity())
        .orderCurrency(targetCurrency)
        .taxAmount(BigDecimal.ZERO)
        .deliveryFee(BigDecimal.ZERO)
        .discountAmount(financials.discountAmount())
        .totalAmount(financials.totalAmount())
        .subtotal(financials.subtotal())
        .providerCurrency(product.pricing().pricingViewProvider().providerCurrency())
        .providerUnitPrice(product.pricing().pricingViewProvider().providerUnitPrice())
        .unitMarkup(product.pricing().pricingViewProvider().unitMarkup())
        .totalMarkup(BigDecimalUtils.multiplyAndRound(
            product.pricing().pricingViewProvider().unitMarkup(),
            itemRequest.quantity()))
        .exchangeRate(exchangeRate)
        .build();

    // Set variant configuration if applicable
    if (pricingInfo.variant() != null) {
      orderItem.setConfigId(pricingInfo.variant().configId());
      orderItem.setProductConfiguration(
          checkoutMapper.formatVariantConfigurators(pricingInfo.variant()));
    }

    return orderItem;
  }

  /**
   * Saves the order to the database.
   */
  private Order saveOrder(Order order) {
    return orderRepository.save(order);
  }

  /**
   * Validates the payment method from checkout request.
   * Checks if payment method exists, is active, and supports the currency.
   */
  private void validatePaymentMethod(CheckoutRequest request, SupportedCurrency currency) {
    // Find payment method item by payment type (itemCode)
    PaymentMethodItem item = paymentMethodItemRepository
        .findByPaymentType(request.paymentProviderCode())
        .orElseThrow(() -> {
          log.error("Payment method not found | code={}", request.paymentProviderCode());
          return new PaymentMethodNotSupportedException(request.paymentProviderCode(),
              "Payment method does not exist");
        });

    // Validate item is active
    if (item.getStatus() == null || item.getStatus() != 1) {
      log.error("Payment method not active | code={}", request.paymentProviderCode());
      throw new PaymentMethodNotSupportedException(request.paymentProviderCode(),
          "Payment method is not active");
    }

    // Validate currency is supported
    if (!currency.name().equals(item.getPaymentCurrency())) {
      log.error("Payment method currency not supported | code={}, currency={}, supported={}",
          request.paymentProviderCode(), currency, item.getPaymentCurrency());
      throw new PaymentMethodNotSupportedException(request.paymentProviderCode(),
          String.format("Payment method does not support currency %s", currency));
    }

    // If variant code is provided, validate it
    if (request.paymentProviderVariantCode() != null
        && !request.paymentProviderVariantCode().isBlank()) {
      validatePaymentMethodVariant(item, request.paymentProviderVariantCode(), currency);
    }
  }

  /**
   * Validates the payment method variant.
   */
  private void validatePaymentMethodVariant(PaymentMethodItem item, String variantCode,
      SupportedCurrency currency) {
    List<PaymentMethodItemVariant> variants = paymentMethodItemVariantRepository
        .findByPaymentMethodItemId(item.getId());

    if (variants.isEmpty()) {
      return;
    }

    PaymentMethodItemVariant activeVariant = variants.stream()
        .filter(variant -> variant.getStatus() != null && variant.getStatus() == 1)
        .filter(variant -> variantCode.equals(variant.getPaymentType()))
        .filter(variant -> currency.name().equals(variant.getPaymentCurrency()))
        .findFirst()
        .orElse(null);

    if (activeVariant == null) {
      log.error("Payment method variant not found or invalid | itemId={}, variantCode={}, currency={}",
          item.getId(), variantCode, currency);
      throw new PaymentMethodNotSupportedException(variantCode,
          String.format("Payment method variant is not active or does not support currency %s",
              currency));
    }
  }

  /**
   * Links cart to order and marks cart as converted.
   * Professional cart-to-order tracking for analytics and customer journey insights.
   */
  private void linkAndConvertCart(Customer customer, Order order) {
    try {
      // Find active cart for customer
      cartRepository.findActiveCartByCustomerId(customer.getId())
          .ifPresent(cart -> {
            // Link cart ID to order
            order.setCartId(cart.getId());
            orderRepository.save(order);

            // Mark cart as converted
            cart.markAsConverted(order.getOrderNumber());
            cartRepository.save(cart);
          });
    } catch (Exception e) {
      // Cart linking is non-critical - don't fail checkout if it errors
      log.warn("Failed to link cart to order, continuing with checkout | error={}", e.getMessage());
    }
  }

  //  Inner Classes

  /**
   * Holds variant and pricing information for an item.
   */
  private record VariantPricingInfo(
      ProductVariantView variant,
      BigDecimal unitPrice,
      Integer stockLevel) {

  }

  /**
   * Holds financial calculations for an order item.
   */
  private record OrderItemFinancials(
      BigDecimal subtotal,
      BigDecimal discountAmount,
      BigDecimal totalAmount) {

  }
}
