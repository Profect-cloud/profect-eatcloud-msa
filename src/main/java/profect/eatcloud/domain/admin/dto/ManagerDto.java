package profect.eatcloud.domain.admin.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "매니저 계정 정보 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerDto {

	@Schema(description = "매니저 고유 식별자 (18자리)", example = "A1B2C3D4E5F6G7H8I9")
	private UUID id;

	@Schema(description = "매니저 이름", example = "홍길동")
	private String name;

	@Schema(description = "이메일 주소", example = "owner@example.com")
	private String email;

	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNumber;

	@Schema(description = "할당된 가게 ID (18자리)", example = "Z9Y8X7W6V5U4T3S2R1")
	private UUID storeId;
}
