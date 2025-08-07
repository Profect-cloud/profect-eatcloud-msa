package profect.eatcloud.domain.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import profect.eatcloud.domain.payment.dto.TossPaymentResponse;
import profect.eatcloud.domain.payment.dto.TossPaymentRequest;
import profect.eatcloud.domain.payment.exception.PaymentException;
import profect.eatcloud.domain.payment.exception.PaymentValidationException;

import java.util.Base64;

@Service
public class TossPaymentService {

    @Autowired
    @Qualifier("tossWebClient")
    private WebClient tossWebClient;
    
    @Value("${toss.secret-key}")
    private String secretKey;

    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Integer amount) {
        validatePaymentRequest(paymentKey, orderId, amount);
        
        String encodedAuth = Base64.getEncoder()
            .encodeToString((secretKey + ":").getBytes());
        
        TossPaymentRequest request = new TossPaymentRequest(paymentKey, orderId, amount);
        
        try {
            TossPaymentResponse response = tossWebClient
                .post()
                .uri("/payments/confirm")
                .header("Authorization", "Basic " + encodedAuth)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TossPaymentResponse.class)
                .block();
            return response;
            
        } catch (Exception e) {
            throw new PaymentException("결제 승인 중 오류가 발생했습니다: " + e.getMessage(), "PAYMENT_CONFIRM_ERROR", e);
        }
    }

    private void validatePaymentRequest(String paymentKey, String orderId, Integer amount) {
        if (paymentKey == null || paymentKey.trim().isEmpty()) {
            throw new PaymentValidationException("paymentKey", "결제 키는 필수입니다.");
        }
        
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new PaymentValidationException("orderId", "주문 ID는 필수입니다.");
        }
        
        if (amount == null || amount <= 0) {
            throw new PaymentValidationException("amount", "결제 금액은 0보다 커야 합니다.");
        }
    }
}