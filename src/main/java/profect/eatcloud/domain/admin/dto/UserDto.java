package profect.eatcloud.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "관리자 전용 전체 사용자 목록 조회 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

	private UUID id;

	@Schema(description = "사용자 실제 이름", example = "홍길동")
	private String name;

	@Schema(description = "사용자 닉네임", example = "귀여운 어피치")
	private String nickname;

	@Schema(description = "이메일 주소", example = "gildong@example.com")
	private String email;

	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNumber;

	@Schema(description = "보유 포인트", example = "1000")
	private Integer points;

	@Schema(description = "계정 생성 일시", example = "2025-07-29T14:00:00")
	private LocalDateTime createdAt;
}
