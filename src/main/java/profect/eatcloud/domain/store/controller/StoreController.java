package profect.eatcloud.domain.store.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import profect.eatcloud.common.ApiResponse;
import profect.eatcloud.domain.store.dto.StoreSearchByMenuCategoryRequestDto;
import profect.eatcloud.domain.store.dto.StoreSearchRequestDto;
import profect.eatcloud.domain.store.dto.StoreSearchResponseDto;
import profect.eatcloud.domain.store.service.StoreService;

@RestController
@RequestMapping("/api/v1/stores")
@AllArgsConstructor

@Tag(name = "5-1. StoreController")
public class StoreController {

	private final StoreService storeService;

	@Operation(summary = "1. 매장 카테고리 별 거리기반 매장 조회")
	@GetMapping("/search/category")
	public ApiResponse<List<StoreSearchResponseDto>> searchStoresByCategoryAndDistance(
		@ModelAttribute StoreSearchRequestDto condition
	) {
		List<StoreSearchResponseDto> stores = storeService.searchStoresByCategoryAndDistance(condition);
		return ApiResponse.success(stores);
	}

	@Operation(summary = "2. 메뉴 카테고리 별 거리 기반 매장 검색")
	@GetMapping("/search/menu-category")
	public ApiResponse<List<StoreSearchResponseDto>> searchStoresByMenuCategoryAndDistance(
		@ModelAttribute StoreSearchByMenuCategoryRequestDto condition
	) {
		List<StoreSearchResponseDto> stores = storeService.searchStoresByMenuCategory(condition);
		return ApiResponse.success(stores);
	}

}