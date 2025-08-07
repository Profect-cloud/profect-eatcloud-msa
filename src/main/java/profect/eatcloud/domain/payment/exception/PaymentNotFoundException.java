package profect.eatcloud.domain.payment.exception;

public class PaymentNotFoundException extends PaymentException {
    
    public PaymentNotFoundException(String message) {
        super(message, "PAYMENT_NOT_FOUND");
    }
    
    public PaymentNotFoundException(String orderId, boolean isOrderId) {
        super("주문 ID '" + orderId + "'에 해당하는 결제 정보를 찾을 수 없습니다.", "PAYMENT_NOT_FOUND");
    }
} 