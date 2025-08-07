package profect.eatcloud.domain.payment.repository;

import org.springframework.stereotype.Repository;
import profect.eatcloud.domain.payment.entity.Payment;
import profect.eatcloud.global.timeData.BaseTimeRepository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends BaseTimeRepository<Payment, UUID> {
    
}