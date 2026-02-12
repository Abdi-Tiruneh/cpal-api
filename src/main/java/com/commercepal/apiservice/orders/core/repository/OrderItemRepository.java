package com.commercepal.apiservice.orders.core.repository;

import com.commercepal.apiservice.orders.core.model.OrderItem;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  @Query("SELECT oi.productName, SUM(oi.quantity), SUM(oi.totalAmount) " +
      "FROM OrderItem oi " +
      "GROUP BY oi.productName " +
      "ORDER BY SUM(oi.totalAmount) DESC")
  List<Object[]> findTopSellingProducts(Pageable pageable);


  List<OrderItem> findByOrderId(Long orderId);
}
