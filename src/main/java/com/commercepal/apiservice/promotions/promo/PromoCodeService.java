//package com.commercepal.apiservice.promotions.promo;
//
//import com.commercepal.apiservice.shared.exceptions.resource.ResourceNotFoundException;
//import com.commercepal.apiservice.orders.core.model.Order;
//import com.commercepal.apiservice.promotions.promo.dto.PromoCodeRequest;
//import com.commercepal.apiservice.utils.ConditionalUpdateUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.sql.Timestamp;
//import java.time.Instant;
//import java.util.List;
//import java.util.Objects;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PromoCodeService {
//
//    private final PromoCodeRepository promoCodeRepository;
//    private final PromoCodeUsageRepository promoCodeUsageRepository;
//
//    public void validatePromoCode(PromoCode code, Order order) {
//        Instant now = Instant.now();
//
//        if (!Boolean.TRUE.equals(code.getIsActive())) {
//            throw new IllegalArgumentException("Promo code is inactive");
//        }
//        if (code.getStartDate() != null && now.isBefore(code.getStartDate().toInstant())) {
//            throw new IllegalArgumentException("Promo code not started yet");
//        }
//        if (code.getEndDate() != null && now.isAfter(code.getEndDate().toInstant())) {
//            throw new IllegalArgumentException("Promo code has expired");
//        }
//
//        // Check min order value
//        if (code.getMinimumOrderAmount() != null && order.getTotalPrice().compareTo(code.getMinimumOrderAmount()) < 0) {
//            throw new IllegalArgumentException("Order does not meet minimum amount");
//        }
//
//        // Check usage limits
//        Integer totalUsed = promoCodeUsageRepository.countByPromoCodeId(code.getId());
//        if (code.getTotalUsageLimit() != null && totalUsed >= code.getTotalUsageLimit()) {
//            throw new IllegalArgumentException("Promo code usage limit reached");
//        }
//
//        Integer customerUsed = promoCodeUsageRepository.countByPromoCodeIdAndCustomerId(code.getId(),
//                order.getCustomerId());
//        if (code.getPerCustomerUsageLimit() != null && customerUsed >= code.getPerCustomerUsageLimit()) {
//            throw new IllegalArgumentException("Promo code already used by customer");
//        }
//
//        // Scope logic
//        if (code.getScope() == PromoCodeScope.CUSTOMER && !Objects.equals(code.getApplicableCustomerId(),
//                order.getCustomerId())) {
//            throw new IllegalArgumentException("Promo code not valid for this customer");
//        }
//    }
//
//    private void savePromoUsage(Long customerId, Long promoCodeId) {
//        PromoCodeUsage usage = promoCodeUsageRepository
//                .findByPromoCodeIdAndCustomerId(promoCodeId, customerId)
//                .orElseGet(() -> {
//                    PromoCodeUsage u = new PromoCodeUsage();
//                    u.setPromoCode(promoCodeRepository.getById(promoCodeId));
//                    u.setCustomerId(customerId);
//                    u.setUsageCount(0);
//                    return u;
//                });
//        usage.setUsageCount(usage.getUsageCount() + 1);
//        usage.setLastUsedAt(Timestamp.from(Instant.now()));
//        promoCodeUsageRepository.save(usage);
//    }
//
////    public void applyPromoCodeToOrder(Order order, PromoCode code) {
////        validatePromoCode(code, order);
////
////        BigDecimal discount = BigDecimal.ZERO;
////
////        switch (code.getScope()) {
////            case GLOBAL -> {
////                discount = calculateDiscount(code, order.getSubtotal());
////            }
////            case CATEGORY -> {
////                discount = order
////                        .getItems()
////                        .stream()
////                        .filter(i -> Objects.equals(i.getCategoryId(), code.getApplicableCategoryId()))
////                        .map(OrderItem::getTotalPrice)
////                        .reduce(BigDecimal.ZERO, BigDecimal::add);
////                discount = calculateDiscount(code, discount);
////            }
////            case PRODUCT -> {
////                discount = order
////                        .getItems()
////                        .stream()
////                        .filter(i -> Objects.equals(i.getProductId(), code.getApplicableProductId()))
////                        .map(OrderItem::getTotalPrice)
////                        .reduce(BigDecimal.ZERO, BigDecimal::add);
////                discount = calculateDiscount(code, discount);
////            }
////        }
////
////        order.setPromoCode(code);
////        order.setDiscount(discount);
////        order.setTotal(order.getSubtotal().subtract(discount));
////
////        // Save promo code usage
////        savePromoUsage(order.getCustomerId(), code.getId());
////    }
//
//
//    private BigDecimal calculateDiscount(PromoCode code, BigDecimal baseAmount) {
//        return switch (code.getDiscountType()) {
//            case FIXED -> code.getDiscountValue().min(baseAmount);
//            case PERCENTAGE -> baseAmount.multiply(
//                    code.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
//        };
//    }
//
//
//    public PromoCode createPromoCode(PromoCodeRequest request) {
//        if (promoCodeRepository.existsByCode(request.getCode())) {
//            throw new IllegalArgumentException("Promo code already exists");
//        }
//
//        Timestamp now = new Timestamp(System.currentTimeMillis());
//
//        PromoCode promo = new PromoCode();
//        promo.setCode(request.getCode().toUpperCase());
//        promo.setDiscountType(request.getDiscountType());
//        promo.setDiscountValue(request.getDiscountValue());
//        promo.setMinimumOrderAmount(request.getMinimumOrderAmount());
//        promo.setScope(request.getScope());
//        promo.setApplicableProductId(request.getApplicableProductId());
//        promo.setApplicableCategoryId(request.getApplicableCategoryId());
//        promo.setApplicableCustomerId(request.getApplicableCustomerId());
//        promo.setStartDate(request.getStartDate());
//        promo.setEndDate(request.getEndDate());
//        promo.setTotalUsageLimit(request.getTotalUsageLimit());
//        promo.setPerCustomerUsageLimit(request.getPerCustomerUsageLimit());
//        promo.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
//        promo.setCreatedAt(now);
//        promo.setUpdatedAt(now);
//
//        return promoCodeRepository.save(promo);
//    }
//
//    public PromoCode updatePromoCode(Long id, PromoCodeRequest request) {
//        PromoCode promo = promoCodeRepository
//                .findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));
//
//        boolean isUpdated = false;
//
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setCode, request.getCode(), promo.getCode());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setDiscountType, request.getDiscountType(),
//                promo.getDiscountType());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setDiscountValue, request.getDiscountValue(),
//                promo.getDiscountValue());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setMinimumOrderAmount, request.getMinimumOrderAmount(),
//                promo.getMinimumOrderAmount());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setScope, request.getScope(), promo.getScope());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setApplicableProductId, request.getApplicableProductId(),
//                promo.getApplicableProductId());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setApplicableCategoryId, request.getApplicableCategoryId(),
//                promo.getApplicableCategoryId());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setApplicableCustomerId, request.getApplicableCustomerId(),
//                promo.getApplicableCustomerId());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setStartDate, request.getStartDate(), promo.getStartDate());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setEndDate, request.getEndDate(), promo.getEndDate());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setTotalUsageLimit, request.getTotalUsageLimit(),
//                promo.getTotalUsageLimit());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setPerCustomerUsageLimit, request.getPerCustomerUsageLimit(),
//                promo.getPerCustomerUsageLimit());
//        isUpdated |= ConditionalUpdateUtils.updateIfChanged(promo::setIsActive, request.getIsActive(), promo.getIsActive());
//
//        if (isUpdated) {
//            promo.setUpdatedAt(Timestamp.from(Instant.now()));
//            promoCodeRepository.save(promo);
//        }
//
//        return promo;
//    }
//
//    public List<PromoCode> getAllActivePromoCodes() {
//        return promoCodeRepository.findAllActive();
//    }
//
//    public List<PromoCode> getAll() {
//        return promoCodeRepository.findAllByOrderByUpdatedAtDesc();
//    }
//
//    public PromoCode getByCode(String code) {
//        return promoCodeRepository
//                .findByCode(code)
//                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));
//    }
//}
