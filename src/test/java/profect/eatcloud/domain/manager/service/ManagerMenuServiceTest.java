package profect.eatcloud.domain.manager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import profect.eatcloud.domain.admin.repository.ManagerStoreApplicationRepository;
import profect.eatcloud.domain.manager.repository.ManagerRepository;
import profect.eatcloud.domain.store.dto.MenuRequestDto;
import profect.eatcloud.domain.store.entity.Menu;
import profect.eatcloud.domain.store.entity.Store;
import profect.eatcloud.domain.store.exception.MenuException;
import profect.eatcloud.domain.store.exception.StoreException;
import profect.eatcloud.domain.store.repository.MenuRepository_min;
import profect.eatcloud.domain.store.repository.StoreRepository_min;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


class ManagerMenuServiceTest {

    private ManagerService managerService;
    private MenuRepository_min menuRepository;
    private StoreRepository_min storeRepository;
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
    void 메뉴가_정상적으로_등록된다() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuNum(1);
        dto.setMenuName("테스트메뉴");
        dto.setMenuCategoryCode("KOREAN");
        dto.setPrice(new BigDecimal("12000"));
        dto.setDescription("맛있는 한식");
        dto.setIsAvailable(true);
        dto.setImageUrl("image-url");

        // when
        managerService.createMenu(storeId, dto);

        // then
        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository, times(1)).save(captor.capture());

        Menu savedMenu = captor.getValue();
        assertEquals("테스트메뉴", savedMenu.getMenuName());
        assertEquals("KOREAN", savedMenu.getMenuCategoryCode());
    }

    @Test
    void 존재하지_않는_가게일_경우_예외가_발생한다() {
        // given
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        MenuRequestDto dto = new MenuRequestDto();

        // when + then
        StoreException exception = assertThrows(StoreException.class, () -> {
            managerService.createMenu(storeId, dto);
        });

        assertEquals("해당 매장을 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 가격이_음수일_경우_예외가_발생한다() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName("제육덮밥");
        dto.setPrice(new BigDecimal("-1000"));

        // when + then
        MenuException exception = assertThrows(MenuException.class, () -> {
            managerService.createMenu(storeId, dto);
        });

        assertEquals("가격은 0 이상이어야 합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("메뉴 이름이 비어 있으면 예외가 발생한다")
    void 메뉴이름이_없으면_예외발생() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName(""); // 비어 있는 이름
        dto.setMenuCategoryCode("KOREAN");
        dto.setPrice(new BigDecimal("12000"));
        dto.setIsAvailable(true);

        // when & then
        MenuException exception = assertThrows(MenuException.class, () -> {
            managerService.createMenu(storeId, dto);
        });

        assertEquals("메뉴 이름은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("정상적인 메뉴 저장 시 모든 필드가 올바르게 저장된다")
    void 정상메뉴저장_모든필드검증() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuNum(10);
        dto.setMenuName("삼겹살 정식");
        dto.setMenuCategoryCode("KOREAN");
        dto.setPrice(new BigDecimal("13000"));
        dto.setDescription("고소한 삼겹살과 밥, 반찬 구성");
        dto.setIsAvailable(null); // 명시적 null
        dto.setImageUrl("image-url.png");

        // when
        managerService.createMenu(storeId, dto);

        // then
        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());

        Menu saved = captor.getValue();
        assertEquals(10, saved.getMenuNum());
        assertEquals("삼겹살 정식", saved.getMenuName());
        assertEquals("KOREAN", saved.getMenuCategoryCode());
        assertEquals(new BigDecimal("13000"), saved.getPrice());
        assertEquals("고소한 삼겹살과 밥, 반찬 구성", saved.getDescription());
        assertEquals("image-url.png", saved.getImageUrl());
        assertTrue(saved.getIsAvailable()); // 기본값 true 적용 확인
    }

    @Test
    void 같은가게에서_중복된_메뉴번호로_등록하면_예외가_발생한다() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.existsByStoreAndMenuNum(store, 1)).thenReturn(true); // 중복된 번호

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuNum(1);
        dto.setMenuName("중복메뉴");
        dto.setPrice(new BigDecimal("10000"));

        // when + then
        MenuException e = assertThrows(MenuException.class, () -> {
            managerService.createMenu(storeId, dto);
        });

        assertEquals("해당 메뉴 번호는 이미 존재합니다.", e.getMessage());
    }

    @Test
    void 존재하지_않는_메뉴를_삭제하면_예외발생() {
        // given
        UUID menuId = UUID.randomUUID();
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        // when & then
        MenuException e = assertThrows(MenuException.class, () -> {
            managerService.deleteMenu(menuId);
        });

        assertEquals("해당 메뉴를 찾을 수 없습니다.", e.getMessage());
    }

    @Test
    void 메뉴_삭제가_정상적으로_동작한다() {
        // given
        UUID menuId = UUID.randomUUID();
        Menu menu = Menu.builder().id(menuId).build();
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        // when
        managerService.deleteMenu(menuId);

        // then
        verify(menuRepository, times(1)).deleteById(menuId);
    }

    @Test
    void 존재하지_않는_메뉴일_경우_예외_발생() {
        // given
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        MenuRequestDto dto = new MenuRequestDto();

        // when + then
        MenuException e = assertThrows(MenuException.class, () -> {
            managerService.updateMenu(storeId, menuId, dto);
        });

        assertEquals("해당 메뉴를 찾을 수 없습니다.", e.getMessage());
    }

    @Test
    void 메뉴_수정이_정상적으로_동작한다() {
        // given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();

        Menu existingMenu = Menu.builder()
                .id(menuId)
                .store(store)
                .menuName("기존메뉴")
                .price(BigDecimal.valueOf(10000))
                .menuNum(1)
                .isAvailable(true)
                .build();

        MenuRequestDto updateDto = new MenuRequestDto();
        updateDto.setMenuName("업데이트된메뉴");
        updateDto.setPrice(BigDecimal.valueOf(15000));
        updateDto.setIsAvailable(false);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(existingMenu));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when
        managerService.updateMenu(storeId, menuId, updateDto);

        // then
        assertEquals("업데이트된메뉴", existingMenu.getMenuName());
        assertEquals(BigDecimal.valueOf(15000), existingMenu.getPrice());
        assertFalse(existingMenu.getIsAvailable());
    }

    @Test
    void 메뉴_이름이_null이면_예외() {
        // given
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        Menu menu = Menu.builder().id(menuId).store(store).build();

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName(null);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when + then
        MenuException e = assertThrows(MenuException.class, () -> {
            managerService.updateMenu(storeId, menuId, dto);
        });

        assertEquals("메뉴 이름은 필수입니다.", e.getMessage());
    }

    // 더 추가할 수 있는 테스트 항목들:
    // - 가격이 음수면 예외
    @Test
    void 가격이_음수면_예외발생() {
        // given
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        Menu menu = Menu.builder().id(menuId).store(store).build();

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName("된장찌개");
        dto.setPrice(new BigDecimal("-1000")); // 음수

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // when + then
        MenuException e = assertThrows(MenuException.class, () -> {
            managerService.updateMenu(storeId, menuId, dto);
        });

        assertEquals("가격은 0 이상이어야 합니다.", e.getMessage());
    }

    // - menuNum 변경 시 중복 검사 (정책 적용 시)
    @Test
    void menuNum_중복되면_예외발생() {
        // given
        UUID menuId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        Menu menu = Menu.builder().id(menuId).store(store).menuNum(1).build();

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName("비빔밥");
        dto.setPrice(BigDecimal.valueOf(10000));
        dto.setMenuNum(2); // menuNum 변경

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.existsByStoreAndMenuNum(store, 2)).thenReturn(true); // 이미 존재하는 번호

        // when + then
        MenuException e = assertThrows(MenuException.class, () -> {
            managerService.updateMenu(storeId, menuId, dto);
        });

        assertEquals("해당 메뉴 번호는 이미 존재합니다.", e.getMessage());
    }


    @Test
    void 전체_필드_수정_정상작동() {
        // given
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        Store store = Store.builder().storeId(storeId).build();
        Menu menu = Menu.builder()
                .id(menuId)
                .store(store)
                .menuName("기존이름")
                .menuNum(1)
                .menuCategoryCode("KOREAN")
                .price(BigDecimal.valueOf(10000))
                .description("기존 설명")
                .isAvailable(true)
                .imageUrl("old-url.png")
                .build();

        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName("신규이름");
        dto.setMenuNum(2);
        dto.setMenuCategoryCode("CHINESE");
        dto.setPrice(BigDecimal.valueOf(14000));
        dto.setDescription("신규 설명");
        dto.setImageUrl("new-url.jpg");
        dto.setIsAvailable(false);

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(menuRepository.existsByStoreAndMenuNum(store, 2)).thenReturn(false);

        // when
        managerService.updateMenu(storeId, menuId, dto);

        // then
        assertEquals("신규이름", menu.getMenuName());
        assertEquals(2, menu.getMenuNum());
        assertEquals("CHINESE", menu.getMenuCategoryCode());
        assertEquals(BigDecimal.valueOf(14000), menu.getPrice());
        assertEquals("신규 설명", menu.getDescription());
        assertEquals("new-url.jpg", menu.getImageUrl());
        assertFalse(menu.getIsAvailable());
    }






}
