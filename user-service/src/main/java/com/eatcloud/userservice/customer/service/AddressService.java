package com.eatcloud.userservice.customer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.eatcloud.userservice.customer.dto.request.AddressRequestDto;
import com.eatcloud.userservice.customer.dto.response.AddressResponseDto;
import com.eatcloud.userservice.customer.entity.Address;
import com.eatcloud.userservice.customer.entity.Customer;
import com.eatcloud.userservice.customer.exception.CustomerErrorCode;
import com.eatcloud.userservice.customer.exception.CustomerException;
import com.eatcloud.userservice.customer.repository.AddressRepository;
import com.eatcloud.userservice.customer.repository.CustomerRepository;
import com.eatcloud.userservice.security.SecurityUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

	private final AddressRepository addressRepository;
	private final CustomerRepository customerRepository;

	public List<AddressResponseDto> getAddressList(UUID customerId) {
		customerRepository.findById(customerId)
			.orElseThrow(() -> new CustomerException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
		List<Address> addresses = addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customerId);
		return addresses.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());
	}

	@Transactional
	public AddressResponseDto createAddress(UUID customerId, AddressRequestDto request) {
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new CustomerException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
		List<Address> existingAddresses = addressRepository.findByCustomerIdAndTimeData_DeletedAtIsNull(customerId);
		boolean isFirstAddress = existingAddresses.isEmpty();

		Address address = Address.builder()
			.zipcode(request.zipcode())
			.roadAddr(request.roadAddr())
			.detailAddr(request.detailAddr())
			.isSelected(isFirstAddress)
			.customer(customer)
			.build();

		Address savedAddress = addressRepository.save(address);
		return toResponse(savedAddress);
	}

	@Transactional
	public AddressResponseDto updateAddress(UUID customerId, UUID addressId, AddressRequestDto request) {
		Address address = addressRepository.findByIdAndCustomerId(addressId, customerId).orElseThrow(() -> new CustomerException(CustomerErrorCode.ADDRESS_NOT_FOUND));

		address.updateAddress(request.zipcode(), request.roadAddr(), request.detailAddr());

		Address updatedAddress = addressRepository.save(address);
		return toResponse(updatedAddress);
	}

	@Transactional
	public void deleteAddress(UUID customerId, UUID addressId) {
		Address address = addressRepository.findByIdAndCustomerId(addressId, customerId)
			.orElseThrow(() -> new CustomerException(CustomerErrorCode.ADDRESS_NOT_FOUND));

		addressRepository.softDeleteByTimeId(
			address.getTimeData().getPTimeId(),
			LocalDateTime.now(),
			SecurityUtil.getCurrentUsername()
		);
	}

	@Transactional
	public void setDefaultAddress(UUID customerId, UUID addressId) {
		Address targetAddress = addressRepository.findByIdAndCustomerId(addressId, customerId)
			.orElseThrow(() -> new CustomerException(CustomerErrorCode.ADDRESS_NOT_FOUND));

		if (targetAddress.getIsSelected()) {
			return;
		}

		addressRepository.findByCustomerIdAndIsSelectedTrueAndTimeData_DeletedAtIsNull(customerId)
			.ifPresent(currentDefault -> {
				currentDefault.changeSelected(false);
				addressRepository.save(currentDefault);
			});

		targetAddress.changeSelected(true);
		addressRepository.save(targetAddress);
	}

	private AddressResponseDto toResponse(Address address) {
		return new AddressResponseDto(
			address.getId(),
			address.getZipcode(),
			address.getRoadAddr(),
			address.getDetailAddr(),
			address.getIsSelected()
		);
	}
}