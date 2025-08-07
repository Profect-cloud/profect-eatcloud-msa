package profect.eatcloud.domain.customer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import profect.eatcloud.domain.customer.entity.Cart;
import profect.eatcloud.domain.customer.exception.CustomerErrorCode;
import profect.eatcloud.domain.customer.exception.CustomerException;
import profect.eatcloud.domain.customer.repository.CartRepository;
import profect.eatcloud.domain.globalCategory.repository.OrderStatusCodeRepository;
import profect.eatcloud.domain.globalCategory.repository.OrderTypeCodeRepository;
import profect.eatcloud.domain.order.dto.OrderMenu;
import profect.eatcloud.domain.order.entity.Order;
import profect.eatcloud.domain.order.repository.OrderRepository;

import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusCodeRepository orderStatusCodeRepository;
    private final OrderTypeCodeRepository orderTypeCodeRepository;

    @Transactional
    public Order createOrder(UUID customerId, String orderTypeCodeStr, Boolean usePoints, Integer pointsToUse) {
        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new CustomerException(CustomerErrorCode.CART_NOT_FOUND));

        if (cart.getCartItems().isEmpty()) {
            throw new CustomerException(CustomerErrorCode.EMPTY_CART);
        }

        UUID storeId = cart.getCartItems().get(0).getStoreId(); // 모든 아이템은 같은 storeId를 가져야 함

        // OrderMenu 리스트로 변환
        List<OrderMenu> orderMenuList = cart.getCartItems().stream()
            .map(item -> new OrderMenu(item.getMenuId(), item.getMenuName(), item.getQuantity(), item.getPrice()))
            .collect(Collectors.toList());

        // 주문번호 생성 (날짜 형식 변경)
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        String orderNumber = "ORD-" + date + "-" + randomPart;

        // 총 금액 계산
        Integer totalPrice = orderMenuList.stream()
            .mapToInt(menu -> menu.getPrice() * menu.getQuantity())
            .sum();

        Order newOrder = Order.builder()
            .orderNumber(orderNumber)
            .orderMenuList(orderMenuList)
            .customerId(customerId)
            .storeId(storeId)
            .orderStatusCode(orderStatusCodeRepository.findByCode("PENDING").orElseThrow()) // 기본 상태
            .orderTypeCode(orderTypeCodeRepository.findByCode(orderTypeCodeStr).orElseThrow(() ->
                new CustomerException(CustomerErrorCode.INVALID_ORDER_TYPE)))
            .totalPrice(totalPrice)
            .usePoints(usePoints != null ? usePoints : false)
            .pointsToUse(pointsToUse != null ? pointsToUse : 0)
            .finalPaymentAmount(totalPrice)
            .build();

        orderRepository.save(newOrder);

        // 장바구니 비우기
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return newOrder;
    }
}
