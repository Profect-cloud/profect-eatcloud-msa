package com.eatcloud.userservice.customer.dto.response;

import java.util.Objects;
import java.util.UUID;

import com.eatcloud.userservice.customer.entity.Customer;

public record CustomerProfileResponseDto(
	UUID id,
	String name,
	String nickname,
	String email,
	String phoneNumber,
	Integer points
) {
	public static CustomerProfileResponseDto from(Customer customer) {
		Objects.requireNonNull(customer, "Customer cannot be null");

		return new CustomerProfileResponseDto(
			customer.getId(),
			customer.getName(),
			customer.getNickname(),
			customer.getEmail(),
			customer.getPhoneNumber(),
			customer.getPoints()
		);
	}
}