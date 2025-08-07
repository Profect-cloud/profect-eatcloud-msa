package profect.eatcloud.domain.manager.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import profect.eatcloud.common.ApiResponse;
import profect.eatcloud.domain.manager.dto.StoreRegisterRequestDto;
import profect.eatcloud.domain.manager.message.ManagerResponseMessage;
import profect.eatcloud.domain.manager.service.ManagerService;
import profect.eatcloud.domain.store.dto.AiDescriptionRequestDto;
import profect.eatcloud.domain.store.dto.AiDescriptionResponseDto;
import profect.eatcloud.domain.store.dto.MenuRequestDto;
import profect.eatcloud.domain.store.dto.MenuResponseDto;
import profect.eatcloud.domain.store.dto.StoreRequestDto;
import profect.eatcloud.domain.store.entity.Menu;
import profect.eatcloud.domain.store.service.AiDescriptionService;

@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "4. ManagerController")
public class ManagerController {

	private final ManagerService managerService;
	private final AiDescriptionService aiDescriptionService;

	@Operation(summary = "1-1. 메뉴 생성")
	@PostMapping("/stores/{storeId}/menus")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MenuResponseDto> createMenu(@PathVariable UUID storeId, @RequestBody @Valid MenuRequestDto dto) {
		Menu created = managerService.createMenu(storeId, dto);
		return ApiResponse.success(MenuResponseDto.from(created));
	}

	//    @Operation(summary = "1-2. 메뉴 수정")
	//    @PutMapping("/stores/{storeId}/menus/{menuId}")
	//    @ResponseStatus(HttpStatus.OK)
	//    // dto로 바꾸기
	//    // 여기서 update 부분 엔티티에서 함수쓰지말고 if문으로
	//    public ApiResponse<MenuResponseDto> updateMenu(@PathVariable UUID storeId,
	//                                                   @PathVariable UUID menuId,
	//                                                   @RequestBody @Valid MenuRequestDto dto) {
	//        Menu updated = managerService.updateMenu(storeId, menuId, dto);
	//
	//        return ApiResponse.success(MenuResponseDto.from(updated));
	//    }

	@Operation(summary = "1-2. 메뉴 수정")
	@PutMapping("/stores/{storeId}/menus/{menuId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<MenuResponseDto> updateMenu(
		@PathVariable UUID storeId,
		@PathVariable UUID menuId,
		@RequestBody @Valid MenuRequestDto dto
	) {
		// DTO를 받아서 서비스에 넘김
		Menu updated = managerService.updateMenu(storeId, menuId, dto);
		return ApiResponse.success(MenuResponseDto.from(updated));
	}

	@Operation(summary = "1-3. 메뉴 삭제")
	@DeleteMapping("/stores/{storeId}/menus/{menuId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ManagerResponseMessage> deleteMenu(@PathVariable UUID menuId) {
		managerService.deleteMenu(menuId);
		return ApiResponse.success(ManagerResponseMessage.MENU_DELETE_SUCCESS);
	}

	@Operation(summary = "1-4. AI 메뉴 설명 생성")
	@PostMapping("/stores/{storeId}/menus/ai-description")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<AiDescriptionResponseDto> generateAIDescription(
		@PathVariable UUID storeId,
		@RequestBody @Valid AiDescriptionRequestDto requestDto) {

		String description = aiDescriptionService.generateDescription(requestDto);
		return ApiResponse.success(new AiDescriptionResponseDto(description));
	}

	//    @Operation(summary = "2-1. 가게 정보 수정")
	//    @PutMapping("/stores/{storeId}")
	//    @ResponseStatus(HttpStatus.OK)
	//    public ApiResponse<ManagerResponseMessage> updateStore(@PathVariable UUID storeId,
	//                                                           @RequestBody @Valid StoreRequestDto dto) {
	//        managerService.updateStore(storeId, dto);
	//        return ApiResponse.success(ManagerResponseMessage.STORE_UPDATE_SUCCESS);
	//    }

	@Operation(summary = "2-1. 가게 정보 수정")
	@PutMapping("/stores/{storeId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ManagerResponseMessage> updateStore(@PathVariable UUID storeId,
		@RequestBody @Valid StoreRequestDto dto) {
		managerService.updateStore(storeId, dto);
		return ApiResponse.success(ManagerResponseMessage.STORE_UPDATE_SUCCESS);
	}

	@Operation(summary = "2-2. 가게 등록 요청")
	@PostMapping("/stores/register")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ManagerResponseMessage> applyForStoreRegistration(
		@RequestBody @Valid StoreRegisterRequestDto dto,
		@AuthenticationPrincipal(expression = "id") UUID managerId) {

		managerService.requestStoreRegistration(managerId, dto);
		return ApiResponse.success(ManagerResponseMessage.STORE_REGISTRATION_REQUEST_SUCCESS);
	}

	@Operation(summary = "2-3. 가게 폐업 요청")
	@PostMapping("/stores/{storeId}/close")
	public ApiResponse<ManagerResponseMessage> applyForStoreClosure(
		@PathVariable UUID storeId,
		@AuthenticationPrincipal(expression = "id") UUID managerId) {

		managerService.requestStoreClosure(managerId, storeId);
		return ApiResponse.success(ManagerResponseMessage.STORE_CLOSURE_REQUEST_SUCCESS);
	}

}
