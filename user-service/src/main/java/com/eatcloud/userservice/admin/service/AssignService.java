package com.eatcloud.userservice.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.eatcloud.userservice.admin.dto.ManagerStoreApplicationRequestDto;
import com.eatcloud.userservice.admin.dto.ManagerStoreApplicationResponseDto;
import com.eatcloud.userservice.admin.entity.ManagerStoreApplication;
import com.eatcloud.userservice.admin.exception.AdminErrorCode;
import com.eatcloud.userservice.admin.exception.AdminException;
import com.eatcloud.userservice.admin.repository.ManagerStoreApplicationRepository;

@Service
@RequiredArgsConstructor
public class AssignService {

	private final ManagerStoreApplicationRepository managerStoreApplicationRepository;

	@Transactional
	public ManagerStoreApplicationResponseDto newManagerStoreApply(ManagerStoreApplicationRequestDto req) {
		if (managerStoreApplicationRepository.existsByManagerEmail(req.getManagerEmail())) {
			throw new AdminException(AdminErrorCode.APPLICATION_EMAIL_ALREADY_EXISTS);
		}

		ManagerStoreApplication entity = ManagerStoreApplication.builder()
			.managerName(req.getManagerName())
			.managerEmail(req.getManagerEmail())
			.managerPassword(req.getManagerPassword())
			.managerPhoneNumber(req.getManagerPhoneNumber())
			.storeName(req.getStoreName())
			.storeAddress(req.getStoreAddress())
			.storePhoneNumber(req.getStorePhoneNumber())
			.categoryId(req.getCategoryId())
			.description(req.getDescription())
			.status("PENDING")
			.build();

		ManagerStoreApplication saved = managerStoreApplicationRepository.save(entity);

		return ManagerStoreApplicationResponseDto.builder()
			.applicationId(saved.getApplicationId())
			.status(saved.getStatus())
			.createdAt(saved.getTimeData().getCreatedAt())
			.build();
	}

}