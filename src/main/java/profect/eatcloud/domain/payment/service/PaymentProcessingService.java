package profect.eatcloud.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import profect.eatcloud.domain.order.dto.OrderMenu;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.service.OrderService;
import profect.eatcloud.domain.payment.dto.*;
import profect.eatcloud.domain.payment.entity.Payment;
import profect.eatcloud.domain.payment.entity.PaymentRequest;
import profect.eatcloud.domain.payment.util.OrderDataParser;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentProcessingService {

    private final PaymentAuthenticationService paymentAuthenticationService;
    private final PaymentValidationService paymentValidationService;
    private final PaymentRollbackService paymentRollbackService;
    private final TossPaymentService tossPaymentService;
    private final PaymentService paymentService;
    private final PointService pointService;
    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    @Value("${toss.client-key}")
    private String clientKey;

    public CheckoutResponse processCheckout(String orderDataJson) {
        try {
            CheckoutRequest request = parseCheckoutRequest(orderDataJson);

            var authResult = paymentAuthenticationService.validateCustomerForPayment(request.getCustomerId());
            if (!authResult.isSuccess()) {
                throw new RuntimeException(authResult.getErrorMessage());
            }

            Order createdOrder = orderService.createPendingOrder(
                authResult.getCustomerId(),
                request.getStoreId(),
                request.getOrderMenuList(),
                convertOrderTypeToCode(request.getOrderType()),
                request.getUsePoints(),
                request.getPointsToUseOrZero()
            );

            if (request.shouldUsePoints()) {
                var pointResult = pointService.usePoints(authResult.getCustomerId(), request.getPointsToUseOrZero());
                if (!pointResult.isSuccess()) {
                    orderService.cancelOrder(createdOrder.getOrderId());
                    throw new RuntimeException(pointResult.getErrorMessage());
                }
            }
            
            String tossOrderId = generateTossOrderId(createdOrder.getOrderId());
            int finalAmount = request.getFinalPaymentAmountOrTotal();
            
            if (finalAmount > 0) {
                paymentValidationService.savePaymentRequest(createdOrder.getOrderId(), tossOrderId, finalAmount);
            }

            return CheckoutResponse.builder()
                .orderId(tossOrderId)
                .internalOrderId(createdOrder.getOrderId().toString())
                .orderNumber(createdOrder.getOrderNumber())
                .amount(finalAmount)
                .userId(authResult.getCustomerId().toString())
                .clientKey(clientKey)
                .usePoints(request.getUsePoints())
                .pointsUsed(request.getPointsToUseOrZero())
                .originalAmount(request.getTotalPrice())
                .customerName(authResult.getCustomerName())
                .message("주문 생성 및 결제 페이지 데이터 생성 성공")
                .build();
                
        } catch (Exception e) {
            log.error("체크아웃 처리 중 오류 발생", e);
            throw new RuntimeException("주문 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public PointChargeResponse processPointCharge(PointChargeRequest request) {
        var authResult = paymentAuthenticationService.validateCustomerForPointCharge();
        
        String userId = request.getUserId();
        if (authResult.isSuccess()) {
            userId = authResult.getCustomerIdAsString();
        } else if (userId == null) {
            throw new RuntimeException("포인트 충전을 위해서는 로그인이 필요합니다.");
        }
        
        if (!request.isValidAmount()) {
            throw new RuntimeException("충전 금액은 0보다 커야 합니다.");
        }
        
        String orderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 12);
        
        return PointChargeResponse.builder()
            .userId(userId)
            .clientKey(clientKey)
            .amount(request.getAmount())
            .orderId(orderId)
            .authenticated(authResult.isSuccess())
            .message(authResult.isSuccess() ? 
                    "포인트 충전 페이지 데이터 생성 성공" : 
                    authResult.getErrorMessage())
            .build();
    }


    public void processPaymentSuccess(PaymentCallbackRequest request) {
        var validationResult = paymentValidationService.validateCallback(
            request.getOrderId(), request.getAmount(), request.getPaymentKey());
        
        if (!validationResult.isSuccess()) {
            throw new RuntimeException(validationResult.getErrorMessage());
        }
        
        try {
            var tossResponse = tossPaymentService.confirmPayment(
                request.getPaymentKey(), request.getOrderId(), request.getAmount());

            PaymentRequest paymentRequest = validationResult.getPaymentRequest();
            UUID internalOrderId = paymentRequest.getOrderId();
            
            Order order = orderService.findById(internalOrderId)
                .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));
            
            Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("고객 정보를 찾을 수 없습니다."));
            
            Payment savedPayment = paymentService.saveSuccessfulPayment(paymentRequest, customer, tossResponse);
            
            orderService.completePayment(internalOrderId, savedPayment.getPaymentId());
            
            paymentValidationService.updatePaymentStatus(paymentRequest.getPaymentRequestId(), "COMPLETED");
            
        } catch (Exception e) {
            handlePaymentFailureRollback(request);
            throw new RuntimeException("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> processPaymentFailure(PaymentCallbackRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        if (request.getOrderId() != null) {
            try {
                Optional<PaymentRequest> savedRequest = paymentValidationService.findByTossOrderId(request.getOrderId());
                
                if (savedRequest.isPresent()) {
                    PaymentRequest paymentRequest = savedRequest.get();
                    var rollbackResult = paymentRollbackService.rollbackPayment(paymentRequest, "CANCELED");
                    
                    result.put("rollbackCompleted", rollbackResult.isSuccess());
                    result.put("internalOrderId", rollbackResult.getOrderId().toString());
                    
                    if (rollbackResult.isSuccess()) {
                        result.put("refundedPoints", rollbackResult.getRefundedPoints());
                        if (rollbackResult.hasRefundedPoints()) {
                            result.put("pointRefundMessage", 
                                rollbackResult.getRefundedPoints() + "P가 환불되었습니다.");
                        }
                    } else {
                        result.put("rollbackError", rollbackResult.getErrorMessage());
                    }
                } else {
                    result.put("rollbackCompleted", false);
                    result.put("rollbackError", "결제 요청을 찾을 수 없습니다.");
                }
            } catch (Exception e) {
                log.error("결제 실패 롤백 처리 실패", e);
                result.put("rollbackCompleted", false);
                result.put("rollbackError", e.getMessage());
            }
        }
        
        return result;
    }
    
    private CheckoutRequest parseCheckoutRequest(String orderDataJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> orderData = objectMapper.readValue(orderDataJson, Map.class);
            
            String customerIdFromForm = (String) orderData.get("customerId");
            Integer totalAmount = (Integer) orderData.get("totalPrice");
            Boolean usePoints = (Boolean) orderData.getOrDefault("usePoints", false);
            Integer pointsToUse = (Integer) orderData.getOrDefault("pointsToUse", 0);
            Integer finalPaymentAmount = (Integer) orderData.getOrDefault("finalPaymentAmount", totalAmount);
            String orderTypeInput = (String) orderData.getOrDefault("orderType", "배달");
            
            UUID storeId = orderData.get("storeId") != null ? 
                UUID.fromString((String) orderData.get("storeId")) :
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            
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
    
    @SuppressWarnings("unchecked")
    private List<OrderMenu> parseOrderMenuList(Map<String, Object> orderData) {
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
    
    private String generateTossOrderId(UUID orderId) {
        return "TOSS_" + orderId.toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    private String convertOrderTypeToCode(String displayName) {
        return switch (displayName) {
            case "배달" -> "DELIVERY";
            case "픽업" -> "PICKUP";
            case "매장 식사" -> "DINE_IN";
            default -> displayName;
        };
    }
    
    private void handlePaymentFailureRollback(PaymentCallbackRequest request) {
        try {
            var validationResult = paymentValidationService.validateCallback(
                request.getOrderId(), request.getAmount(), request.getPaymentKey());
            
            if (validationResult.isSuccess()) {
                PaymentRequest paymentRequest = validationResult.getPaymentRequest();
                paymentRollbackService.rollbackPayment(paymentRequest, "FAILED");
            }
        } catch (Exception rollbackException) {
            log.error("결제 성공 처리 중 롤백 실패", rollbackException);
        }
    }
}
