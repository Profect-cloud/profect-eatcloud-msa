package profect.eatcloud.domain.payment.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import profect.eatcloud.domain.payment.entity.Payment;
import profect.eatcloud.domain.payment.entity.PaymentRequest;
import profect.eatcloud.domain.payment.repository.PaymentRepository;
import profect.eatcloud.domain.payment.repository.PaymentRequestRepository;
import profect.eatcloud.domain.payment.dto.TossPaymentResponse;
import profect.eatcloud.domain.globalCategory.entity.PaymentStatusCode;
import profect.eatcloud.domain.globalCategory.entity.PaymentMethodCode;
import profect.eatcloud.domain.globalCategory.repository.PaymentStatusCodeRepository;
import profect.eatcloud.domain.globalCategory.repository.PaymentMethodCodeRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final CustomerRepository customerRepository;
    private final PaymentStatusCodeRepository paymentStatusCodeRepository;
    private final PaymentMethodCodeRepository paymentMethodCodeRepository;
    private static final long PAYMENT_TIMEOUT_MS = 5 * 60 * 1000;
    private static final long TEST_PAYMENT_TIMEOUT_MS = 10 * 1000;

    @Transactional
    public Payment saveSuccessfulPayment(PaymentRequest paymentRequest, Customer customer, TossPaymentResponse tossResponse) {
        Timestamp approvedTime;
        try {
            if (tossResponse.getApprovedAt() != null) {
                String approvedAtStr = tossResponse.getApprovedAt();
                if (approvedAtStr.contains("+") || approvedAtStr.contains("Z")) {
                    approvedAtStr = approvedAtStr.substring(0, 19);
                }
                approvedTime = Timestamp.valueOf(LocalDateTime.parse(approvedAtStr));
            } else {
                approvedTime = Timestamp.valueOf(LocalDateTime.now());
            }
        } catch (Exception e) {
            approvedTime = Timestamp.valueOf(LocalDateTime.now());
        }

        PaymentStatusCode paidStatus = paymentStatusCodeRepository.findByCode("PAID")
                .orElseThrow(() -> new RuntimeException("결제 상태 코드를 찾을 수 없습니다: PAID"));

        String methodCode = mapTossMethodToCode(tossResponse.getMethod());
        PaymentMethodCode paymentMethod = paymentMethodCodeRepository.findByCode(methodCode)
                .orElse(paymentMethodCodeRepository.findByCode("CARD")
                        .orElseThrow(() -> new RuntimeException("기본 결제 방법 코드를 찾을 수 없습니다: CARD")));

        Payment payment = Payment.builder()
                .totalAmount(tossResponse.getTotalAmount())
                .pgTransactionId(tossResponse.getPaymentKey())
                .approvalCode(tossResponse.getOrderId())
                .paymentRequest(paymentRequest)
                .customer(customer)
                .paymentStatusCode(paidStatus)
                .paymentMethodCode(paymentMethod)
                .approvedAt(approvedTime)
                .requestedAt(Timestamp.valueOf(paymentRequest.getRequestedAt()))
                .build();

        return paymentRepository.save(payment);
    }

    private String mapTossMethodToCode(String tossMethod) {
        if (tossMethod == null) return "CARD";
        
        return switch (tossMethod) {
            case "카드" -> "CARD";
            case "가상계좌" -> "VIRTUAL_ACCOUNT";
            case "계좌이체" -> "TRANSFER";
            case "휴대폰" -> "PHONE";
            case "상품권", "도서문화상품권", "게임문화상품권" -> "GIFT_CERTIFICATE";
            default -> "CARD";
        };
    }

    @Async
    public CompletableFuture<Void> schedulePaymentTimeout(UUID paymentRequestId) {
        return schedulePaymentTimeout(paymentRequestId, false);
    }

    @Async
    public CompletableFuture<Void> schedulePaymentTimeout(UUID paymentRequestId, boolean testMode) {
        return CompletableFuture.runAsync(() -> {
            try {
                long timeoutMs = testMode ? TEST_PAYMENT_TIMEOUT_MS : PAYMENT_TIMEOUT_MS;
                Thread.sleep(timeoutMs);
                updateExpiredPaymentRequest(paymentRequestId);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Payment timeout task interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error in payment timeout task: " + e.getMessage());
            }
        });
    }

    @Transactional
    public void updateExpiredPaymentRequest(UUID paymentRequestId) {
        paymentRequestRepository.findById(paymentRequestId)
                .ifPresent(paymentRequest -> {
                    if ("PENDING".equals(paymentRequest.getStatus())) {
                        paymentRequest.setStatus("CANCELED");
                        paymentRequest.setRespondedAt(LocalDateTime.now());
                        paymentRequest.setFailureReason("Payment timeout - No response within 5 minutes");
                        paymentRequestRepository.save(paymentRequest);
                        
                        System.out.println("Payment request " + paymentRequestId + " expired and set to CANCELED");
                    }
                });
    }

    @Transactional
    public void updatePaymentRequestToPaid(UUID paymentRequestId) {
        paymentRequestRepository.findById(paymentRequestId)
                .ifPresent(paymentRequest -> {
                    paymentRequest.setStatus("PAID");
                    paymentRequest.setRespondedAt(LocalDateTime.now());
                    paymentRequestRepository.save(paymentRequest);
                });
    }
}