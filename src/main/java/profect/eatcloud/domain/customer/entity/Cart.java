package profect.eatcloud.domain.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import profect.eatcloud.domain.customer.dto.CartItem;
import profect.eatcloud.global.timeData.BaseTimeEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "p_cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "cart_id")
	private UUID cartId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "cart_items", nullable = false, columnDefinition = "jsonb")
	private List<CartItem> cartItems;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;
}