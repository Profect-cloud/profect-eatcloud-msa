package profect.eatcloud.domain.customer.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import profect.eatcloud.domain.customer.dto.response.CustomerOrderResponse;
import profect.eatcloud.domain.customer.service.CustomerOrderService;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.service.OrderService;
import profect.eatcloud.security.SecurityUtil;

@RestController
@RequestMapping("/api/v1/customers/orders")
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "3-4. CustomerOrderController", description = "고객 주문 생성 API")
public class CustomerOrderController {

	private final CustomerOrderService customerOrderService;
	private final OrderService orderService;

	public CustomerOrderController(OrderService orderService, CustomerOrderService customerOrderService) {
		this.customerOrderService = customerOrderService;
		this.orderService = orderService;
	}

	private UUID getCustomerUuid(@AuthenticationPrincipal UserDetails userDetails) {
		return UUID.fromString(userDetails.getUsername());
	}

	@Operation(summary = "고객 주문 생성", description = "고객의 장바구니에서 주문을 생성합니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<CustomerOrderResponse> createOrder(
		@RequestParam String orderTypeCode,
		@RequestParam(required = false, defaultValue = "false") Boolean usePoints,
		@RequestParam(required = false, defaultValue = "0") Integer pointsToUse) {

		String customerIdStr = SecurityUtil.getCurrentUsername();
		UUID customerId = UUID.fromString(customerIdStr);

		Order order = customerOrderService.createOrder(customerId, orderTypeCode, usePoints, pointsToUse);
		CustomerOrderResponse dto = new CustomerOrderResponse(order, usePoints, pointsToUse);
		return profect.eatcloud.common.ApiResponse.success(dto);
	}

	@Operation(summary = "고객 주문 목록 조회")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<List<Order>> getMyOrders(
		@AuthenticationPrincipal UserDetails userDetails) {
		UUID customerId = getCustomerUuid(userDetails);
		return profect.eatcloud.common.ApiResponse.success(orderService.findOrdersByCustomer(customerId));
	}

	@Operation(summary = "고객 주문 상세 조회")
	@GetMapping("/{orderId}")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<Order> getMyOrder(@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable UUID orderId) {
		UUID customerId = getCustomerUuid(userDetails);
		return profect.eatcloud.common.ApiResponse.success(
			orderService.findOrderByCustomerAndOrderId(customerId, orderId));
	}
}
