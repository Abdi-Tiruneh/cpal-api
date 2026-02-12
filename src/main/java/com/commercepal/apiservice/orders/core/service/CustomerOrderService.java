package com.commercepal.apiservice.orders.core.service;

import com.commercepal.apiservice.orders.core.customer.dto.CustomerOrderDetailsDto;
import com.commercepal.apiservice.orders.core.customer.dto.CustomerOrderListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerOrderService {

  Page<CustomerOrderListDto> getMyOrders(Long customerId, Pageable pageable);

  CustomerOrderDetailsDto getOrderDetails(Long customerId, String orderNumber);
}
