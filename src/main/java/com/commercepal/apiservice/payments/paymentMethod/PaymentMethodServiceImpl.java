package com.commercepal.apiservice.payments.paymentMethod;

import com.commercepal.apiservice.payments.paymentMethod.dto.PaymentMethodItemResponse;
import com.commercepal.apiservice.payments.paymentMethod.dto.PaymentMethodItemVariantResponse;
import com.commercepal.apiservice.payments.paymentMethod.dto.PaymentMethodResponse;
import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethod;
import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethodItem;
import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethodItemVariant;
import com.commercepal.apiservice.payments.paymentMethod.repository.PaymentMethodItemRepository;
import com.commercepal.apiservice.payments.paymentMethod.repository.PaymentMethodItemVariantRepository;
import com.commercepal.apiservice.payments.paymentMethod.repository.PaymentMethodRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for payment method operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentMethodServiceImpl implements PaymentMethodService {

  private static final Integer ACTIVE_STATUS = 1;

  private final PaymentMethodRepository paymentMethodRepository;
  private final PaymentMethodItemRepository paymentMethodItemRepository;
  private final PaymentMethodItemVariantRepository paymentMethodItemVariantRepository;

  @Override
  public List<PaymentMethodResponse> getAllPaymentMethods() {
    log.debug("Fetching all active payment methods");

    // Fetch all active payment methods
    List<PaymentMethod> paymentMethods = paymentMethodRepository.findActivePaymentMethods();
    if (paymentMethods.isEmpty()) {
      log.debug("No active payment methods found");
      return List.of();
    }

    // Extract payment method IDs
    List<Integer> paymentMethodIds = paymentMethods.stream()
        .map(PaymentMethod::getId)
        .toList();

    // Fetch all active items for these payment methods in one query (avoid N+1)
    List<PaymentMethodItem> allItems = paymentMethodItemRepository.findActivePaymentMethodItems()
        .stream()
        .filter(item -> paymentMethodIds.contains(item.getPaymentMethodId()))
        .toList();

    // Extract payment method item IDs
    List<Integer> paymentMethodItemIds = allItems.stream()
        .map(PaymentMethodItem::getId)
        .toList();

    // Fetch all active variants for these items in one query (avoid N+1)
    List<PaymentMethodItemVariant> allVariants = paymentMethodItemVariantRepository
        .findActivePaymentMethodItemVariants()
        .stream()
        .filter(variant -> paymentMethodItemIds.contains(variant.getPaymentMethodItemId()))
        .toList();

    // Group items by payment method ID
    Map<Integer, List<PaymentMethodItem>> itemsByPaymentMethodId = allItems.stream()
        .collect(Collectors.groupingBy(PaymentMethodItem::getPaymentMethodId));

    // Group variants by payment method item ID
    Map<Integer, List<PaymentMethodItemVariant>> variantsByItemId = allVariants.stream()
        .collect(Collectors.groupingBy(PaymentMethodItemVariant::getPaymentMethodItemId));

    // Build response
    List<PaymentMethodResponse> response = new ArrayList<>();
    for (PaymentMethod paymentMethod : paymentMethods) {
      List<PaymentMethodItem> items = itemsByPaymentMethodId.getOrDefault(paymentMethod.getId(),
          List.of());
      List<PaymentMethodItemResponse> itemResponses = items.stream()
          .map(item -> mapToItemResponse(item,
              variantsByItemId.getOrDefault(item.getId(), List.of())))
          .collect(Collectors.toList());

      PaymentMethodResponse paymentMethodResponse = PaymentMethodResponse.builder()
          .displayName(paymentMethod.getName())
          .code(paymentMethod.getPaymentMethod())
          .iconUrl(paymentMethod.getIconUrl())
          .paymentMethodItemResponses(itemResponses)
          .build();

      response.add(paymentMethodResponse);
    }

    log.debug("Successfully fetched {} payment methods", response.size());
    return response;
  }

  /**
   * Maps PaymentMethodItem entity to PaymentMethodItemResponse DTO.
   */
  private PaymentMethodItemResponse mapToItemResponse(
      PaymentMethodItem item,
      List<PaymentMethodItemVariant> variants) {
    List<PaymentMethodItemVariantResponse> variantResponses = variants.stream()
        .map(this::mapToVariantResponse)
        .collect(Collectors.toList());

    return PaymentMethodItemResponse.builder()
        .displayName(item.getName())
        .itemCode(item.getPaymentType())
        .currency(item.getPaymentCurrency())
        .iconUrl(item.getIconUrl())
        .paymentInstruction(item.getPaymentInstruction())
        .paymentMethodItemResponses(variantResponses)
        .build();
  }

  /**
   * Maps PaymentMethodItemVariant entity to PaymentMethodItemVariantResponse DTO.
   */
  private PaymentMethodItemVariantResponse mapToVariantResponse(PaymentMethodItemVariant variant) {
    return PaymentMethodItemVariantResponse.builder()
        .displayName(variant.getName())
        .variantCode(variant.getPaymentType())
        .currency(variant.getPaymentCurrency())
        .iconUrl(variant.getIconUrl())
        .paymentInstruction(variant.getPaymentInstruction())
        .build();
  }
}
