package profect.eatcloud.domain.store.entity;

import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import profect.eatcloud.domain.globalCategory.entity.StoreCategory;
import profect.eatcloud.domain.store.dto.StoreSearchResponseDto;
import profect.eatcloud.global.timeData.BaseTimeEntity;

@Entity
@Table(name = "p_stores")
@SqlResultSetMapping(
	name = "StoreSearchResponseMapping",
	classes = @ConstructorResult(
		targetClass = StoreSearchResponseDto.class,
		columns = {
			@ColumnResult(name = "store_id", type = UUID.class),
			@ColumnResult(name = "store_name", type = String.class),
			@ColumnResult(name = "store_address", type = String.class),
			@ColumnResult(name = "store_lat", type = Double.class),
			@ColumnResult(name = "store_lon", type = Double.class),
			@ColumnResult(name = "min_cost", type = Integer.class),
			@ColumnResult(name = "open_status", type = Boolean.class)
		}
	)
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class Store extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "store_id")
	private UUID storeId;

	@Column(name = "store_name", nullable = false, length = 200)
	private String storeName;

	@Column(name = "store_address", length = 300)
	private String storeAddress;

	@Column(name = "phone_number", length = 18)
	private String phoneNumber;

	@Column(name = "min_cost", nullable = false)
	@Builder.Default
	private Integer minCost = 0;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "store_lat")
	private Double storeLat;

	@Column(name = "store_lon")
	private Double storeLon;

	@Column(name = "open_status")
	private Boolean openStatus;

	@Column(name = "open_time", nullable = false)
	private LocalTime openTime;

	@Column(name = "close_time", nullable = false)
	private LocalTime closeTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private StoreCategory storeCategory;

}