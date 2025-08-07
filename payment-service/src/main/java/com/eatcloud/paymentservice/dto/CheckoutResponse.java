package com.eatcloud.paymentservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckoutResponse {
    private final String orderId;
    private final String internalOrderId;
    private final String orderNumber;
    private final Integer amount;
    private final String userId;
    private final String clientKey;
    private final Boolean usePoints;
    private final Integer pointsUsed;
    private final Integer originalAmount;
    private final Object orderItems;
    private final String customerName;
    private final String message;
}
