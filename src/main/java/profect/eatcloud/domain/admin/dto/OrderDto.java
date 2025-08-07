package profect.eatcloud.domain.admin.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
	private UUID orderId;
	private String orderNumber;
	private UUID customerId;
	private UUID storeId;
	private UUID paymentId;
	private String orderStatus;
	private String orderType;
	private String orderMenuList;  // JSON 형태
}
