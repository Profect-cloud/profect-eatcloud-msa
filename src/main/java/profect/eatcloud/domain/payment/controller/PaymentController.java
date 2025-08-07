package profect.eatcloud.domain.payment.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import profect.eatcloud.domain.payment.dto.CheckoutResponse;
import profect.eatcloud.domain.payment.dto.PaymentCallbackRequest;
import profect.eatcloud.domain.payment.dto.PointChargeRequest;
import profect.eatcloud.domain.payment.dto.PointChargeResponse;
import profect.eatcloud.domain.payment.service.PaymentAuthenticationService;
import profect.eatcloud.domain.payment.service.PaymentProcessingService;
import profect.eatcloud.domain.payment.util.PaymentDtoConverter;

@Controller
@RequestMapping("/api/v1/payment")
@Tag(name = "9-2. PaymentViewController")
@Slf4j
public class PaymentController {

	private final PaymentProcessingService paymentProcessingService;
	private final PaymentAuthenticationService paymentAuthenticationService;

	public PaymentController(PaymentProcessingService paymentProcessingService,
		PaymentAuthenticationService paymentAuthenticationService) {
		this.paymentProcessingService = paymentProcessingService;
		this.paymentAuthenticationService = paymentAuthenticationService;
	}

	@Operation(summary = "주문 페이지", description = "결제 주문 페이지를 표시합니다.")
	@GetMapping("/order")
	public String orderPage(Model model) {
		var authResult = paymentAuthenticationService.validateCustomerForOrderPage();

		if (!authResult.isSuccess()) {
			throw new RuntimeException("인증에 실패했습니다: " + authResult.getErrorMessage());
		}

		model.addAttribute("customerId", authResult.getCustomerIdAsString());
		model.addAttribute("customerPoints", authResult.getCustomerPoints());
		model.addAttribute("customerName", authResult.getCustomerName());

		return "order/order";
	}

	@Operation(summary = "결제 페이지", description = "주문 정보를 받아 결제 페이지로 이동합니다.")
	@PostMapping("/checkout")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> checkoutPage(@RequestParam("orderData") String orderDataJson) {
		try {
			CheckoutResponse response = paymentProcessingService.processCheckout(orderDataJson);
			return ResponseEntity.ok(PaymentDtoConverter.toMap(response));
		} catch (Exception e) {
			log.error("체크아웃 처리 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(PaymentDtoConverter.createErrorResponse(e.getMessage()));
		}
	}

	@Operation(summary = "포인트 충전 페이지", description = "포인트 충전을 위한 결제 페이지를 표시합니다.")
	@GetMapping("/charge")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getPaymentPage(@RequestParam(required = false) String userId,
		@RequestParam(required = false) Integer amount) {
		try {
			PointChargeRequest request = PointChargeRequest.builder()
				.userId(userId)
				.amount(amount)
				.build();

			PointChargeResponse response = paymentProcessingService.processPointCharge(request);
			return ResponseEntity.ok(PaymentDtoConverter.toMap(response));
		} catch (Exception e) {
			log.error("포인트 충전 페이지 생성 중 오류 발생", e);
			return ResponseEntity.badRequest()
				.body(PaymentDtoConverter.createErrorResponse(e.getMessage()));
		}
	}

	@Operation(summary = "결제 성공 콜백", description = "토스페이먼츠 결제 성공 콜백을 처리합니다.")
	@GetMapping("/success")
	public String paymentSuccess(@RequestParam String paymentKey,
		@RequestParam String orderId,
		@RequestParam Integer amount,
		Model model) {
		try {
			PaymentCallbackRequest request = PaymentCallbackRequest.builder()
				.paymentKey(paymentKey)
				.orderId(orderId)
				.amount(amount)
				.build();

			paymentProcessingService.processPaymentSuccess(request);

			model.addAttribute("paymentKey", paymentKey);
			model.addAttribute("orderId", orderId);
			model.addAttribute("amount", amount);
			model.addAttribute("message", "결제가 성공적으로 처리되었습니다.");

			return "payment/success";

		} catch (Exception e) {
			log.error("결제 성공 처리 중 오류 발생", e);
			model.addAttribute("error", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
			return "payment/fail";
		}
	}

	@Operation(summary = "결제 취소 콜백", description = "토스페이먼츠 결제 취소 콜백을 처리합니다.")
	@GetMapping("/cancel")
	public String paymentCancel(@RequestParam(required = false) String paymentKey,
		@RequestParam(required = false) String orderId,
		@RequestParam(required = false) Integer amount,
		@RequestParam(required = false) String message,
		@RequestParam(required = false) String code,
		Model model) {

		PaymentCallbackRequest request = PaymentCallbackRequest.builder()
			.paymentKey(paymentKey)
			.orderId(orderId)
			.amount(amount)
			.message(message)
			.code(code)
			.build();

		Map<String, Object> rollbackResult = paymentProcessingService.processPaymentFailure(request);

		rollbackResult.forEach(model::addAttribute);
		model.addAttribute("message", message != null ? message : "결제가 취소되었습니다.");
		model.addAttribute("code", code);
		model.addAttribute("orderId", orderId);
		model.addAttribute("paymentKey", paymentKey);
		model.addAttribute("amount", amount);
		model.addAttribute("status", "CANCELED");

		return "payment/cancel";
	}

	@Operation(summary = "결제 실패 콜백", description = "토스페이먼츠 결제 실패 콜백을 처리합니다.")
	@GetMapping("/fail")
	public String paymentFail(@RequestParam(required = false) String message,
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String orderId,
		Model model) {

		PaymentCallbackRequest request = PaymentCallbackRequest.builder()
			.orderId(orderId)
			.message(message)
			.code(code)
			.build();

		Map<String, Object> rollbackResult = paymentProcessingService.processPaymentFailure(request);

		rollbackResult.forEach(model::addAttribute);
		model.addAttribute("message", message != null ? message : "알 수 없는 오류가 발생했습니다.");
		model.addAttribute("code", code);
		model.addAttribute("orderId", orderId);
		model.addAttribute("status", "FAILED");

		return "payment/fail";
	}
}
