package com.commercepal.apiservice.orders.internal;//package com.commercepal.apiservice.orders.internal;
//
//import com.commercepal.apiservice.orders.api.Order;
//import com.commercepal.apiservice.orders.api.OrderService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
/// **
// * REST controller for order operations.
// */
//@RestController
//@RequestMapping("/api/v1/orders")
//@RequiredArgsConstructor
//public class CheckoutController {
//
//	private final OrderService orderService;
//
//	@PostMapping
//	public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
//		return ResponseEntity.status(HttpStatus.CREATED)
//			.body(orderService.createOrder(order));
//	}
//
//	@GetMapping("/{id}")
//	public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
//		return orderService.findById(id)
//			.map(ResponseEntity::ok)
//			.orElse(ResponseEntity.notFound().build());
//	}
//
//	@GetMapping("/number/{orderNumber}")
//	public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
//		return orderService.findByOrderNumber(orderNumber)
//			.map(ResponseEntity::ok)
//			.orElse(ResponseEntity.notFound().build());
//	}
//
//	@GetMapping("/customer/{customerId}")
//	public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerId) {
//		return ResponseEntity.ok(orderService.findByCustomerId(customerId));
//	}
//
//	@PatchMapping("/{id}/status")
//	public ResponseEntity<Order> updateOrderStatus(
//		@PathVariable UUID id,
//		@RequestBody OrderStatusUpdateRequest request) {
//		return ResponseEntity.ok(orderService.updateOrderStatus(id, request.status()));
//	}
//
//	public record OrderStatusUpdateRequest(Order.OrderStatus status) {}
//}
//
