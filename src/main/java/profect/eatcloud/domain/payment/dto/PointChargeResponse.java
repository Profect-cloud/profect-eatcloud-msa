package profect.eatcloud.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointChargeResponse {
    private final String userId;
    private final String clientKey;
    private final Integer amount;
    private final String orderId;
    private final Boolean authenticated;
    private final String message;
}
