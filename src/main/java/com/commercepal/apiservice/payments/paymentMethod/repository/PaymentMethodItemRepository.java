package com.commercepal.apiservice.payments.paymentMethod.repository;

import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethodItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for PaymentMethodItem persistence.
 */
@Repository
public interface PaymentMethodItemRepository extends JpaRepository<PaymentMethodItem, Integer>,
    JpaSpecificationExecutor<PaymentMethodItem> {

  /**
   * Find payment method items by payment method ID
   */
  List<PaymentMethodItem> findByPaymentMethodId(Integer paymentMethodId);

  /**
   * Find payment method items by user type
   */
  List<PaymentMethodItem> findByUserType(String userType);

  /**
   * Find payment method items by payment type
   */
  Optional<PaymentMethodItem> findByPaymentType(String paymentType);

  /**
   * Find payment method items by payment currency
   */
  List<PaymentMethodItem> findByPaymentCurrency(String paymentCurrency);

  /**
   * Find payment method items by status
   */
  List<PaymentMethodItem> findByStatus(Integer status);

  /**
   * Find payment method items by payment method ID and user type
   */
  List<PaymentMethodItem> findByPaymentMethodIdAndUserType(Integer paymentMethodId,
      String userType);

  /**
   * Find payment method items by payment method ID and payment currency
   */
  List<PaymentMethodItem> findByPaymentMethodIdAndPaymentCurrency(Integer paymentMethodId,
      String paymentCurrency);

  /**
   * Find active payment method items
   */
  @Query("SELECT pmi FROM PaymentMethodItem pmi WHERE pmi.status = 1")
  List<PaymentMethodItem> findActivePaymentMethodItems();

  /**
   * Find active payment method items by payment method ID
   */
  @Query("SELECT pmi FROM PaymentMethodItem pmi WHERE pmi.status = 1 AND pmi.paymentMethodId = :paymentMethodId")
  List<PaymentMethodItem> findActivePaymentMethodItemsByPaymentMethodId(
      @Param("paymentMethodId") Integer paymentMethodId);

  /**
   * Find active payment method items by payment method ID and user type
   */
  @Query("SELECT pmi FROM PaymentMethodItem pmi WHERE pmi.status = 1 AND pmi.paymentMethodId = :paymentMethodId AND pmi.userType = :userType")
  List<PaymentMethodItem> findActivePaymentMethodItemsByPaymentMethodIdAndUserType(
      @Param("paymentMethodId") Integer paymentMethodId,
      @Param("userType") String userType);

  /**
   * Find payment method item by name
   */
  Optional<PaymentMethodItem> findByName(String name);

  /**
   * Check if payment method item name exists
   */
  boolean existsByName(String name);
}
