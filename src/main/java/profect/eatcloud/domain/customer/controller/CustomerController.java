package profect.eatcloud.domain.customer.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import profect.eatcloud.domain.customer.dto.request.CustomerProfileUpdateRequestDto;
import profect.eatcloud.domain.customer.dto.request.CustomerWithdrawRequestDto;
import profect.eatcloud.domain.customer.dto.response.CustomerProfileResponseDto;
import profect.eatcloud.domain.customer.exception.CustomerErrorCode;
import profect.eatcloud.domain.customer.exception.CustomerException;
import profect.eatcloud.domain.customer.message.ResponseMessage;
import profect.eatcloud.domain.customer.service.CustomerService;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "3-1. CustomerController", description = "고객 프로필 관리 API")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	private UUID getCustomerUuid(@AuthenticationPrincipal UserDetails userDetails) {
		try {
			return UUID.fromString(userDetails.getUsername());
		} catch (IllegalArgumentException e) {
			throw new CustomerException(CustomerErrorCode.INVALID_CUSTOMER_ID);
		}
	}

	@Operation(summary = "1. 고객 프로필 조회", description = "인증된 고객의 프로필 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 고객 ID"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객을 찾을 수 없음")
	})
	@GetMapping("/profile")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<CustomerProfileResponseDto> getCustomer(
		@AuthenticationPrincipal UserDetails userDetails) {

		UUID customerId = getCustomerUuid(userDetails);
		CustomerProfileResponseDto response = customerService.getCustomerProfile(customerId);
		return profect.eatcloud.common.ApiResponse.success(response);
	}

	@Operation(summary = "2. 고객 프로필 수정", description = "인증된 고객의 프로필 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객을 찾을 수 없음"),
		@ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
	})
	@PatchMapping("/profile")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<ResponseMessage> updateCustomer(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody CustomerProfileUpdateRequestDto request) {

		UUID customerId = getCustomerUuid(userDetails);
		customerService.updateCustomer(customerId, request);
		return profect.eatcloud.common.ApiResponse.success(ResponseMessage.PROFILE_UPDATE_SUCCESS);
	}

	@Operation(summary = "3. 고객 탈퇴", description = "인증된 고객이 서비스에서 탈퇴합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "탈퇴 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 탈퇴 사유 누락"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "고객을 찾을 수 없음")
	})
	@PostMapping("/withdraw")
	@ResponseStatus(HttpStatus.OK)
	public profect.eatcloud.common.ApiResponse<ResponseMessage> withdrawCustomer(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody CustomerWithdrawRequestDto request) {

		UUID customerId = getCustomerUuid(userDetails);
		customerService.withdrawCustomer(customerId, request);
		return profect.eatcloud.common.ApiResponse.success(ResponseMessage.CUSTOMER_WITHDRAW_SUCCESS);
	}
}