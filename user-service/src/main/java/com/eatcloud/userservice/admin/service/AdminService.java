package com.eatcloud.userservice.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import com.eatcloud.userservice.admin.dto.ManagerDto;
import com.eatcloud.userservice.admin.dto.UserDto;
import com.eatcloud.userservice.admin.exception.AdminErrorCode;
import com.eatcloud.userservice.admin.exception.AdminException;
import com.eatcloud.userservice.customer.entity.Customer;
import com.eatcloud.userservice.customer.repository.CustomerRepository;
import com.eatcloud.userservice.manager.entity.Manager;
import com.eatcloud.userservice.manager.repository.ManagerRepository;

@Service
@AllArgsConstructor
public class AdminService {
	private final CustomerRepository customerRepository;
	private final ManagerRepository managerRepository;

	@Transactional(readOnly = true)
	public List<UserDto> getAllCustomers() {
		return customerRepository.findAll().stream()
			.map(c -> UserDto.builder()
				.id(c.getId())
				.name(c.getName())
				.nickname(c.getNickname())
				.email(c.getEmail())
				.phoneNumber(c.getPhoneNumber())
				.points(c.getPoints())
				.build()
			)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public UserDto getCustomerByEmail(String email) {
		Customer c = customerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.CUSTOMER_NOT_FOUND));

		return UserDto.builder()
			.id(c.getId())
			.name(c.getName())
			.nickname(c.getNickname())
			.email(c.getEmail())
			.phoneNumber(c.getPhoneNumber())
			.points(c.getPoints())
			.build();
	}

	@Transactional
	public void deleteCustomerByEmail(String email) {
		Customer c = customerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.CUSTOMER_NOT_FOUND));

		customerRepository.deleteById(c.getId());
	}

	@Transactional(readOnly = true)
	public List<ManagerDto> getAllManagers() {
		return managerRepository.findAll().stream()
			.map(m -> ManagerDto.builder()
				.id(m.getId())
				.name(m.getName())
				.email(m.getEmail())
				.phoneNumber(m.getPhoneNumber())
				.storeId(m.getStoreId())
				.build()
			)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ManagerDto getManagerByEmail(String email) {
		Manager m = managerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.MANAGER_NOT_FOUND));

		return ManagerDto.builder()
			.id(m.getId())
			.name(m.getName())
			.email(m.getEmail())
			.phoneNumber(m.getPhoneNumber())
			.storeId(m.getStoreId())
			.build();
	}

	@Transactional
	public void deleteManagerByEmail(String email) {
		Manager m = managerRepository.findByEmail(email)
			.orElseThrow(() -> new AdminException(AdminErrorCode.MANAGER_NOT_FOUND));

		managerRepository.deleteById(m.getId());
	}
}