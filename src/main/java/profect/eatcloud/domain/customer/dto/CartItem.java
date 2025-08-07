package profect.eatcloud.domain.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class CartItem {
	private UUID menuId;
	private String menuName;
	private Integer quantity;
	private Integer price;
	private UUID storeId;
}