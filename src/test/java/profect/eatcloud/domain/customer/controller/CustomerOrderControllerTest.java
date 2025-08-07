package profect.eatcloud.domain.customer.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import profect.eatcloud.Domain.Customer.Controller.CustomerOrderController;
import profect.eatcloud.common.ApiResponse;
import profect.eatcloud.domain.customer.dto.response.CustomerOrderResponse;
import profect.eatcloud.domain.customer.service.CustomerOrderService;
import profect.eatcloud.domain.globalCategory.entity.OrderStatusCode;
import profect.eatcloud.domain.globalCategory.entity.OrderTypeCode;
import profect.eatcloud.domain.order.dto.OrderMenu;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.service.OrderService;
import profect.eatcloud.security.SecurityUtil;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomerOrderControllerTest {

	@InjectMocks
	private CustomerOrderController customerOrderController;

	@Mock
	private CustomerOrderService customerOrderService;

	@Mock
	private OrderService orderService;

	private UserDetails userDetails;
	private UUID customerId;
	private MockedStatic<SecurityUtil> securityUtilMock;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		customerId = UUID.randomUUID();
		userDetails = User.builder()
			.username(customerId.toString())
			.password("password")
			.authorities(Collections.emptyList())
			.build();

		securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
		securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn(customerId.toString());
	}

	@AfterEach
	void tearDown() {
		securityUtilMock.close();
	}

	@Test
	void createOrder_success() {
		// given
		String orderTypeCode = "DELIVERY";
		Boolean usePoints = true;
		Integer pointsToUse = 1000;

		OrderStatusCode orderStatusCode = new OrderStatusCode();
		orderStatusCode.setCode("PENDING");
		orderStatusCode.setDisplayName("대기중");

		OrderTypeCode orderTypeCodeEntity = new OrderTypeCode();
		orderTypeCodeEntity.setCode("DELIVERY");
		orderTypeCodeEntity.setDisplayName("배달");

		OrderMenu menu1 = OrderMenu.builder()
			.menuId(UUID.randomUUID())
			.menuName("메뉴1")
			.quantity(2)
			.price(5000)
			.build();

		List<OrderMenu> orderMenus = List.of(menu1);

		Order mockOrder = Order.builder()
			.orderNumber("ORD-20250804-ABCDE")
			.orderMenuList(orderMenus)
			.customerId(customerId)
			.storeId(UUID.randomUUID())
			.orderStatusCode(orderStatusCode)
			.orderTypeCode(orderTypeCodeEntity)
			.totalPrice(10000)
			.usePoints(usePoints)
			.pointsToUse(pointsToUse)
			.finalPaymentAmount(9000)
			.build();

		when(customerOrderService.createOrder(customerId, orderTypeCode, usePoints, pointsToUse))
			.thenReturn(mockOrder);

		// when
		ApiResponse<CustomerOrderResponse> response = customerOrderController.createOrder(
			orderTypeCode, usePoints, pointsToUse);

		// then
		assertThat(response.getCode()).isEqualTo(200);
		assertThat(response.getData()).isNotNull();
		assertThat(response.getData().getOrderNumber()).isEqualTo("ORD-20250804-ABCDE");
		verify(customerOrderService).createOrder(customerId, orderTypeCode, usePoints, pointsToUse);
	}

	@Test
	void getMyOrders() {
		// given
		List<Order> mockOrders = List.of(new Order(), new Order());
		when(orderService.findOrdersByCustomer(customerId)).thenReturn(mockOrders);

		// when
		ApiResponse<List<Order>> response = customerOrderController.getMyOrders(userDetails);

		// then
		assertThat(response.getCode()).isEqualTo(200);
		assertThat(response.getData()).hasSize(2);
		verify(orderService).findOrdersByCustomer(customerId);
	}

	@Test
	void getMyOrder() {
		// given
		UUID orderId = UUID.randomUUID();
		Order mockOrder = new Order();
		when(orderService.findOrderByCustomerAndOrderId(customerId, orderId)).thenReturn(mockOrder);

		// when
		ApiResponse<Order> response = customerOrderController.getMyOrder(userDetails, orderId);

		// then
		assertThat(response.getCode()).isEqualTo(200);
		assertThat(response.getData()).isNotNull();
		verify(orderService).findOrderByCustomerAndOrderId(customerId, orderId);
	}
}