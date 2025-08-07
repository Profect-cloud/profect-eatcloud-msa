package profect.eatcloud.domain.payment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import profect.eatcloud.domain.payment.entity.PaymentRequest;
import profect.eatcloud.domain.payment.repository.PaymentRequestRepository;
import profect.eatcloud.domain.payment.service.PaymentValidationService.ValidationResult;

@ExtendWith(MockitoExtension.class)
class PaymentValidationServiceTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentValidationService paymentValidationService;

    @DisplayName("결제 요청 저장 성공")
    @Test
    void givenPaymentInfo_whenSavePaymentRequest_thenReturnSavedRequest() {
        UUID orderId = UUID.randomUUID();
        String tossOrderId = "ORDER_123456";
        Integer amount = 10000;

        PaymentRequest savedRequest = new PaymentRequest(orderId, "TOSS", "{\"tossOrderId\":\"ORDER_123456\",\"amount\":10000}");
        UUID paymentRequestId = UUID.randomUUID();
        savedRequest.setPaymentRequestId(paymentRequestId);
        
        given(paymentRequestRepository.save(any(PaymentRequest.class)))
                .willReturn(savedRequest);
        given(paymentService.schedulePaymentTimeout(paymentRequestId))
                .willReturn(CompletableFuture.completedFuture(null));

        PaymentRequest result = paymentValidationService.savePaymentRequest(orderId, tossOrderId, amount);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getPgProvider()).isEqualTo("TOSS");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        then(paymentRequestRepository).should().save(any(PaymentRequest.class));
        then(paymentService).should().schedulePaymentTimeout(paymentRequestId);
    }

    @DisplayName("중복 결제 방지 - 이미 승인된 결제")
    @Test
    void givenConfirmedPayment_whenValidateCallback_thenReturnFailure() {
        String tossOrderId = "ORDER_ALREADY_CONFIRMED";
        Integer amount = 15000;
        String paymentKey = "payment_key_123";

        PaymentRequest confirmedRequest = new PaymentRequest(UUID.randomUUID(), "TOSS", "{\"tossOrderId\":\"ORDER_ALREADY_CONFIRMED\",\"amount\":15000}");
        confirmedRequest.setStatus("CONFIRMED");

        given(paymentRequestRepository.findAll())
                .willReturn(Arrays.asList(confirmedRequest));

        ValidationResult result = paymentValidationService.validateCallback(tossOrderId, amount, paymentKey);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("이미 처리된 결제입니다");
    }

    @DisplayName("금액 불일치 검증 실패")
    @Test
    void givenAmountMismatch_whenValidateCallback_thenReturnFailure() {
        String tossOrderId = "ORDER_AMOUNT_MISMATCH";
        Integer savedAmount = 10000;
        Integer callbackAmount = 15000;
        String paymentKey = "payment_key_123";

        PaymentRequest savedRequest = new PaymentRequest(UUID.randomUUID(), "TOSS", "{\"tossOrderId\":\"ORDER_AMOUNT_MISMATCH\",\"amount\":10000}");
        savedRequest.setStatus("PENDING");

        given(paymentRequestRepository.findAll())
                .willReturn(Arrays.asList(savedRequest));

        ValidationResult result = paymentValidationService.validateCallback(tossOrderId, callbackAmount, paymentKey);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("결제 금액이 일치하지 않습니다");
        assertThat(result.getErrorMessage()).contains("저장된 금액: 10000");
        assertThat(result.getErrorMessage()).contains("콜백 금액: 15000");
    }

    @DisplayName("존재하지 않는 주문 검증 실패")
    @Test
    void givenNonExistentOrder_whenValidateCallback_thenReturnFailure() {
        String nonExistentOrderId = "ORDER_NOT_FOUND";
        Integer amount = 20000;
        String paymentKey = "payment_key_not_found";

        given(paymentRequestRepository.findAll())
                .willReturn(Arrays.asList());

        ValidationResult result = paymentValidationService.validateCallback(nonExistentOrderId, amount, paymentKey);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("저장된 결제 요청을 찾을 수 없습니다");
        assertThat(result.getErrorMessage()).contains("ORDER_NOT_FOUND");
    }

    @DisplayName("정상 검증 성공")
    @Test
    void givenValidCallback_whenValidateCallback_thenReturnSuccess() {
        String tossOrderId = "ORDER_SUCCESS";
        Integer amount = 25000;
        String paymentKey = "payment_key_success";

        PaymentRequest savedRequest = new PaymentRequest(UUID.randomUUID(), "TOSS", "{\"tossOrderId\":\"ORDER_SUCCESS\",\"amount\":25000}");
        savedRequest.setStatus("PENDING");

        given(paymentRequestRepository.findAll())
                .willReturn(Arrays.asList(savedRequest));

        ValidationResult result = paymentValidationService.validateCallback(tossOrderId, amount, paymentKey);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPaymentRequest()).isNotNull();
        assertThat(result.getPaymentRequest().getStatus()).isEqualTo("PENDING");
    }

    @DisplayName("해킹 시도 차단 - 금액 조작 감지")
    @Test
    void givenHackedAmount_whenValidateCallback_thenBlockPayment() {
        String tossOrderId = "ORDER_HACK_ATTEMPT";
        Integer originalAmount = 100000;
        Integer hackedAmount = 1000;
        String paymentKey = "payment_key_hack";

        PaymentRequest originalRequest = new PaymentRequest(UUID.randomUUID(), "TOSS", "{\"tossOrderId\":\"ORDER_HACK_ATTEMPT\",\"amount\":100000}");
        originalRequest.setStatus("PENDING");

        given(paymentRequestRepository.findAll())
                .willReturn(Arrays.asList(originalRequest));

        ValidationResult result = paymentValidationService.validateCallback(tossOrderId, hackedAmount, paymentKey);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("결제 금액이 일치하지 않습니다");
    }

    @DisplayName("결제 한도 초과 검증")
    @Test
    void givenOverLimitAmount_whenValidateCallback_thenReturnFailure() {
        String tossOrderId = "ORDER_123";
        Integer overLimitAmount = 100_000_001;
        String paymentKey = "payment_key";

        ValidationResult result = paymentValidationService.validateCallback(tossOrderId, overLimitAmount, paymentKey);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("결제 한도 초과");
    }

    @DisplayName("Null 파라미터 예외 처리")
    @Test
    void givenNullParameters_whenSavePaymentRequest_thenThrowException() {
        assertThatThrownBy(() -> {
            paymentValidationService.savePaymentRequest(null, null, null);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("결제 상태 업데이트 성공")
    @Test
    void givenPaymentRequestId_whenUpdatePaymentStatus_thenUpdateStatus() {
        UUID paymentRequestId = UUID.randomUUID();
        PaymentRequest paymentRequest = new PaymentRequest(UUID.randomUUID(), "TOSS", "{\"amount\":10000}");
        paymentRequest.setStatus("PENDING");

        given(paymentRequestRepository.findById(paymentRequestId))
                .willReturn(Optional.of(paymentRequest));
        given(paymentRequestRepository.save(any(PaymentRequest.class)))
                .willReturn(paymentRequest);

        paymentValidationService.updatePaymentStatus(paymentRequestId, "CONFIRMED");

        assertThat(paymentRequest.getStatus()).isEqualTo("CONFIRMED");
        then(paymentRequestRepository).should().save(paymentRequest);
    }

    @DisplayName("존재하지 않는 결제 요청 상태 업데이트")
    @Test
    void givenNonExistentPaymentRequestId_whenUpdatePaymentStatus_thenDoNothing() {
        UUID nonExistentPaymentRequestId = UUID.randomUUID();

        given(paymentRequestRepository.findById(nonExistentPaymentRequestId))
                .willReturn(Optional.empty());

        paymentValidationService.updatePaymentStatus(nonExistentPaymentRequestId, "CONFIRMED");

        then(paymentRequestRepository).should(never()).save(any(PaymentRequest.class));
    }
}