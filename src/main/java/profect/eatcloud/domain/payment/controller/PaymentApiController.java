package profect.eatcloud.domain.payment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import profect.eatcloud.domain.payment.dto.TossPaymentResponse;
import profect.eatcloud.domain.payment.exception.PaymentValidationException;
import profect.eatcloud.domain.payment.service.TossPaymentService;

@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "9-1. PaymentController")
public class PaymentApiController {

	private final TossPaymentService tossPaymentService;

	public PaymentApiController(TossPaymentService tossPaymentService) {
		this.tossPaymentService = tossPaymentService;
	}

	@Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 처리합니다.")
	@PostMapping("/confirm")
	public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> request) {
		try {
			String paymentKey = (String)request.get("paymentKey");
			String orderId = (String)request.get("orderId");
			Integer amount = (Integer)request.get("amount");

			TossPaymentResponse response = tossPaymentService.confirmPayment(paymentKey, orderId, amount);

			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			result.put("data", response);
			result.put("message", "결제가 성공적으로 처리되었습니다.");

			return ResponseEntity.ok(result);

		} catch (PaymentValidationException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	@Operation(summary = "결제 상태 확인", description = "주문 ID로 결제 상태를 조회합니다.")
	@GetMapping("/status/{orderId}")
	public ResponseEntity<?> getPaymentStatus(@PathVariable String orderId) {
		Map<String, Object> result = new HashMap<>();
		result.put("orderId", orderId);
		result.put("status", "pending");
		result.put("message", "결제 대기 중입니다.");

		return ResponseEntity.ok(result);
	}

	@Operation(summary = "결제 검증", description = "결제 정보의 유효성을 검증합니다.")
	@PostMapping("/validate")
	public ResponseEntity<?> validatePayment(@RequestBody Map<String, Object> request) {
		try {
			String paymentKey = (String)request.get("paymentKey");
			String orderId = (String)request.get("orderId");
			Integer amount = (Integer)request.get("amount");

			tossPaymentService.confirmPayment(paymentKey, orderId, amount);

			Map<String, Object> result = new HashMap<>();
			result.put("valid", true);
			result.put("message", "결제 정보가 유효합니다.");

			return ResponseEntity.ok(result);

		} catch (PaymentValidationException e) {
			Map<String, Object> result = new HashMap<>();
			result.put("valid", false);
			result.put("error", e.getMessage());
			result.put("errorCode", e.getErrorCode());

			return ResponseEntity.badRequest().body(result);
		}
	}
} 