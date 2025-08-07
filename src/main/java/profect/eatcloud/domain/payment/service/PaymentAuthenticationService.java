package profect.eatcloud.domain.payment.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.security.AuthenticationHelper;

import java.util.UUID;

@Service
public class PaymentAuthenticationService {
    
    private final AuthenticationHelper authenticationHelper;

    public PaymentAuthenticationService(AuthenticationHelper authenticationHelper) {
        this.authenticationHelper = authenticationHelper;
    }

    public PaymentAuthResult validateCustomerForPayment(String requestCustomerId) {
        try {
            Customer currentCustomer = authenticationHelper.getCurrentCustomer();

            if (!authenticationHelper.validateCustomerId(requestCustomerId)) {
                return PaymentAuthResult.failure("인증된 고객 정보와 주문 정보가 일치하지 않습니다.");
            }
            
            return PaymentAuthResult.success(currentCustomer, "인증 성공");
            
        } catch (AuthenticationHelper.AuthenticationException e) {
            return PaymentAuthResult.failure("인증되지 않은 고객입니다: " + e.getMessage());
        }
    }

    public PaymentAuthResult validateCustomerForOrderPage() {
        try {
            Customer currentCustomer = authenticationHelper.getCurrentCustomer();
            return PaymentAuthResult.success(currentCustomer, "인증된 고객");
        } catch (AuthenticationHelper.AuthenticationException e) {
            return PaymentAuthResult.failure("인가되지 않은 고객입니다: " + e.getMessage());
        }
    }

    public PaymentAuthResult validateCustomerForPointCharge() {
        try {
            Customer currentCustomer = authenticationHelper.getCurrentCustomer();
            return PaymentAuthResult.success(currentCustomer, "포인트 충전 인증 성공");
        } catch (AuthenticationHelper.AuthenticationException e) {
            return PaymentAuthResult.failure("포인트 충전을 위해서는 로그인이 필요합니다: " + e.getMessage());
        }
    }

    @Getter
    public static class PaymentAuthResult {
        private final boolean success;
        private final Customer customer;
        private final String message;
        private final String errorMessage;
        
        private PaymentAuthResult(boolean success, Customer customer, String message, String errorMessage) {
            this.success = success;
            this.customer = customer;
            this.message = message;
            this.errorMessage = errorMessage;
        }
        
        public static PaymentAuthResult success(Customer customer, String message) {
            return new PaymentAuthResult(true, customer, message, null);
        }
        
        public static PaymentAuthResult failure(String errorMessage) {
            return new PaymentAuthResult(false, null, null, errorMessage);
        }

        public UUID getCustomerId() {
            return customer != null ? customer.getId() : null;
        }
        
        public String getCustomerIdAsString() {
            UUID customerId = getCustomerId();
            return customerId != null ? customerId.toString() : null;
        }
        
        public String getCustomerName() {
            return customer != null ? customer.getName() : "NULL";
        }
        
        public Integer getCustomerPoints() {
            return customer != null ? (customer.getPoints() != null ? customer.getPoints() : 0) : 0;
        }
    }
}
