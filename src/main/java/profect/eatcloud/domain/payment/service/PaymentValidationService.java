package profect.eatcloud.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import profect.eatcloud.domain.payment.entity.PaymentRequest;
import profect.eatcloud.domain.payment.repository.PaymentRequestRepository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Service
public class PaymentValidationService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Integer MAX_AMOUNT = 100_000_000;

    public PaymentValidationService(PaymentRequestRepository paymentRequestRepository, PaymentService paymentService) {
        this.paymentRequestRepository = paymentRequestRepository;
        this.paymentService = paymentService;
    }

    public PaymentRequest savePaymentRequest(UUID orderId, String tossOrderId, Integer amount) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID는 필수입니다");
        }
        if (tossOrderId == null || tossOrderId.trim().isEmpty()) {
            throw new IllegalArgumentException("토스 주문 ID는 필수입니다");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다");
        }
        String requestPayload = String.format("{\"tossOrderId\":\"%s\",\"amount\":%d}", tossOrderId, amount);
        PaymentRequest paymentRequest = new PaymentRequest(orderId, "TOSS", requestPayload);
        PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);
        
        paymentService.schedulePaymentTimeout(savedRequest.getPaymentRequestId());
        
        return savedRequest;
    }

    public ValidationResult validateCallback(String tossOrderId, Integer callbackAmount, String paymentKey) {

        if (tossOrderId == null || callbackAmount == null) {
            return ValidationResult.fail("필수 정보가 누락되었습니다");
        }

        if (callbackAmount > MAX_AMOUNT) {
            return ValidationResult.fail("결제 한도 초과: 최대 " + MAX_AMOUNT + "원");
        }

        Optional<PaymentRequest> savedRequest = findByTossOrderId(tossOrderId);
        if (savedRequest.isEmpty()) {
            return ValidationResult.fail("저장된 결제 요청을 찾을 수 없습니다: " + tossOrderId);
        }

        PaymentRequest request = savedRequest.get();

        if (!"PENDING".equals(request.getStatus())) {
            return ValidationResult.fail("이미 처리된 결제입니다. 상태: " + request.getStatus());
        }

        try {
            Integer savedAmount = extractAmount(request.getRequestPayload());
            if (!savedAmount.equals(callbackAmount)) {
                return ValidationResult.fail(String.format("결제 금액이 일치하지 않습니다. 저장된 금액: %d, 콜백 금액: %d",
                        savedAmount, callbackAmount));
            }
        } catch (Exception e) {
            return ValidationResult.fail("결제 정보 파싱 오류: " + e.getMessage());
        }

        return ValidationResult.success(request);
    }

    public Optional<PaymentRequest> findByTossOrderId(String tossOrderId) {
        try {
            List<PaymentRequest> requests = paymentRequestRepository.findAll();

            return requests.stream()
                    .filter(req -> {
                        try {
                            if (req.getRequestPayload() == null) {
                                return false;
                            }

                            JsonNode jsonNode = objectMapper.readTree(req.getRequestPayload());
                            if (jsonNode.has("tossOrderId")) {
                                String savedTossOrderId = jsonNode.get("tossOrderId").asText();
                                return tossOrderId.equals(savedTossOrderId);
                            }
                            return false;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Integer extractAmount(String payload) throws Exception {
        if (payload == null || payload.trim().isEmpty()) {
            throw new Exception("결제 정보가 비어있습니다");
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            if (!jsonNode.has("amount")) {
                throw new Exception("금액 정보가 없습니다");
            }
            return jsonNode.get("amount").asInt();
        } catch (Exception e) {
            throw new Exception("JSON 파싱 실패: " + e.getMessage());
        }
    }

    public void updatePaymentStatus(UUID paymentRequestId, String status) {
        if ("COMPLETED".equals(status)) {
            paymentService.updatePaymentRequestToPaid(paymentRequestId);
        } else {
            paymentRequestRepository.findById(paymentRequestId)
                    .ifPresent(request -> {
                        request.setStatus(status);
                        paymentRequestRepository.save(request);
                    });
        }
    }

    @Getter
    public static class ValidationResult {
        private final boolean success;
        private final String errorMessage;
        private final PaymentRequest paymentRequest;

        private ValidationResult(boolean success, String errorMessage, PaymentRequest paymentRequest) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.paymentRequest = paymentRequest;
        }

        public static ValidationResult success(PaymentRequest paymentRequest) {
            return new ValidationResult(true, null, paymentRequest);
        }

        public static ValidationResult fail(String errorMessage) {
            return new ValidationResult(false, errorMessage, null);
        }

    }
}