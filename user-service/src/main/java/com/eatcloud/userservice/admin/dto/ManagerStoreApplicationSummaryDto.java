package com.eatcloud.userservice.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Admin용: 신청서 목록 조회 시 표시할 요약 정보 DTO */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "매니저·스토어 신청서 요약 정보")
public class ManagerStoreApplicationSummaryDto {

	@Schema(description = "신청 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private UUID applicationId;

	@Schema(description = "매니저 이름", example = "홍길동")
	private String managerName;

	@Schema(description = "매니저 이메일", example = "mgr@example.com")
	private String managerEmail;

	@Schema(description = "스토어 이름", example = "우리동네피자")
	private String storeName;

	@Schema(description = "신청 상태", example = "PENDING")
	private String status;

	@Schema(description = "신청 일시")
	private LocalDateTime appliedAt;
}

