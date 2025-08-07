package profect.eatcloud.domain.customer.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import profect.eatcloud.common.ApiResponse;
import profect.eatcloud.domain.customer.dto.CartItem;
import profect.eatcloud.domain.customer.dto.request.AddCartItemRequest;
import profect.eatcloud.domain.customer.dto.request.UpdateCartItemRequest;
import profect.eatcloud.domain.customer.message.ResponseMessage;
import profect.eatcloud.domain.customer.service.CartService;
import profect.eatcloud.security.SecurityUtil;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Mock
    private UserDetails userDetails;

    private UUID customerId;
    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn(customerId.toString());

        when(userDetails.getUsername()).thenReturn(customerId.toString());
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("장바구니 항목 추가 성공")
    void addItem_success() {
        AddCartItemRequest request = new AddCartItemRequest(
            UUID.randomUUID(),
            "테스트메뉴",
            3,
            10000,
            UUID.randomUUID()
        );

        ApiResponse<ResponseMessage> response = cartController.addItem(userDetails, request);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(ResponseMessage.CART_ADD_SUCCESS);
        verify(cartService, times(1)).addItem(customerId, request);
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    void getCart_success() {
        List<CartItem> mockCartItems = List.of(
            CartItem.builder()
                .menuId(UUID.randomUUID())
                .menuName("메뉴1")
                .quantity(2)
                .price(15000)
                .storeId(UUID.randomUUID())
                .build()
        );

        when(cartService.getCart(customerId)).thenReturn(mockCartItems);

        ApiResponse<List<CartItem>> response = cartController.getCart(userDetails);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(mockCartItems);
        verify(cartService, times(1)).getCart(customerId);
    }

    @Test
    @DisplayName("장바구니 메뉴 수량 변경 성공")
    void updateQuantity_success() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(
            UUID.randomUUID(),
            5
        );

        ApiResponse<ResponseMessage> response = cartController.updateQuantity(userDetails, request);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(ResponseMessage.CART_UPDATE_SUCCESS);
        verify(cartService, times(1)).updateItemQuantity(customerId, request);
    }

    @Test
    @DisplayName("장바구니 개별 메뉴 삭제 성공")
    void removeItem_success() {
        UUID menuId = UUID.randomUUID();

        ApiResponse<ResponseMessage> response = cartController.removeItem(userDetails, menuId);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(ResponseMessage.CART_ITEM_DELETE_SUCCESS);
        verify(cartService, times(1)).removeItem(customerId, menuId);
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    void clearCart_success() {
        ApiResponse<ResponseMessage> response = cartController.clearCart(userDetails);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isEqualTo(ResponseMessage.CART_CLEAR_SUCCESS);
        verify(cartService, times(1)).clearCart(customerId);
    }
}