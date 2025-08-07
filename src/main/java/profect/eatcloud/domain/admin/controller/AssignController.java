package profect.eatcloud.domain.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import profect.eatcloud.domain.admin.dto.ManagerStoreApplicationRequestDto;
import profect.eatcloud.domain.admin.dto.ManagerStoreApplicationResponseDto;
import profect.eatcloud.domain.admin.service.AssignService;
import profect.eatcloud.common.ApiResponse;
import profect.eatcloud.common.ApiResponseStatus;

@RestController
@RequestMapping("/api/v1/unauth")
@Tag(name = "2-4. Unauth API", description = "로그인 없이, Admin에게 신청 요청하는 API")
@AllArgsConstructor
public class AssignController {

	private final AssignService assignService;

	@Operation(summary = "매니저·스토어 신청하기")
	@PostMapping("/manager-apply")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ManagerStoreApplicationResponseDto> apply(
		@RequestBody ManagerStoreApplicationRequestDto request) {

		ManagerStoreApplicationResponseDto resp = assignService.newManagerStoreApply(request);
		return ApiResponse.of(ApiResponseStatus.CREATED, resp);
	}

}
