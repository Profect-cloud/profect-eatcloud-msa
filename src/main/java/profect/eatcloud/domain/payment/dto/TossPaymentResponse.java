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
@Schema(description = "토스페이먼츠 결제 승인 응답")
public class TossPaymentResponse {
    
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
    
    @JsonProperty("status")
    @Schema(
        description = "결제 상태",
        example = "DONE",
        allowableValues = {"DONE", "CANCELED", "PARTIAL_CANCELED", "ABORTED", "WAITING_FOR_DEPOSIT"}
    )
    private String status;
    
    @JsonProperty("totalAmount")
    @Schema(
        description = "총 결제 금액 (원 단위)",
        example = "25000"
    )
    private Integer totalAmount;
    
    @JsonProperty("method")
    @Schema(
        description = "결제 수단",
        example = "카드",
        allowableValues = {"카드", "가상계좌", "계좌이체", "휴대폰", "상품권", "도서문화상품권", "게임문화상품권"}
    )
    private String method;
    
    @JsonProperty("requestedAt")
    @Schema(
        description = "결제 요청 시간 (ISO 8601 형식)",
        example = "2024-12-01T14:30:00+09:00"
    )
    private String requestedAt;
    
    @JsonProperty("approvedAt")
    @Schema(
        description = "결제 승인 시간 (ISO 8601 형식)",
        example = "2024-12-01T14:30:05+09:00"
    )
    private String approvedAt;
}