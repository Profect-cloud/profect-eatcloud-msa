package profect.eatcloud.domain.customer.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import profect.eatcloud.domain.customer.dto.CartItem;
import profect.eatcloud.domain.customer.dto.request.AddCartItemRequest;
import profect.eatcloud.domain.customer.dto.request.UpdateCartItemRequest;
import profect.eatcloud.domain.customer.exception.CustomerErrorCode;
import profect.eatcloud.domain.customer.exception.CustomerException;
import profect.eatcloud.domain.customer.message.ResponseMessage;
import profect.eatcloud.domain.customer.service.CartService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/customers/cart")
@Tag(name = "3-3. CartController", description = "장바구니 관리 API")
public class CartController {

	private final CartService cartService;

	private UUID getCustomerUuid(@AuthenticationPrincipal UserDetails userDetails) {
		try {
			return UUID.fromString(userDetails.getUsername());
		} catch (IllegalArgumentException e) {
			throw new CustomerException(CustomerErrorCode.INVALID_CUSTOMER_ID);
		}
	}

	@Operation(summary = "1. 장바구니 추가", description = "장바구니에 메뉴를 추가합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "추가 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객 또는 메뉴를 찾을 수 없음")
	})
	@PostMapping("/add")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<ResponseMessage> addItem(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestBody AddCartItemRequest request) {

		UUID customerId = getCustomerUuid(userDetails);
		cartService.addItem(customerId, request);
		return profect.eatcloud.common.ApiResponse.success(ResponseMessage.CART_ADD_SUCCESS);
	}

	@Operation(summary = "2. 장바구니 조회", description = "장바구니를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 고객 ID"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객을 찾을 수 없음")
	})
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<List<CartItem>> getCart(
		@AuthenticationPrincipal UserDetails userDetails) {

		UUID customerId = getCustomerUuid(userDetails);
		List<CartItem> cartItems = cartService.getCart(customerId);
		return profect.eatcloud.common.ApiResponse.success(cartItems);
	}

	@Operation(summary = "3. 장바구니 메뉴 수량 변경", description = "장바구니에 담긴 메뉴의 수량을 변경합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객 또는 장바구니를 찾을 수 없음")
	})
	@PatchMapping("/update")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<ResponseMessage> updateQuantity(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestBody UpdateCartItemRequest request) {

		UUID customerId = getCustomerUuid(userDetails);
		cartService.updateItemQuantity(customerId, request);
		return profect.eatcloud.common.ApiResponse.success(ResponseMessage.CART_UPDATE_SUCCESS);
	}

	@Operation(summary = "4. 장바구니 메뉴 개별 삭제", description = "장바구니에 담긴 메뉴를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "삭제 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객 또는 장바구니를 찾을 수 없음")
	})
	@DeleteMapping("/delete/{menuId}")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<ResponseMessage> removeItem(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable UUID menuId) {

		UUID customerId = getCustomerUuid(userDetails);
		cartService.removeItem(customerId, menuId);
		return profect.eatcloud.common.ApiResponse.success(ResponseMessage.CART_ITEM_DELETE_SUCCESS);
	}

	@Operation(summary = "5. 장바구니 전체 삭제", description = "장바구니에 담긴 모든 메뉴를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객을 찾을 수 없음")
	})
	@DeleteMapping("/clear")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<ResponseMessage> clearCart(
		@AuthenticationPrincipal UserDetails userDetails) {

		UUID customerId = getCustomerUuid(userDetails);
		cartService.clearCart(customerId);
		return profect.eatcloud.common.ApiResponse.success(ResponseMessage.CART_CLEAR_SUCCESS);
	}
}