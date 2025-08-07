package profect.eatcloud.domain.payment.util;

import profect.eatcloud.domain.payment.dto.CheckoutResponse;
import profect.eatcloud.domain.payment.dto.PointChargeResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Payment DTO를 Map으로 변환하는 유틸리티 클래스
 */
public class PaymentDtoConverter {

    private PaymentDtoConverter() {
        // 유틸리티 클래스이므로 인스턴스 생성 방지
    }

    /**
     * CheckoutResponse를 Map으로 변환
     */
    public static Map<String, Object> toMap(CheckoutResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", response.getOrderId());
        map.put("internalOrderId", response.getInternalOrderId());
        map.put("orderNumber", response.getOrderNumber());
        map.put("amount", response.getAmount());
        map.put("userId", response.getUserId());
        map.put("clientKey", response.getClientKey());
        map.put("usePoints", response.getUsePoints());
        map.put("pointsUsed", response.getPointsUsed());
        map.put("originalAmount", response.getOriginalAmount());
        map.put("orderItems", response.getOrderItems());
        map.put("customerName", response.getCustomerName());
        map.put("message", response.getMessage());
        return map;
    }

    /**
     * PointChargeResponse를 Map으로 변환
     */
    public static Map<String, Object> toMap(PointChargeResponse response) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", response.getUserId());
        map.put("clientKey", response.getClientKey());
        map.put("amount", response.getAmount());
        map.put("orderId", response.getOrderId());
        map.put("authenticated", response.getAuthenticated());
        map.put("message", response.getMessage());
        return map;
    }

    /**
     * 에러 응답 Map 생성
     */
    public static Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", errorMessage);
        return map;
    }
}
