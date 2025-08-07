package profect.eatcloud.domain.customer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdateCartItemRequest {
    @NotNull(message = "메뉴 ID는 필수입니다.")
    private UUID menuId;

    @NotNull(message = "수량은 필수입니다.")
    @PositiveOrZero(message = "수량은 0 이상이어야 합니다.")
    private Integer quantity;
}
