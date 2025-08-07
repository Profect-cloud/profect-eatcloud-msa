package profect.eatcloud.domain.customer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AddCartItemRequest {
    @NotNull(message = "메뉴 ID는 필수입니다.")
    private UUID menuId;

    @NotNull(message = "메뉴 이름은 필수입니다.")
    private String menuName;

    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 0보다 커야 합니다.")
    private Integer quantity;

    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private Integer price;

    @NotNull(message = "매장 ID는 필수입니다.")
    private UUID storeId;
}
