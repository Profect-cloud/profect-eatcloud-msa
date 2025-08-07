package profect.eatcloud.domain.admin.dto;

import java.util.Map;

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
public class DashboardDto {
	private Long totalCustomers;
	private Long totalStores;
	private Long totalCategories;
	private Long totalOrders;
	private Map<String, Long> additionalMetrics; // 예: 일별 매출, 주문 건수
}
