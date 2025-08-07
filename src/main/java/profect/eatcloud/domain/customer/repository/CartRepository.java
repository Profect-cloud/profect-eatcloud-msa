package profect.eatcloud.domain.customer.repository;

import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;
import profect.eatcloud.domain.customer.entity.Cart;
import profect.eatcloud.global.timeData.BaseTimeRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends BaseTimeRepository<Cart, UUID> {
    Optional<Cart> findByCustomerId(UUID customerId);
    void deleteByCustomerId(UUID customerId);

    @Query("SELECT COUNT(c) > 0 FROM Cart c WHERE c.customer.id = :customerId")
    boolean existsByCustomerId(@Param("customerId") UUID customerId);
}
