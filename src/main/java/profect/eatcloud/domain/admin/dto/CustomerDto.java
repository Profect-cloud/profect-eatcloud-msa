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
public class CustomerDto {
	private UUID id;
	private String name;
	private String nickname;
	private String email;
	private String password;
	private String phoneNumber;
	private Integer points;
}