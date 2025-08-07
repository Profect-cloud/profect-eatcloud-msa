package profect.eatcloud.domain.manager.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import profect.eatcloud.domain.admin.entity.ManagerStoreApplication;
import profect.eatcloud.domain.admin.repository.ManagerStoreApplicationRepository;
import profect.eatcloud.domain.manager.dto.StoreRegisterRequestDto;
import profect.eatcloud.domain.manager.entity.Manager;
import profect.eatcloud.domain.manager.exception.ManagerErrorCode;
import profect.eatcloud.domain.manager.exception.ManagerException;
import profect.eatcloud.domain.manager.repository.ManagerRepository;
import profect.eatcloud.domain.store.dto.MenuRequestDto;
import profect.eatcloud.domain.store.dto.StoreRequestDto;
import profect.eatcloud.domain.store.entity.Menu;
import profect.eatcloud.domain.store.entity.Store;
import profect.eatcloud.domain.store.exception.MenuErrorCode;
import profect.eatcloud.domain.store.exception.MenuException;
import profect.eatcloud.domain.store.exception.StoreErrorCode;
import profect.eatcloud.domain.store.exception.StoreException;
import profect.eatcloud.domain.store.repository.MenuRepository_min;
import profect.eatcloud.domain.store.repository.StoreRepository_min;

@Service
public class ManagerService {

	private final MenuRepository_min menuRepository;
	private final StoreRepository_min storeRepository;
	private final ManagerStoreApplicationRepository applicationRepository;
	private final ManagerRepository managerRepository;

	@Autowired
	public ManagerService(MenuRepository_min menuRepository, StoreRepository_min storeRepository,
		ManagerStoreApplicationRepository applicationRepository, ManagerRepository managerRepository) {
		this.menuRepository = menuRepository;
		this.storeRepository = storeRepository;
		this.applicationRepository = applicationRepository;
		this.managerRepository = managerRepository;
	}

	public Menu createMenu(UUID storeId, MenuRequestDto dto) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

		if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			throw new MenuException(MenuErrorCode.INVALID_MENU_PRICE);
		}

		if (dto.getMenuName() == null || dto.getMenuName().trim().isEmpty()) {
			throw new MenuException(MenuErrorCode.MENU_NAME_REQUIRED);
		}

		Boolean isAvailable = dto.getIsAvailable();
		if (isAvailable == null) {
			isAvailable = true;
		}

		if (menuRepository.existsByStoreAndMenuNum(store, dto.getMenuNum())) {
			throw new MenuException(MenuErrorCode.DUPLICATE_MENU_NUM);
		}

		Menu menu = Menu.builder()
			.store(store)
			.menuNum(dto.getMenuNum())
			.menuName(dto.getMenuName())
			.menuCategoryCode(dto.getMenuCategoryCode())
			.price(dto.getPrice())
			.description(dto.getDescription())
			.isAvailable(isAvailable)
			.imageUrl(dto.getImageUrl())
			.build();

		return menuRepository.save(menu);
	}

	public Menu updateMenu(UUID storeId, UUID menuId, MenuRequestDto dto) {
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

		if (dto.getMenuName() == null || dto.getMenuName().trim().isEmpty()) {
			throw new MenuException(MenuErrorCode.MENU_NAME_REQUIRED);
		}

		if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			throw new MenuException(MenuErrorCode.INVALID_MENU_PRICE);
		}

		if (dto.getMenuNum() != menu.getMenuNum()) {
			boolean exists = menuRepository.existsByStoreAndMenuNum(menu.getStore(), dto.getMenuNum());
			if (exists) {
				throw new MenuException(MenuErrorCode.DUPLICATE_MENU_NUM);
			}
		}

		// updateFrom 제거하고 수동으로 반영
		menu.setMenuNum(dto.getMenuNum());
		menu.setMenuName(dto.getMenuName());
		menu.setMenuCategoryCode(dto.getMenuCategoryCode());
		menu.setPrice(dto.getPrice());
		menu.setDescription(dto.getDescription());
		menu.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
		menu.setImageUrl(dto.getImageUrl());

		return menuRepository.save(menu);
	}

	@Transactional
	public void deleteMenu(UUID menuId) {
		menuRepository.findById(menuId)
			.orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

		menuRepository.deleteById(menuId);
	}

	public void updateStore(UUID storeId, StoreRequestDto dto) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

		if (dto.getStoreName() != null)
			store.setStoreName(dto.getStoreName());
		if (dto.getStoreAddress() != null)
			store.setStoreAddress(dto.getStoreAddress());
		if (dto.getPhoneNumber() != null)
			store.setPhoneNumber(dto.getPhoneNumber());
		if (dto.getMinCost() != null)
			store.setMinCost(dto.getMinCost());
		if (dto.getDescription() != null)
			store.setDescription(dto.getDescription());
		if (dto.getStoreLat() != null)
			store.setStoreLat(dto.getStoreLat());
		if (dto.getStoreLon() != null)
			store.setStoreLon(dto.getStoreLon());
		if (dto.getOpenTime() != null)
			store.setOpenTime(dto.getOpenTime());
		if (dto.getCloseTime() != null)
			store.setCloseTime(dto.getCloseTime());
	}

	@Transactional
	public void requestStoreRegistration(UUID managerId, StoreRegisterRequestDto dto) {

		Manager manager = managerRepository.findById(managerId)
			.orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

		ManagerStoreApplication application = ManagerStoreApplication.builder()
			.applicationId(UUID.randomUUID())
			.managerName(manager.getName())
			.managerEmail(manager.getEmail())
			.managerPhoneNumber(manager.getPhoneNumber())
			.storeName(dto.getStoreName())
			.storeAddress(dto.getStoreAddress())
			.storePhoneNumber(dto.getStorePhoneNumber())
			.categoryId(dto.getCategoryId())
			.description(dto.getDescription())
			.status("PENDING")
			.build();

		applicationRepository.save(application);
	}

	@Transactional
	public void requestStoreClosure(UUID managerId, UUID storeId) {
		Manager manager = managerRepository.findById(managerId)
			.orElseThrow(() -> new ManagerException(ManagerErrorCode.MANAGER_NOT_FOUND));

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

		ManagerStoreApplication application = ManagerStoreApplication.builder()
			.applicationId(UUID.randomUUID())
			.managerName(manager.getName())
			.managerEmail(manager.getEmail())
			.managerPhoneNumber(manager.getPhoneNumber())
			.storeName(store.getStoreName())
			.storePhoneNumber(store.getPhoneNumber())
			.categoryId(store.getStoreCategory().getId())
			.description("폐업 요청")
			.status("CLOSURE_REQUESTED")
			.build();

		applicationRepository.save(application);
	}

}
