package profect.eatcloud.domain.customer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import profect.eatcloud.domain.customer.dto.request.AddressRequestDto;
import profect.eatcloud.domain.customer.dto.response.AddressResponseDto;
import profect.eatcloud.domain.customer.entity.Address;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.exception.CustomerException;
import profect.eatcloud.domain.customer.repository.AddressRepository;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import profect.eatcloud.global.timeData.TimeData;
import profect.eatcloud.security.SecurityUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private AddressService addressService;

	private Customer customer;
	private UUID customerId;
	private TimeData timeData;

	@BeforeEach
	void setUp() {
		customerId = UUID.randomUUID();
		customer = Customer.builder()
			.id(customerId)
			.name("testUser")
			.build();

		timeData = TimeData.builder()
			.pTimeId(UUID.randomUUID())
			.createdAt(LocalDateTime.now())
			.createdBy("testUser")
			.updatedAt(LocalDateTime.now())
			.updatedBy("testUser")
			.build();
	}

	@Test
	@DisplayName("주소 목록 조회 - 성공")
	void getAddressList_Success() {

		given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
		Address address1 = createAddress(UUID.randomUUID(), "12345", "서울시 강남구", "101호", true);
		Address address2 = createAddress(UUID.randomUUID(), "67890", "서울시 서초구", "202호", false);

		given(addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customerId))
			.willReturn(Arrays.asList(address1, address2));

		// when
		List<AddressResponseDto> result = addressService.getAddressList(customerId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).zipcode()).isEqualTo("12345");
		assertThat(result.get(0).isSelected()).isTrue();
		assertThat(result.get(1).zipcode()).isEqualTo("67890");
		assertThat(result.get(1).isSelected()).isFalse();
	}

	@Test
	@DisplayName("주소 등록 - 첫 번째 주소는 기본 주소로 설정")
	void createAddress_FirstAddress_SetAsDefault() {
		// given
		AddressRequestDto request = new AddressRequestDto("12345", "서울시 강남구 테스트로 123", "101호");

		given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
		given(addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customerId))
			.willReturn(List.of());

		Address savedAddress = createAddress(UUID.randomUUID(), "12345", "서울시 강남구 테스트로 123", "101호", true);
		given(addressRepository.save(any(Address.class))).willReturn(savedAddress);

		// when
		AddressResponseDto result = addressService.createAddress(customerId, request);

		// then
		assertThat(result.isSelected()).isTrue();
		then(addressRepository).should().save(argThat(address ->
			address.getIsSelected() == true &&
				address.getZipcode().equals("12345")
		));
	}

	@Test
	@DisplayName("주소 등록 - 이미 주소가 있으면 기본값 false")
	void createAddress_NotFirstAddress_SetAsNotDefault() {
		// given
		AddressRequestDto request = new AddressRequestDto("12345", "서울시 강남구 테스트로 123", "101호");
		Address existingAddress = createAddress(UUID.randomUUID(), "99999", "기존주소", "기존", true);

		given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
		given(addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customerId))
			.willReturn(List.of(existingAddress)); // 이미 주소가 있음

		Address savedAddress = createAddress(UUID.randomUUID(), "12345", "서울시 강남구 테스트로 123", "101호", false);
		given(addressRepository.save(any(Address.class))).willReturn(savedAddress);

		// when
		AddressResponseDto result = addressService.createAddress(customerId, request);

		// then
		assertThat(result.isSelected()).isFalse();
		then(addressRepository).should().save(argThat(address ->
			address.getIsSelected() == false
		));
	}

	@Test
	@DisplayName("주소 수정 - 성공")
	void updateAddress_Success() {
		// given
		UUID addressId = UUID.randomUUID();
		Address existingAddress = createAddress(addressId, "12345", "서울시 강남구", "101호", false);
		AddressRequestDto request = new AddressRequestDto("99999", "서울시 송파구 새로운로 999", "999호");

		given(addressRepository.findByIdAndCustomerId(addressId, customerId))
			.willReturn(Optional.of(existingAddress));
		given(addressRepository.save(any(Address.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		AddressResponseDto result = addressService.updateAddress(customerId, addressId, request);

		// then
		assertThat(result.zipcode()).isEqualTo("99999");
		assertThat(result.roadAddr()).isEqualTo("서울시 송파구 새로운로 999");
		assertThat(result.detailAddr()).isEqualTo("999호");
		then(addressRepository).should().save(argThat(address ->
			address.getZipcode().equals("99999") &&
				address.getRoadAddr().equals("서울시 송파구 새로운로 999")
		));
	}

	@Test
	@DisplayName("주소 수정 - 존재하지 않는 주소")
	void updateAddress_NotFound() {
		// given
		UUID addressId = UUID.randomUUID();
		AddressRequestDto request = new AddressRequestDto("99999", "서울시 송파구", "999호");

		given(addressRepository.findByIdAndCustomerId(addressId, customerId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.updateAddress(customerId, addressId, request))
			.isInstanceOf(CustomerException.class)
			.hasMessage("해당 배송지를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("주소 삭제 - 성공")
	void deleteAddress_Success() {
		// given
		UUID addressId = UUID.randomUUID();
		Address address = createAddress(addressId, "12345", "서울시 강남구", "101호", false);

		given(addressRepository.findByIdAndCustomerId(addressId, customerId))
			.willReturn(Optional.of(address));

		try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
			securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testUser");

			// when
			addressService.deleteAddress(customerId, addressId);

			// then
			then(addressRepository).should().softDeleteByTimeId(
				eq(address.getTimeData().getPTimeId()),
				any(LocalDateTime.class),
				eq("testUser")
			);
		}
	}

	@Test
	@DisplayName("주소 삭제 - 존재하지 않는 주소")
	void deleteAddress_NotFound() {
		// given
		UUID addressId = UUID.randomUUID();

		given(addressRepository.findByIdAndCustomerId(addressId, customerId))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> addressService.deleteAddress(customerId, addressId))
			.isInstanceOf(CustomerException.class)
			.hasMessage("해당 배송지를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("기본 주소 설정 - 성공")
	void setDefaultAddress_Success() {
		// given
		UUID addressId = UUID.randomUUID();
		Address targetAddress = createAddress(addressId, "12345", "서울시 강남구", "101호", false);
		Address currentDefaultAddress = createAddress(UUID.randomUUID(), "67890", "서울시 서초구", "202호", true);

		given(addressRepository.findByIdAndCustomerId(addressId, customerId))
			.willReturn(Optional.of(targetAddress));
		given(addressRepository.findByCustomerIdAndIsSelectedTrueAndTimeData_DeletedAtIsNull(customerId))
			.willReturn(Optional.of(currentDefaultAddress));
		given(addressRepository.save(any(Address.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		addressService.setDefaultAddress(customerId, addressId);

		// then
		then(addressRepository).should(times(2)).save(any(Address.class));
		verify(addressRepository).save(argThat(address ->
			address.getId().equals(currentDefaultAddress.getId()) && !address.getIsSelected()
		));
		verify(addressRepository).save(argThat(address ->
			address.getId().equals(addressId) && address.getIsSelected()
		));
	}

	@Test
	@DisplayName("기본 주소 설정 - 이미 기본 주소인 경우")
	void setDefaultAddress_AlreadyDefault() {
		// given
		UUID addressId = UUID.randomUUID();
		Address targetAddress = createAddress(addressId, "12345", "서울시 강남구", "101호", true);

		given(addressRepository.findByIdAndCustomerId(addressId, customerId))
			.willReturn(Optional.of(targetAddress));

		// when
		addressService.setDefaultAddress(customerId, addressId);

		// then
		then(addressRepository).should(never()).save(any(Address.class));
	}

	private Address createAddress(UUID id, String zipcode, String roadAddr, String detailAddr, boolean isSelected) {
		Address address = Address.builder()
			.id(id)
			.zipcode(zipcode)
			.roadAddr(roadAddr)
			.detailAddr(detailAddr)
			.isSelected(isSelected)
			.customer(customer)
			.build();
		address.setTimeData(timeData);
		return address;
	}
}