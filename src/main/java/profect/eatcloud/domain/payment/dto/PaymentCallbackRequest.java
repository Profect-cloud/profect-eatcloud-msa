package profect.eatcloud.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCallbackRequest {
    private final String paymentKey;
    private final String orderId;
    private final Integer amount;
    private final String message;
    private final String code;
}
