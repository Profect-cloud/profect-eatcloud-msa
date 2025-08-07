package profect.eatcloud.domain.order.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import profect.eatcloud.domain.order.dto.AdminOrderCompleteRequestDto;
import profect.eatcloud.domain.order.dto.AdminOrderConfirmRequestDto;
import profect.eatcloud.domain.order.dto.AdminOrderResponseDto;
import profect.eatcloud.domain.order.service.AdminOrderService;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "8-3.Manager - Order", description = "사장님용 주문 관리 API")
public class ManagerOrderController {

	private final AdminOrderService adminOrderService;

	@PostMapping("/confirm")
	@Operation(summary = "주문 수락", description = "결제 완료된 주문을 수락합니다. (PAID → CONFIRMED)")
	public ResponseEntity<AdminOrderResponseDto> confirmOrder(
		@RequestBody AdminOrderConfirmRequestDto request) {

		log.info("주문 수락 요청: orderId={}", request.getOrderId());

		try {
			AdminOrderResponseDto response = adminOrderService.confirmOrder(request.getOrderId());
			log.info("주문 수락 완료: orderId={}, orderNumber={}",
				response.getOrderId(), response.getOrderNumber());

			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			log.error("주문 수락 실패: orderId={}, error={}", request.getOrderId(), e.getMessage());

			AdminOrderResponseDto errorResponse = AdminOrderResponseDto.builder()
				.orderId(request.getOrderId())
				.orderNumber(null)
				.orderStatus("ERROR")
				.message(e.getMessage())
				.build();

			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	@PostMapping("/complete")
	@Operation(summary = "주문 완료", description = "수락된 주문을 완료 처리합니다. (CONFIRMED → COMPLETED)")
	public ResponseEntity<AdminOrderResponseDto> completeOrder(
		@RequestBody AdminOrderCompleteRequestDto request) {

		log.info("주문 완료 요청: orderId={}", request.getOrderId());

		try {
			AdminOrderResponseDto response = adminOrderService.completeOrder(request.getOrderId());
			log.info("주문 완료 처리 완료: orderId={}, orderNumber={}",
				response.getOrderId(), response.getOrderNumber());

			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			log.error("주문 완료 실패: orderId={}, error={}", request.getOrderId(), e.getMessage());

			AdminOrderResponseDto errorResponse = AdminOrderResponseDto.builder()
				.orderId(request.getOrderId())
				.orderNumber(null)
				.orderStatus("ERROR")
				.message(e.getMessage())
				.build();

			return ResponseEntity.badRequest().body(errorResponse);
		}
	}

	@GetMapping("/{orderId}/status")
	@Operation(summary = "주문 상태 조회", description = "주문 ID로 현재 주문 상태를 조회합니다.")
	public ResponseEntity<AdminOrderResponseDto> getOrderStatus(
		@Parameter(description = "주문 ID") @PathVariable UUID orderId) {

		log.info("주문 상태 조회 요청: orderId={}", orderId);

		try {
			AdminOrderResponseDto response = adminOrderService.getOrderStatus(orderId);
			log.info("주문 상태 조회 완료: orderId={}, status={}", orderId, response.getOrderStatus());

			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			log.error("주문 상태 조회 실패: orderId={}, error={}", orderId, e.getMessage());

			AdminOrderResponseDto errorResponse = AdminOrderResponseDto.builder()
				.orderId(orderId)
				.orderNumber(null)
				.orderStatus("ERROR")
				.message(e.getMessage())
				.build();

			return ResponseEntity.badRequest().body(errorResponse);
		}
	}
}
