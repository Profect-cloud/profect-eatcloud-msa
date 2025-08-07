package profect.eatcloud.domain.store.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import profect.eatcloud.domain.store.entity.Menu;
import profect.eatcloud.domain.store.exception.MenuNotFoundException;
import profect.eatcloud.domain.store.service.MenuService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.nio.file.Paths.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @InjectMocks
    private MenuController menuController;

    @Mock
    private MenuService menuService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(menuController).build();
    }

    @Test
    void 메뉴단건조회_성공() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        Menu menu = Menu.builder()
                .id(menuId)
                .menuName("김치찌개")
                .price(new BigDecimal("9000"))
                .build();

        when(menuService.getMenuById(eq(storeId), eq(menuId))).thenReturn(menu);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stores/{storeId}/menus/{menuId}", storeId, menuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuName").value("김치찌개"))
                .andExpect(jsonPath("$.price").value(9000));
    }


    @Test
    void 메뉴단건조회_메뉴없음_404반환() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        when(menuService.getMenuById(eq(storeId), eq(menuId)))
                .thenThrow(new MenuNotFoundException("해당 메뉴를 찾을 수 없습니다."));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stores/{storeId}/menus/{menuId}", storeId, menuId))
                .andExpect(status().isNotFound());
    }

    @Test
    void 메뉴전체조회_성공() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();

        List<Menu> menuList = List.of(
                Menu.builder()
                        .id(UUID.randomUUID())
                        .menuName("김치찌개")
                        .price(new BigDecimal("9000"))
                        .build(),
                Menu.builder()
                        .id(UUID.randomUUID())
                        .menuName("된장찌개")
                        .price(new BigDecimal("8500"))
                        .build()
        );

        when(menuService.getMenusByStore(eq(storeId))).thenReturn(menuList);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/stores/{storeId}/menus", storeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].menuName").value("김치찌개"))
                .andExpect(jsonPath("$[1].menuName").value("된장찌개"));
    }


}


