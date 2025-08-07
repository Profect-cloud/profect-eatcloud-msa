package profect.eatcloud.domain.payment.service;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.service.OrderService;
import profect.eatcloud.domain.payment.entity.PaymentRequest;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PaymentRollbackService {
    
    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final PointService pointService;
    private final PaymentValidationService paymentValidationService;

    public RollbackResult rollbackPayment(PaymentRequest paymentRequest, String status) {
        try {
            log.info("결제 롤백 시작 - PaymentRequestId: {}, Status: {}", 
                    paymentRequest.getPaymentRequestId(), status);
            
            Order order = orderService.findById(paymentRequest.getOrderId())
                    .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다: " + paymentRequest.getOrderId()));
            
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("고객 정보를 찾을 수 없습니다: " + order.getCustomerId()));
            
            paymentValidationService.updatePaymentStatus(paymentRequest.getPaymentRequestId(), status);
            log.info("결제 요청 상태 업데이트 완료 - Status: {}", status);
            
            orderService.cancelOrder(paymentRequest.getOrderId());
            log.info("주문 취소 완료 - OrderId: {}", paymentRequest.getOrderId());
            
            if (order.getUsePoints() != null && order.getUsePoints() &&
                order.getPointsToUse() != null && order.getPointsToUse() > 0) {
                
                var pointResult = pointService.refundPoints(customer.getId(), order.getPointsToUse());
                
                if (pointResult.isSuccess()) {
                    log.info("포인트 롤백 완료 - CustomerId: {}, RefundedPoints: {}", 
                            customer.getId(), order.getPointsToUse());
                } else {
                    log.error("포인트 롤백 실패 - CustomerId: {}, Points: {}, Error: {}", 
                            customer.getId(), order.getPointsToUse(), pointResult.getErrorMessage());
                }
            } else {
                log.info("사용된 포인트가 없어 포인트 롤백 생략");
            }
            
            return RollbackResult.success(paymentRequest.getOrderId(), order.getPointsToUse());
            
        } catch (Exception e) {
            log.error("결제 롤백 처리 실패 - PaymentRequestId: {}, Error: {}", 
                    paymentRequest.getPaymentRequestId(), e.getMessage(), e);
            return RollbackResult.failure(e.getMessage());
        }
    }

    public RollbackResult rollbackByOrderId(UUID orderId, String status) {
        try {
            log.info("주문 ID로 롤백 시작 - OrderId: {}, Status: {}", orderId, status);
            
            Order order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다: " + orderId));
            
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("고객 정보를 찾을 수 없습니다: " + order.getCustomerId()));
            
            orderService.cancelOrder(orderId);
            log.info("주문 취소 완료 - OrderId: {}", orderId);
            
            if (order.getUsePoints() != null && order.getUsePoints() &&
                order.getPointsToUse() != null && order.getPointsToUse() > 0) {
                
                var pointResult = pointService.refundPoints(customer.getId(), order.getPointsToUse());
                
                if (pointResult.isSuccess()) {
                    log.info("포인트 롤백 완료 - CustomerId: {}, RefundedPoints: {}", 
                            customer.getId(), order.getPointsToUse());
                } else {
                    log.error("포인트 롤백 실패 - CustomerId: {}, Points: {}, Error: {}", 
                            customer.getId(), order.getPointsToUse(), pointResult.getErrorMessage());
                }
            } else {
                log.info("사용된 포인트가 없어 포인트 롤백 생략");
            }
            
            return RollbackResult.success(orderId, order.getPointsToUse());
            
        } catch (Exception e) {
            log.error("주문 롤백 처리 실패 - OrderId: {}, Error: {}", orderId, e.getMessage(), e);
            return RollbackResult.failure(e.getMessage());
        }
    }

    public static class RollbackResult {
        @Getter
        private final boolean success;
        @Getter
        private final UUID orderId;
        private final Integer refundedPoints;
        @Getter
        private final String errorMessage;
        
        private RollbackResult(boolean success, UUID orderId, Integer refundedPoints, String errorMessage) {
            this.success = success;
            this.orderId = orderId;
            this.refundedPoints = refundedPoints;
            this.errorMessage = errorMessage;
        }
        
        public static RollbackResult success(UUID orderId, Integer refundedPoints) {
            return new RollbackResult(true, orderId, refundedPoints, null);
        }
        
        public static RollbackResult failure(String errorMessage) {
            return new RollbackResult(false, null, null, errorMessage);
        }

        public Integer getRefundedPoints() { return refundedPoints != null ? refundedPoints : 0; }
        public boolean hasRefundedPoints() {
            return refundedPoints != null && refundedPoints > 0;
        }
    }
}
