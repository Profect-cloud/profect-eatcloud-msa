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
public class AdminDto {
	private UUID id;
	private String name;
	private String email;
	private String password;
	private String phoneNumber;
	private String position;
}
