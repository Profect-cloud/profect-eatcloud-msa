package profect.eatcloud.domain.customer.dto.response;

import lombok.Data;
import lombok.Getter;
import profect.eatcloud.domain.order.dto.OrderMenu;
import profect.eatcloud.domain.order.entity.Order;

import java.util.List;
import java.util.UUID;

@Data
@Getter
public class CustomerOrderResponse {
    private String orderNumber;
    private List<OrderMenu> orderMenuList;
    private Integer totalPrice;
    private Boolean usePoints;
    private Integer pointsToUse;
    private Integer finalPaymentAmount;
    private UUID customerId;
    private UUID storeId;
    private String orderStatus;
    private String orderType;

    public CustomerOrderResponse(Order order) {
        this.orderNumber = order.getOrderNumber();
        this.orderMenuList = order.getOrderMenuList();
        
        // 총 가격 계산
        this.totalPrice = order.getOrderMenuList().stream()
                .mapToInt(menu -> menu.getPrice() * menu.getQuantity())
                .sum();
        
        // 포인트 사용 관련 (기본값 설정)
        this.usePoints = false;
        this.pointsToUse = 0;
        this.finalPaymentAmount = this.totalPrice;
        
        this.customerId = order.getCustomerId();
        this.storeId = order.getStoreId();
        this.orderStatus = order.getOrderStatusCode().getDisplayName();
        this.orderType = order.getOrderTypeCode().getDisplayName();
    }
    
    public CustomerOrderResponse(Order order, Boolean usePoints, Integer pointsToUse) {
        this(order);
        this.usePoints = usePoints;
        this.pointsToUse = pointsToUse != null ? pointsToUse : 0;
        this.finalPaymentAmount = this.totalPrice - this.pointsToUse;
    }
}
