package profect.eatcloud.domain.order.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import profect.eatcloud.domain.order.dto.request.OrderStatusUpdateRequest;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.service.OrderService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "8-1. Order Management")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@Operation(summary = "주문 조회", description = "주문 ID로 주문 정보를 조회합니다.")
	@GetMapping("/orders/{orderId}")
	public ResponseEntity<Map<String, Object>> getOrder(@PathVariable UUID orderId) {
		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Order> order = orderService.findById(orderId);

			if (order.isPresent()) {
				Order foundOrder = order.get();
				response.put("orderId", foundOrder.getOrderId());
				response.put("orderNumber", foundOrder.getOrderNumber());
				response.put("customerId", foundOrder.getCustomerId());
				response.put("storeId", foundOrder.getStoreId());
				response.put("paymentId", foundOrder.getPaymentId());
				response.put("orderMenuList", foundOrder.getOrderMenuList());
				response.put("orderStatus", foundOrder.getOrderStatusCode().getCode());
				response.put("orderType", foundOrder.getOrderTypeCode().getCode());
				response.put("createdAt", foundOrder.getTimeData().getCreatedAt());
				response.put("message", "주문 조회 성공");

				return ResponseEntity.ok(response);
			} else {
				response.put("error", "주문을 찾을 수 없습니다.");
				return ResponseEntity.notFound().build();
			}

		} catch (Exception e) {
			response.put("error", "주문 조회 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

	@Operation(summary = "주문 번호로 주문 조회", description = "주문 번호로 주문 정보를 조회합니다.")
	@GetMapping("/orders/number/{orderNumber}")
	public ResponseEntity<Map<String, Object>> getOrderByNumber(@PathVariable String orderNumber) {
		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Order> order = orderService.findOrderByNumber(orderNumber);

			if (order.isPresent()) {
				Order foundOrder = order.get();
				response.put("orderId", foundOrder.getOrderId());
				response.put("orderNumber", foundOrder.getOrderNumber());
				response.put("customerId", foundOrder.getCustomerId());
				response.put("storeId", foundOrder.getStoreId());
				response.put("paymentId", foundOrder.getPaymentId());
				response.put("orderMenuList", foundOrder.getOrderMenuList());
				response.put("orderStatus", foundOrder.getOrderStatusCode().getCode());
				response.put("orderType", foundOrder.getOrderTypeCode().getCode());
				response.put("createdAt", foundOrder.getTimeData().getCreatedAt());
				response.put("message", "주문 조회 성공");

				return ResponseEntity.ok(response);
			} else {
				response.put("error", "주문을 찾을 수 없습니다.");
				return ResponseEntity.notFound().build();
			}

		} catch (Exception e) {
			response.put("error", "주문 조회 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(response);
		}
	}

	@Operation(summary = "고객 주문 목록 조회")
	@GetMapping("/customers/{customerId}/orders")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable UUID customerId) {
		return ResponseEntity.ok(orderService.findOrdersByCustomer(customerId));
	}

	@Operation(summary = "고객 주문 상세 조회")
	@GetMapping("/customers/{customerId}/orders/{orderId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Order> getCustomerOrderDetail(@PathVariable UUID customerId, @PathVariable UUID orderId) {
		return ResponseEntity.ok(orderService.findOrderByCustomerAndOrderId(customerId, orderId));
	}

	@Operation(summary = "매장 주문 목록 조회")
	@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
	@GetMapping("/stores/{storeId}/orders")
	public ResponseEntity<List<Order>> getStoreOrders(@PathVariable UUID storeId) {
		return ResponseEntity.ok(orderService.findOrdersByStore(storeId));
	}

	@Operation(summary = "매장 주문 상세 조회")
	@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
	@GetMapping("/stores/{storeId}/orders/{orderId}")
	public ResponseEntity<Order> getStoreOrderDetail(@PathVariable UUID orderId, @PathVariable UUID storeId) {
		return ResponseEntity.ok(orderService.findOrderByStoreAndOrderId(storeId, orderId));
	}

	@Operation(summary = "주문 상태 변경")
	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/orders/{orderId}/status")
	public ResponseEntity<Void> updateOrderStatus(@PathVariable UUID orderId,
		@RequestBody @Valid OrderStatusUpdateRequest request) {
		orderService.updateOrderStatus(orderId, request.getStatusCode());
		return ResponseEntity.noContent().build();
	}
}