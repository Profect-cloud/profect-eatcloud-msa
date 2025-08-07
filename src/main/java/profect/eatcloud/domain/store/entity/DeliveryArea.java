package profect.eatcloud.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;
import profect.eatcloud.global.timeData.BaseTimeEntity;
import java.util.UUID;

@Entity
@Table(name = "delivery_areas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryArea extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "area_id")
	private UUID areaId;

	@Column(name = "area_name", nullable = false, length = 100)
	private String areaName;
}