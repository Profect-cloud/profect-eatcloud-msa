package profect.eatcloud.domain.payment.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.domain.customer.repository.CustomerRepository;
import java.util.UUID;
import java.util.Optional;

@Service
public class PointService {

    private final CustomerRepository customerRepository;

    public PointService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public boolean canUsePoints(UUID customerId, Integer pointsToUse) {
        if (customerId == null || pointsToUse == null || pointsToUse <= 0) {
            return false;
        }
        
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            return false;
        }

        Integer currentPoints = customer.get().getPoints();
        return currentPoints != null && currentPoints >= pointsToUse;
    }

    public PointResult usePoints(UUID customerId, Integer pointsToUse) {
        if (customerId == null || pointsToUse == null || pointsToUse <= 0) {
            return PointResult.fail("유효하지 않은 포인트 사용 요청입니다");
        }
        
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return PointResult.fail("고객을 찾을 수 없습니다");
        }

        Customer customer = customerOpt.get();
        Integer currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;

        if (currentPoints < pointsToUse) {
            return PointResult.fail("포인트가 부족합니다. 보유: " + currentPoints + ", 사용요청: " + pointsToUse);
        }

        customer.setPoints(currentPoints - pointsToUse);
        customerRepository.save(customer);

        return PointResult.success(pointsToUse, currentPoints - pointsToUse);
    }

    public PointResult refundPoints(UUID customerId, Integer pointsToRefund) {
        if (customerId == null || pointsToRefund == null || pointsToRefund <= 0) {
            return PointResult.fail("유효하지 않은 포인트 환불 요청입니다");
        }
        
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return PointResult.fail("고객을 찾을 수 없습니다");
        }

        Customer customer = customerOpt.get();
        Integer currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;

        customer.setPoints(currentPoints + pointsToRefund);
        customerRepository.save(customer);

        return PointResult.success(pointsToRefund, currentPoints + pointsToRefund);
    }

    @Getter
    public static class PointResult {
        private final boolean success;
        private final String errorMessage;
        private final Integer usedPoints;
        private final Integer remainingPoints;

        private PointResult(boolean success, String errorMessage, Integer usedPoints, Integer remainingPoints) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.usedPoints = usedPoints;
            this.remainingPoints = remainingPoints;
        }

        public static PointResult success(Integer usedPoints, Integer remainingPoints) {
            return new PointResult(true, null, usedPoints, remainingPoints);
        }

        public static PointResult fail(String errorMessage) {
            return new PointResult(false, errorMessage, 0, 0);
        }
    }
}