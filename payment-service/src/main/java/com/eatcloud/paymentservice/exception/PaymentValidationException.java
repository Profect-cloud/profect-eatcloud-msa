package com.eatcloud.paymentservice.exception;

public class PaymentValidationException extends PaymentException {
    public PaymentValidationException(String field, String reason) {
        super("결제 검증 실패: " + field + " - " + reason, "PAYMENT_VALIDATION_ERROR");
    }
} 