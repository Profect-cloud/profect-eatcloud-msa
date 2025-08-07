package profect.eatcloud.domain.admin.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import profect.eatcloud.global.timeData.BaseTimeEntity;

@Entity
@Table(name = "p_admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(length = 100)
	private String name;

	@Column(nullable = false)
	private String password;

	@Column(name = "phone_number", length = 18)
	private String phoneNumber;

	@Column(length = 50)
	private String position;
}
