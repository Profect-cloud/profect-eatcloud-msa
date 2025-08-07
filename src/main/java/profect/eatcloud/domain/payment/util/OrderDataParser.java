package profect.eatcloud.domain.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import profect.eatcloud.domain.order.dto.OrderMenu;
import profect.eatcloud.domain.payment.dto.CheckoutRequest;

import java.util.*;

/**
 * 주문 데이터 파싱을 담당하는 유틸리티 클래스
 */
public class OrderDataParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_STORE_ID = "550e8400-e29b-41d4-a716-446655440000";

    private OrderDataParser() {
        // 유틸리티 클래스이므로 인스턴스 생성 방지
    }

    /**
     * JSON 문자열을 CheckoutRequest로 파싱
     */
    public static CheckoutRequest parseCheckoutRequest(String orderDataJson) {
        try {
            Map<String, Object> orderData = objectMapper.readValue(orderDataJson, Map.class);
            
            String customerIdFromForm = (String) orderData.get("customerId");
            Integer totalAmount = (Integer) orderData.get("totalPrice");
            Boolean usePoints = (Boolean) orderData.getOrDefault("usePoints", false);
            Integer pointsToUse = (Integer) orderData.getOrDefault("pointsToUse", 0);
            Integer finalPaymentAmount = (Integer) orderData.getOrDefault("finalPaymentAmount", totalAmount);
            String orderTypeInput = (String) orderData.getOrDefault("orderType", "배달");
            
            UUID storeId = orderData.get("storeId") != null ? 
                UUID.fromString((String) orderData.get("storeId")) :
                UUID.fromString(DEFAULT_STORE_ID);
            
            List<OrderMenu> orderMenuList = parseOrderMenuList(orderData);
            
            return CheckoutRequest.builder()
                .customerId(customerIdFromForm)
                .totalPrice(totalAmount)
                .usePoints(usePoints)
                .pointsToUse(pointsToUse)
                .finalPaymentAmount(finalPaymentAmount)
                .orderType(orderTypeInput)
                .storeId(storeId)
                .orderMenuList(orderMenuList)
                .build();
                
        } catch (Exception e) {
            throw new RuntimeException("주문 데이터 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 주문 타입 디스플레이명을 코드로 변환
     */
    public static String convertOrderTypeToCode(String displayName) {
        return switch (displayName) {
            case "배달" -> "DELIVERY";
            case "픽업" -> "PICKUP";
            case "매장 식사" -> "DINE_IN";
            default -> displayName;
        };
    }
    
    /**
     * Toss용 주문 ID 생성
     */
    public static String generateTossOrderId(UUID orderId) {
        return "TOSS_" + orderId.toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    // === Private Helper Methods ===
    
    @SuppressWarnings("unchecked")
    private static List<OrderMenu> parseOrderMenuList(Map<String, Object> orderData) {
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) orderData.get("orderMenuList");
        List<OrderMenu> orderMenuList = new ArrayList<>();
        
        if (itemsData != null) {
            for (Map<String, Object> item : itemsData) {
                OrderMenu orderMenu = OrderMenu.builder()
                    .menuId(UUID.fromString((String) item.get("menuId")))
                    .menuName((String) item.get("menuName"))
                    .price((Integer) item.get("price"))
                    .quantity((Integer) item.get("quantity"))
                    .build();
                orderMenuList.add(orderMenu);
            }
        }
        
        return orderMenuList;
    }
}
