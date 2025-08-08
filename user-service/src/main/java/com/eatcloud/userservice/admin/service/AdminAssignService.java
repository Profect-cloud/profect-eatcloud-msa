package com.eatcloud.userservice.admin.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import com.eatcloud.userservice.admin.dto.ManagerStoreApplicationDetailDto;
import com.eatcloud.userservice.admin.dto.ManagerStoreApplicationSummaryDto;
import com.eatcloud.userservice.admin.entity.ManagerStoreApplication;
import com.eatcloud.userservice.admin.exception.AdminErrorCode;
import com.eatcloud.userservice.admin.exception.AdminException;
import com.eatcloud.userservice.admin.repository.ManagerStoreApplicationRepository;
import com.eatcloud.userservice.manager.entity.Manager;
import com.eatcloud.userservice.manager.repository.ManagerRepository;

@Service
@AllArgsConstructor
public class AdminAssignService {

	private final ManagerStoreApplicationRepository managerStoreApplicationRepository;
	private final ManagerRepository managerRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<ManagerStoreApplicationSummaryDto> getAllApplications() {
		return managerStoreApplicationRepository.findAll().stream()
			.map(app -> ManagerStoreApplicationSummaryDto.builder()
				.applicationId(app.getApplicationId())
				.managerName(app.getManagerName())
				.managerEmail(app.getManagerEmail())
				.storeName(app.getStoreName())
				.status(app.getStatus())
				.appliedAt(app.getTimeData().getCreatedAt())
				.build()
			)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ManagerStoreApplicationDetailDto getApplicationDetail(UUID applicationId) {
		ManagerStoreApplication app = managerStoreApplicationRepository.findById(applicationId)
			.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));

		return ManagerStoreApplicationDetailDto.builder()
			.applicationId(app.getApplicationId())
			.managerName(app.getManagerName())
			.managerEmail(app.getManagerEmail())
			.managerPhoneNumber(app.getManagerPhoneNumber())
			.storeName(app.getStoreName())
			.storeAddress(app.getStoreAddress())
			.storePhoneNumber(app.getStorePhoneNumber())
			.categoryId(app.getCategoryId())
			.description(app.getDescription())
			.status(app.getStatus())
			.reviewerAdminId(app.getReviewerAdminId())
			.reviewComment(app.getReviewComment())
			.appliedAt(app.getTimeData().getCreatedAt())
			.updatedAt(app.getTimeData().getUpdatedAt())
			.build();
	}

	@Transactional
	public void approveApplication(UUID adminId, UUID applicationId) {
		ManagerStoreApplication app = managerStoreApplicationRepository.findById(applicationId)
			.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));
		if (!"PENDING".equals(app.getStatus())) {
			throw new AdminException(AdminErrorCode.APPLICATION_ALREADY_PROCESSED);
		}

		//Store store = createStore(app);
		//createManager(store, app);

		app.setStatus("APPROVED");
		app.setReviewerAdminId(adminId);
		managerStoreApplicationRepository.save(app);
	}

	@Transactional
	public void rejectApplication(UUID adminId, UUID applicationId) {
		ManagerStoreApplication app = managerStoreApplicationRepository.findById(applicationId)
			.orElseThrow(() -> new AdminException(AdminErrorCode.APPLICATION_NOT_FOUND));
		if (!"PENDING".equals(app.getStatus())) {
			throw new AdminException(AdminErrorCode.APPLICATION_ALREADY_PROCESSED);
		}

		app.setStatus("REJECTED");
		app.setReviewerAdminId(adminId);
		managerStoreApplicationRepository.save(app);
	}

	private Manager createManager(UUID storeId, ManagerStoreApplication app) {
		Manager mgr = Manager.builder()
			.id(UUID.randomUUID())
			.email(app.getManagerEmail())
			.password(passwordEncoder.encode(app.getManagerPassword()))
			.name(app.getManagerName())
			.phoneNumber(app.getManagerPhoneNumber())
			.storeId(storeId)
			.build();

		return managerRepository.save(mgr);
	}
}
