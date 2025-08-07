package profect.eatcloud.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토스페이먼츠 결제 승인 요청")
public class TossPaymentRequest {

    @JsonProperty("paymentKey")
    @Schema(
        description = "토스페이먼츠에서 발급한 결제 키",
        example = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6"
    )
    private String paymentKey;
    
    @JsonProperty("orderId") 
    @Schema(
        description = "주문 고유 식별자 (배달 주문번호)",
        example = "ORDER_20241201_001"
    )
    private String orderId;
    
    @JsonProperty("amount")
    @Schema(
        description = "결제 금액 (원 단위)",
        example = "25000"
    )
    private Integer amount;
}