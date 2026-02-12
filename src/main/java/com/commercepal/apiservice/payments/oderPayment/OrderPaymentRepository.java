package com.commercepal.apiservice.payments.oderPayment;

import com.commercepal.apiservice.orders.enums.PaymentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long>,
    JpaSpecificationExecutor<OrderPayment> {

  Optional<OrderPayment> findByReference(String reference);

  Optional<OrderPayment> findByReferenceAndStatus(String reference, PaymentStatus status);

  boolean existsByReference(String reference);
}
