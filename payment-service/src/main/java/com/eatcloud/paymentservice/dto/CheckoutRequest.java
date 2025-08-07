package com.eatcloud.paymentservice.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import profect.eatcloud.domain.order.dto.OrderMenu;

@Getter
@Builder
public class CheckoutRequest {
    private final String customerId;
    private final Integer totalPrice;
    private final Boolean usePoints;
    private final Integer pointsToUse;
    private final Integer finalPaymentAmount;
    private final String orderType;
    private final UUID storeId;
    private final List<OrderMenu> orderMenuList;
    
    public boolean shouldUsePoints() {
        return usePoints != null && usePoints && pointsToUse != null && pointsToUse > 0;
    }
    
    public int getPointsToUseOrZero() {
        return pointsToUse != null ? pointsToUse : 0;
    }
    
    public int getFinalPaymentAmountOrTotal() {
        return finalPaymentAmount != null ? finalPaymentAmount : totalPrice;
    }
}
