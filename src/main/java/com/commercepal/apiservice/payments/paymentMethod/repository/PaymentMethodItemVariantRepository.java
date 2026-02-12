package com.commercepal.apiservice.payments.paymentMethod.repository;

import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethodItemVariant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for PaymentMethodItemVariant persistence.
 */
@Repository
public interface PaymentMethodItemVariantRepository extends
    JpaRepository<PaymentMethodItemVariant, Integer>,
    JpaSpecificationExecutor<PaymentMethodItemVariant> {

  /**
   * Find payment method item variants by payment method item ID
   */
  List<PaymentMethodItemVariant> findByPaymentMethodItemId(Integer paymentMethodItemId);

  /**
   * Find payment method item variants by user type
   */
  List<PaymentMethodItemVariant> findByUserType(String userType);

  /**
   * Find payment method item variants by payment type
   */
  List<PaymentMethodItemVariant> findByPaymentType(String paymentType);

  /**
   * Find payment method item variants by payment currency
   */
  List<PaymentMethodItemVariant> findByPaymentCurrency(String paymentCurrency);

  /**
   * Find payment method item variants by status
   */
  List<PaymentMethodItemVariant> findByStatus(Integer status);

  /**
   * Find payment method item variants by payment method item ID and user type
   */
  List<PaymentMethodItemVariant> findByPaymentMethodItemIdAndUserType(Integer paymentMethodItemId,
      String userType);

  /**
   * Find payment method item variants by payment method item ID and payment currency
   */
  List<PaymentMethodItemVariant> findByPaymentMethodItemIdAndPaymentCurrency(
      Integer paymentMethodItemId, String paymentCurrency);

  /**
   * Find active payment method item variants
   */
  @Query("SELECT pmiv FROM PaymentMethodItemVariant pmiv WHERE pmiv.status = 1")
  List<PaymentMethodItemVariant> findActivePaymentMethodItemVariants();

  /**
   * Find active payment method item variants by payment method item ID
   */
  @Query("SELECT pmiv FROM PaymentMethodItemVariant pmiv WHERE pmiv.status = 1 AND pmiv.paymentMethodItemId = :paymentMethodItemId")
  List<PaymentMethodItemVariant> findActivePaymentMethodItemVariantsByPaymentMethodItemId(
      @Param("paymentMethodItemId") Integer paymentMethodItemId);

  /**
   * Find active payment method item variants by payment method item ID and user type
   */
  @Query("SELECT pmiv FROM PaymentMethodItemVariant pmiv WHERE pmiv.status = 1 AND pmiv.paymentMethodItemId = :paymentMethodItemId AND pmiv.userType = :userType")
  List<PaymentMethodItemVariant> findActivePaymentMethodItemVariantsByPaymentMethodItemIdAndUserType(
      @Param("paymentMethodItemId") Integer paymentMethodItemId,
      @Param("userType") String userType);

  /**
   * Find payment method item variant by name
   */
  Optional<PaymentMethodItemVariant> findByName(String name);

  /**
   * Check if payment method item variant name exists
   */
  boolean existsByName(String name);
}
