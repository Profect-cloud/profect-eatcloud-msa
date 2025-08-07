package profect.eatcloud.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "매니저·스토어 신청 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerStoreApplicationResponseDto {

	@Schema(description = "신청 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID applicationId;

	@Schema(description = "상태", example = "PENDING")
	private String status;

	@Schema(description = "신청 일시")
	private LocalDateTime createdAt;
}
