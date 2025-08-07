package profect.eatcloud.domain.admin.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class ManagerCreateRequestDto {

	@Schema(description = "로그인 이메일", example = "mgr@example.com")
	private String email;

	@Schema(description = "비밀번호", example = "P@ssw0rd!")
	private String password;

	@Schema(description = "매니저 이름", example = "홍길동")
	private String name;

	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNumber;

	/** (선택) 바로 가게까지 할당하고 싶다면 전달 */
	@Schema(description = "할당할 가게 ID", example = "11111111-1111-1111-1111-111111111111")
	private UUID storeId;
}
