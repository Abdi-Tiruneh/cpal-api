package com.commercepal.apiservice.payments.paymentMethod.repository;

import com.commercepal.apiservice.payments.paymentMethod.model.PaymentMethod;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for PaymentMethod persistence.
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer>,
    JpaSpecificationExecutor<PaymentMethod> {

  /**
   * Find payment method by name
   */
  Optional<PaymentMethod> findByName(String name);

  /**
   * Find payment methods by payment method type
   */
  List<PaymentMethod> findByPaymentMethod(String paymentMethod);

  /**
   * Find payment methods by user type
   */
  List<PaymentMethod> findByUserType(String userType);

  /**
   * Find payment methods by status
   */
  List<PaymentMethod> findByStatus(Integer status);

  /**
   * Find payment methods by payment method type and user type
   */
  List<PaymentMethod> findByPaymentMethodAndUserType(String paymentMethod, String userType);

  /**
   * Find active payment methods (status = 1 or active)
   */
  @Query("SELECT pm FROM PaymentMethod pm WHERE pm.status = 1")
  List<PaymentMethod> findActivePaymentMethods();

  /**
   * Find active payment methods by user type
   */
  @Query("SELECT pm FROM PaymentMethod pm WHERE pm.status = 1 AND pm.userType = :userType")
  List<PaymentMethod> findActivePaymentMethodsByUserType(@Param("userType") String userType);

  /**
   * Check if payment method name exists
   */
  boolean existsByName(String name);
}
