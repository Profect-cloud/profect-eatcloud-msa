package profect.eatcloud.domain.admin.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import profect.eatcloud.domain.admin.dto.ManagerStoreApplicationDetailDto;
import profect.eatcloud.domain.admin.dto.ManagerStoreApplicationSummaryDto;
import profect.eatcloud.domain.admin.entity.ManagerStoreApplication;
import profect.eatcloud.domain.admin.exception.AdminErrorCode;
import profect.eatcloud.domain.admin.exception.AdminException;
import profect.eatcloud.domain.admin.repository.ManagerStoreApplicationRepository;
import profect.eatcloud.domain.globalCategory.entity.StoreCategory;
import profect.eatcloud.domain.globalCategory.repository.StoreCategoryRepository;
import profect.eatcloud.domain.manager.entity.Manager;
import profect.eatcloud.domain.manager.repository.ManagerRepository;
import profect.eatcloud.domain.store.entity.Store;
import profect.eatcloud.domain.store.repository.StoreRepository_hong;

@Service
@AllArgsConstructor
public class AdminAssignService {

	private final ManagerStoreApplicationRepository managerStoreApplicationRepository;
	private final ManagerRepository managerRepository;
	private final StoreRepository_hong storeRepository;
	private final StoreCategoryRepository categoryRepository;
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

		Store store = createStore(app);
		createManager(store, app);

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

		// 3) 상태 변경
		app.setStatus("REJECTED");
		app.setReviewerAdminId(adminId);
		managerStoreApplicationRepository.save(app);
	}

	private Store createStore(ManagerStoreApplication app) {
		StoreCategory storeCategory = null;
		if (app.getCategoryId() != null) {
			storeCategory = categoryRepository.findById(app.getCategoryId())
				.orElseThrow(() -> new AdminException(AdminErrorCode.CATEGORY_NOT_FOUND));
		}

		Store store = Store.builder()
			.storeId(UUID.randomUUID())
			.storeName(app.getStoreName())
			.storeAddress(app.getStoreAddress())
			.phoneNumber(app.getStorePhoneNumber())
			.storeCategory(storeCategory)
			.minCost(0)
			.description(app.getDescription())
			.openStatus(false)
			.openTime(null)
			.closeTime(null)
			.build();

		return storeRepository.save(store);
	}

	private Manager createManager(Store store, ManagerStoreApplication app) {
		Manager mgr = Manager.builder()
			.id(UUID.randomUUID())
			.email(app.getManagerEmail())
			.password(passwordEncoder.encode(app.getManagerPassword()))
			.name(app.getManagerName())
			.phoneNumber(app.getManagerPhoneNumber())
			.store(store)
			.build();

		return managerRepository.save(mgr);
	}
}
