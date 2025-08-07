package profect.eatcloud.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointChargeRequest {
    private final String userId;
    private final Integer amount;
    
    public boolean isValidAmount() {
        return amount != null && amount > 0;
    }
}
