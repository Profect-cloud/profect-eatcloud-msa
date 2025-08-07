package profect.eatcloud.domain.customer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import profect.eatcloud.common.ApiResponse;
import profect.eatcloud.domain.customer.dto.request.AddressRequestDto;
import profect.eatcloud.domain.customer.dto.response.AddressResponseDto;
import profect.eatcloud.domain.customer.exception.CustomerErrorCode;
import profect.eatcloud.domain.customer.exception.CustomerException;
import profect.eatcloud.domain.customer.message.ResponseMessage;
import profect.eatcloud.domain.customer.service.AddressService;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

	@Mock
	private AddressService addressService;

	private AddressController addressController;
	private UUID customerId;
	private UserDetails userDetails;

	@BeforeEach
	void setUp() {
		addressController = new AddressController(addressService);
		customerId = UUID.randomUUID();

		userDetails = User.builder()
			.username(customerId.toString())
			.password("password")
			.authorities(Collections.emptyList())
			.build();
	}

	@Test
	@DisplayName("주소 목록 조회 - 성공")
	void getAddressList_Success() {
		// given
		AddressResponseDto address1 = new AddressResponseDto(
			UUID.randomUUID(),
			"12345",
			"서울시 강남구 테스트로 123",
			"101호",
			true
		);
		AddressResponseDto address2 = new AddressResponseDto(
			UUID.randomUUID(),
			"67890",
			"서울시 서초구 샘플로 456",
			"202호",
			false
		);
		List<AddressResponseDto> addresses = Arrays.asList(address1, address2);

		given(addressService.getAddressList(customerId)).willReturn(addresses);

		// when
		ApiResponse<List<AddressResponseDto>> response =
			addressController.getAddressList(userDetails);

		// then
		assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getData()).hasSize(2);
		assertThat(response.getData().get(0).zipcode()).isEqualTo("12345");
		assertThat(response.getData().get(0).isSelected()).isTrue();
		assertThat(response.getData().get(1).zipcode()).isEqualTo("67890");
		assertThat(response.getData().get(1).isSelected()).isFalse();
	}

	@Test
	@DisplayName("주소 목록 조회 - 빈 목록")
	void getAddressList_EmptyList() {
		// given
		given(addressService.getAddressList(customerId)).willReturn(List.of());

		// when
		ApiResponse<List<AddressResponseDto>> response =
			addressController.getAddressList(userDetails);

		// then
		assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getData()).isEmpty();
	}

	@Test
	@DisplayName("주소 등록 - 성공")
	void createAddress_Success() {
		// given
		AddressRequestDto request = new AddressRequestDto(
			"12345",
			"서울시 강남구 테스트로 123",
			"101호"
		);

		AddressResponseDto expectedResponse = new AddressResponseDto(
			UUID.randomUUID(),
			"12345",
			"서울시 강남구 테스트로 123",
			"101호",
			true
		);

		given(addressService.createAddress(eq(customerId), any(AddressRequestDto.class)))
			.willReturn(expectedResponse);

		// when
		ApiResponse<List<AddressResponseDto>> response =
			addressController.createAddress(userDetails, request);

		// then
		assertThat(response.getCode()).isEqualTo(HttpStatus.CREATED.value());
		assertThat(response.getData()).isNotNull();
		assertThat(response.getData()).hasSize(1);
		assertThat(response.getData().getFirst().zipcode()).isEqualTo("12345");
		assertThat(response.getData().getFirst().roadAddr()).isEqualTo("서울시 강남구 테스트로 123");
		assertThat(response.getData().getFirst().detailAddr()).isEqualTo("101호");
		assertThat(response.getData().getFirst().isSelected()).isTrue();
	}

	@Test
	@DisplayName("주소 등록 - Service에서 고객을 찾을 수 없는 예외 발생")
	void createAddress_CustomerNotFound() {
		// given
		AddressRequestDto request = new AddressRequestDto(
			"12345",
			"서울시 강남구 테스트로 123",
			"101호"
		);

		given(addressService.createAddress(eq(customerId), any(AddressRequestDto.class)))
			.willThrow(new CustomerException(CustomerErrorCode.CUSTOMER_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> addressController.createAddress(userDetails, request))
			.isInstanceOf(CustomerException.class)
			.hasMessage("해당 고객을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("주소 수정 - 성공")
	void updateAddress_Success() {
		// given
		UUID addressId = UUID.randomUUID();
		AddressRequestDto request = new AddressRequestDto(
			"99999",
			"서울시 송파구 새로운로 999",
			"999호"
		);

		AddressResponseDto expectedResponse = new AddressResponseDto(
			addressId,
			"99999",
			"서울시 송파구 새로운로 999",
			"999호",
			false
		);

		given(addressService.updateAddress(eq(customerId), eq(addressId), any(AddressRequestDto.class)))
			.willReturn(expectedResponse);

		// when
		ApiResponse<List<AddressResponseDto>> response =
			addressController.updateAddress(userDetails, addressId, request);

		// then
		assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getData()).isNotNull();
		assertThat(response.getData()).hasSize(1);
		assertThat(response.getData().getFirst().id()).isEqualTo(addressId);
		assertThat(response.getData().getFirst().zipcode()).isEqualTo("99999");
		assertThat(response.getData().getFirst().roadAddr()).isEqualTo("서울시 송파구 새로운로 999");
		assertThat(response.getData().getFirst().detailAddr()).isEqualTo("999호");
	}

	@Test
	@DisplayName("주소 수정 - 존재하지 않는 주소")
	void updateAddress_AddressNotFound() {
		// given
		UUID addressId = UUID.randomUUID();
		AddressRequestDto request = new AddressRequestDto(
			"99999",
			"서울시 송파구 새로운로 999",
			"999호"
		);

		given(addressService.updateAddress(eq(customerId), eq(addressId), any(AddressRequestDto.class)))
			.willThrow(new CustomerException(CustomerErrorCode.ADDRESS_NOT_FOUND));

		// when & then
		assertThatThrownBy(() -> addressController.updateAddress(userDetails, addressId, request))
			.isInstanceOf(CustomerException.class)
			.hasMessage("해당 배송지를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("주소 삭제 - 성공")
	void deleteAddress_Success() {
		// given
		UUID addressId = UUID.randomUUID();
		willDoNothing().given(addressService).deleteAddress(customerId, addressId);

		// when
		ApiResponse<ResponseMessage> response = addressController.deleteAddress(userDetails, addressId);

		// then
		assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getData()).isEqualTo(ResponseMessage.ADDRESS_DELETE_SUCCESS);
	}

	@Test
	@DisplayName("주소 삭제 - 존재하지 않는 주소")
	void deleteAddress_AddressNotFound() {
		// given
		UUID addressId = UUID.randomUUID();
		willThrow(new CustomerException(CustomerErrorCode.ADDRESS_NOT_FOUND))
			.given(addressService).deleteAddress(customerId, addressId);

		// when & then
		assertThatThrownBy(() -> addressController.deleteAddress(userDetails, addressId))
			.isInstanceOf(CustomerException.class)
			.hasMessage("해당 배송지를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("기본 주소 설정 - 성공")
	void setDefaultAddress_Success() {
		// given
		UUID addressId = UUID.randomUUID();
		willDoNothing().given(addressService).setDefaultAddress(customerId, addressId);

		// when
		ApiResponse<ResponseMessage> response = addressController.setDefaultAddress(userDetails, addressId);

		// then
		assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getData()).isEqualTo(ResponseMessage.ADDRESS_SELECT_SUCCESS);
	}

	@Test
	@DisplayName("기본 주소 설정 - 존재하지 않는 주소")
	void setDefaultAddress_AddressNotFound() {
		// given
		UUID addressId = UUID.randomUUID();
		willThrow(new CustomerException(CustomerErrorCode.ADDRESS_NOT_FOUND))
			.given(addressService).setDefaultAddress(customerId, addressId);

		// when & then
		assertThatThrownBy(() -> addressController.setDefaultAddress(userDetails, addressId))
			.isInstanceOf(CustomerException.class)
			.hasMessage("해당 배송지를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("잘못된 UUID 형식으로 예외 발생")
	void invalidCustomerUuid_ThrowsException() {
		// given
		UserDetails invalidUserDetails = User.builder()
			.username("invalid-uuid")
			.password("password")
			.authorities(Collections.emptyList())
			.build();

		// when & then
		assertThatThrownBy(() -> addressController.getAddressList(invalidUserDetails))
			.isInstanceOf(CustomerException.class)
			.hasMessage("유효하지 않은 고객 ID입니다");
	}
}