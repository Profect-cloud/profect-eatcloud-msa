package profect.eatcloud.domain.manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import profect.eatcloud.domain.admin.entity.ManagerStoreApplication;
import profect.eatcloud.domain.admin.repository.ManagerStoreApplicationRepository;
import profect.eatcloud.domain.globalCategory.entity.StoreCategory;
import profect.eatcloud.domain.manager.dto.StoreRegisterRequestDto;
import profect.eatcloud.domain.manager.entity.Manager;
import profect.eatcloud.domain.manager.exception.ManagerErrorCode;
import profect.eatcloud.domain.manager.exception.ManagerException;
import profect.eatcloud.domain.manager.repository.ManagerRepository;
import profect.eatcloud.domain.store.dto.StoreRequestDto;
import profect.eatcloud.domain.store.entity.Store;
import profect.eatcloud.domain.store.exception.StoreErrorCode;
import profect.eatcloud.domain.store.exception.StoreException;
import profect.eatcloud.domain.store.repository.MenuRepository_min;
import profect.eatcloud.domain.store.repository.StoreRepository_min;

public class ManagerServiceTest {

	private ManagerService managerService;
	private StoreRepository_min storeRepository;
	private MenuRepository_min menuRepository;
	private ManagerRepository managerRepository;
	private ManagerStoreApplicationRepository applicationRepository;

	@BeforeEach
	void setUp() {
		storeRepository = mock(StoreRepository_min.class);
		menuRepository = mock(MenuRepository_min.class);
		managerRepository = mock(ManagerRepository.class);
		applicationRepository = mock(ManagerStoreApplicationRepository.class);

		managerService = new ManagerService(
			menuRepository,
			storeRepository,
			applicationRepository,
			managerRepository
		);
	}

	@Test
	void 가게정보_수정_정상작동() {
		UUID storeId = UUID.randomUUID();
		Store store = mock(Store.class);
		UUID categoryId = UUID.randomUUID(); // 반드시 categoryId 지정

		StoreRequestDto dto = StoreRequestDto.builder()
			.storeName("테스트 가게")
			.storeAddress("서울시 종로구")
			.phoneNumber("010-1234-5678")
			.minCost(10000)
			.description("설명")
			.storeLat(37.5665)
			.storeLon(126.9780)
			.openTime(LocalTime.of(9, 0))
			.closeTime(LocalTime.of(18, 0))
			.categoryId(categoryId) // 중요!
			.build();

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		managerService.updateStore(storeId, dto);

		verify(store).setStoreName(dto.getStoreName());
		verify(store).setStoreAddress(dto.getStoreAddress());
		verify(store).setPhoneNumber(dto.getPhoneNumber());
		verify(store).setMinCost(dto.getMinCost());
		verify(store).setDescription(dto.getDescription());
		verify(store).setStoreLat(dto.getStoreLat());
		verify(store).setStoreLon(dto.getStoreLon());
		verify(store).setOpenTime(dto.getOpenTime());
		verify(store).setCloseTime(dto.getCloseTime());
	}

	@Test
	void 가게정보_수정_실패_가게없음() {
		UUID storeId = UUID.randomUUID();
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		StoreException e = assertThrows(StoreException.class, () -> {
			managerService.updateStore(storeId, new StoreRequestDto());
		});

		assertEquals(StoreErrorCode.STORE_NOT_FOUND, e.getErrorCode());
	}

	@Test
	void 가게등록요청_정상작동() {
		UUID managerId = UUID.randomUUID();
		Manager manager = Manager.builder()
			.name("사장님")
			.email("boss@example.com")
			.phoneNumber("010-1234-5678")
			.build();

		StoreRegisterRequestDto dto = new StoreRegisterRequestDto();
		dto.setStoreName("맛집");
		dto.setStoreAddress("서울시 어딘가");
		dto.setStorePhoneNumber("02-123-4567");
		dto.setCategoryId(UUID.randomUUID());
		dto.setDescription("한식 전문점");

		when(managerRepository.findById(managerId)).thenReturn(Optional.of(manager));

		managerService.requestStoreRegistration(managerId, dto);

		ArgumentCaptor<ManagerStoreApplication> captor = ArgumentCaptor.forClass(ManagerStoreApplication.class);
		verify(applicationRepository).save(captor.capture());
		assertEquals("맛집", captor.getValue().getStoreName());
		assertEquals("PENDING", captor.getValue().getStatus());
	}

	@Test
	void 가게등록요청_실패_사장님없음() {
		UUID managerId = UUID.randomUUID();
		when(managerRepository.findById(managerId)).thenReturn(Optional.empty());

		ManagerException e = assertThrows(ManagerException.class, () -> {
			managerService.requestStoreRegistration(managerId, new StoreRegisterRequestDto());
		});

		assertEquals(ManagerErrorCode.MANAGER_NOT_FOUND, e.getErrorCode());
	}

	@Test
	void 폐업요청_정상작동() {
		UUID managerId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();

		Manager manager = Manager.builder()
			.name("사장님")
			.email("boss@example.com")
			.phoneNumber("010-1111-2222")
			.build();

		Store store = Store.builder()
			.storeId(storeId)
			.storeName("가게")
			.phoneNumber("02-0000-1111")
			.storeCategory(StoreCategory.builder().categoryId(UUID.randomUUID()).build())
			.build();

		when(managerRepository.findById(managerId)).thenReturn(Optional.of(manager));
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		managerService.requestStoreClosure(managerId, storeId);

		ArgumentCaptor<ManagerStoreApplication> captor = ArgumentCaptor.forClass(ManagerStoreApplication.class);
		verify(applicationRepository).save(captor.capture());
		assertEquals("CLOSURE_REQUESTED", captor.getValue().getStatus());
	}

	@Test
	void 폐업요청_실패_사장님없음() {
		UUID managerId = UUID.randomUUID();
		when(managerRepository.findById(managerId)).thenReturn(Optional.empty());

		ManagerException e = assertThrows(ManagerException.class, () -> {
			managerService.requestStoreClosure(managerId, UUID.randomUUID());
		});

		assertEquals(ManagerErrorCode.MANAGER_NOT_FOUND, e.getErrorCode());
	}

	@Test
	void 폐업요청_실패_가게없음() {
		UUID managerId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		when(managerRepository.findById(managerId)).thenReturn(Optional.of(new Manager()));
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		StoreException e = assertThrows(StoreException.class, () -> {
			managerService.requestStoreClosure(managerId, storeId);
		});

		assertEquals(StoreErrorCode.STORE_NOT_FOUND, e.getErrorCode());
	}
}
