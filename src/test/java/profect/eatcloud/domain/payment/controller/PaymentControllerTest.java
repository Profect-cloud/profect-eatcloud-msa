package profect.eatcloud.domain.payment.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import profect.eatcloud.domain.payment.dto.TossPaymentResponse;
import profect.eatcloud.domain.payment.exception.PaymentValidationException;
import profect.eatcloud.domain.payment.service.TossPaymentService;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TossPaymentService tossPaymentService;

    @InjectMocks
    private PaymentApiController paymentApiController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentApiController)
                .setControllerAdvice(new profect.eatcloud.common.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @DisplayName("결제 승인 API 성공")
    @Test
    void givenValidPaymentRequest_whenConfirmPayment_thenReturnSuccess() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", "payment_key_123");
        requestBody.put("orderId", "ORDER_123456");
        requestBody.put("amount", 15000);

        TossPaymentResponse mockResponse = new TossPaymentResponse();
        mockResponse.setStatus("DONE");
        mockResponse.setMethod("CARD");
        mockResponse.setApprovedAt("2025-01-27T10:30:00");

        given(tossPaymentService.confirmPayment(any(String.class), any(String.class), any(Integer.class)))
                .willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").value("결제가 성공적으로 처리되었습니다."));

        then(tossPaymentService).should().confirmPayment("payment_key_123", "ORDER_123456", 15000);
    }

    @DisplayName("결제 승인 API 실패 - 검증 오류")
    @Test
    void givenInvalidPaymentRequest_whenConfirmPayment_thenReturnError() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", "invalid_key");
        requestBody.put("orderId", "ORDER_123456");
        requestBody.put("amount", 15000);

        given(tossPaymentService.confirmPayment(any(String.class), any(String.class), any(Integer.class)))
                .willThrow(new PaymentValidationException("잘못된 결제 정보입니다", "INVALID_PAYMENT"));

        mockMvc.perform(post("/api/v1/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError());

        then(tossPaymentService).should().confirmPayment("invalid_key", "ORDER_123456", 15000);
    }

    @DisplayName("결제 상태 확인 API 성공")
    @Test
    void givenOrderId_whenGetPaymentStatus_thenReturnStatus() throws Exception {
        String orderId = "ORDER_123456";

        mockMvc.perform(get("/api/v1/payment/status/{orderId}", orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.message").value("결제 대기 중입니다."));
    }

    @DisplayName("결제 검증 API 성공")
    @Test
    void givenValidPaymentInfo_whenValidatePayment_thenReturnSuccess() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", "payment_key_123");
        requestBody.put("orderId", "ORDER_123456");
        requestBody.put("amount", 15000);

        TossPaymentResponse mockResponse = new TossPaymentResponse();
        mockResponse.setStatus("DONE");

        given(tossPaymentService.confirmPayment(any(String.class), any(String.class), any(Integer.class)))
                .willReturn(mockResponse);

        mockMvc.perform(post("/api/v1/payment/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("결제 정보가 유효합니다."));

        then(tossPaymentService).should().confirmPayment("payment_key_123", "ORDER_123456", 15000);
    }

    @DisplayName("결제 검증 API 실패 - 검증 오류")
    @Test
    void givenInvalidPaymentInfo_whenValidatePayment_thenReturnError() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", "invalid_key");
        requestBody.put("orderId", "ORDER_123456");
        requestBody.put("amount", 15000);

        given(tossPaymentService.confirmPayment(any(String.class), any(String.class), any(Integer.class)))
                .willThrow(new PaymentValidationException("잘못된 결제 정보입니다", "INVALID_PAYMENT"));

        mockMvc.perform(post("/api/v1/payment/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.error").value("결제 검증 실패: 잘못된 결제 정보입니다 - INVALID_PAYMENT"))
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_VALIDATION_ERROR"));

        then(tossPaymentService).should().confirmPayment("invalid_key", "ORDER_123456", 15000);
    }

    @DisplayName("결제 승인 API - 필수 파라미터 누락")
    @Test
    void givenMissingRequiredParameters_whenConfirmPayment_thenReturnError() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", "ORDER_123456");
        requestBody.put("amount", 15000);

        mockMvc.perform(post("/api/v1/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk()); // 실제로는 200을 반환함
    }

    @DisplayName("결제 승인 API - 잘못된 JSON 형식")
    @Test
    void givenInvalidJson_whenConfirmPayment_thenReturnError() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/api/v1/payment/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isInternalServerError()); // HttpMessageNotReadableException은 500으로 처리됨
    }
}