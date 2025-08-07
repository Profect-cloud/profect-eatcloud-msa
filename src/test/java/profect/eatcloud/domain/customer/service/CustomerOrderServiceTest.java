package profect.eatcloud.domain.customer.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import profect.eatcloud.domain.customer.entity.Cart;
import profect.eatcloud.domain.customer.exception.CustomerException;
import profect.eatcloud.domain.customer.repository.CartRepository;
import profect.eatcloud.domain.globalCategory.entity.OrderStatusCode;
import profect.eatcloud.domain.globalCategory.entity.OrderTypeCode;
import profect.eatcloud.domain.globalCategory.repository.OrderStatusCodeRepository;
import profect.eatcloud.domain.globalCategory.repository.OrderTypeCodeRepository;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class CustomerOrderServiceTest {

    @InjectMocks
    private CustomerOrderService customerOrderService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusCodeRepository orderStatusCodeRepository;

    @Mock
    private OrderTypeCodeRepository orderTypeCodeRepository;

    private UUID customerId;
    private Cart cart;
    private OrderStatusCode orderStatusPending;
    private OrderTypeCode orderTypeCode;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customerId = UUID.randomUUID();

        var cartItem1 = profect.eatcloud.domain.customer.dto.CartItem.builder()
                .menuId(UUID.randomUUID())
                .menuName("Test Menu 1")
                .quantity(2)
                .price(5000)
                .storeId(UUID.randomUUID())
                .build();

        var cartItem2 = profect.eatcloud.domain.customer.dto.CartItem.builder()
                .menuId(UUID.randomUUID())
                .menuName("Test Menu 2")
                .quantity(1)
                .price(10000)
                .storeId(cartItem1.getStoreId()) // 동일 매장 ID
                .build();

        cart = Cart.builder()
                .cartId(UUID.randomUUID())
                .cartItems(new ArrayList<>(List.of(cartItem1, cartItem2)))
                .build();

        orderStatusPending = OrderStatusCode.builder().code("PENDING").build();
        orderTypeCode = OrderTypeCode.builder().code("TAKEOUT").build();
    }

    @Test
    void createOrder_Success() {
        // given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(orderStatusCodeRepository.findByCode("PENDING")).thenReturn(Optional.of(orderStatusPending));
        when(orderTypeCodeRepository.findByCode("TAKEOUT")).thenReturn(Optional.of(orderTypeCode));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Order result = customerOrderService.createOrder(customerId, "TAKEOUT", true, 1000);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getOrderMenuList()).hasSize(2);
        assertThat(result.getOrderStatusCode().getCode()).isEqualTo("PENDING");
        assertThat(result.getOrderTypeCode().getCode()).isEqualTo("TAKEOUT");
        assertThat(result.getUsePoints()).isTrue();
        assertThat(result.getPointsToUse()).isEqualTo(1000);

        int expectedTotal = 2 * 5000 + 1 * 10000;
        assertThat(result.getTotalPrice()).isEqualTo(expectedTotal);
        assertThat(result.getFinalPaymentAmount()).isEqualTo(expectedTotal);

        assertThat(cart.getCartItems()).isEmpty();

        verify(cartRepository).save(cart);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_EmptyCart_Throws() {
        // given
        cart.setCartItems(new ArrayList<>());
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));

        // when, then
        assertThatThrownBy(() -> customerOrderService.createOrder(customerId, "TAKEOUT", false, 0))
                .isInstanceOf(CustomerException.class)
                .hasMessage("장바구니가 비어 있습니다");
    }

    @Test
    void createOrder_CartNotFound_Throws() {
        // given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> customerOrderService.createOrder(customerId, "TAKEOUT", false, 0))
                .isInstanceOf(CustomerException.class)
                .hasMessage("장바구니를 찾을 수 없습니다");
    }

    @Test
    void createOrder_InvalidOrderTypeCode_Throws() {
        // given
        when(cartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(cart));
        when(orderStatusCodeRepository.findByCode("PENDING")).thenReturn(Optional.of(orderStatusPending));
        when(orderTypeCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> customerOrderService.createOrder(customerId, "INVALID", false, 0))
                .isInstanceOf(CustomerException.class)
                .hasMessage("유효하지 않은 주문 타입 코드입니다");
    }
}