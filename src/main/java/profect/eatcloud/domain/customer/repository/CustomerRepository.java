package profect.eatcloud.domain.customer.repository;

import java.util.Optional;
import java.util.UUID;

import profect.eatcloud.domain.customer.entity.Customer;
import profect.eatcloud.global.timeData.BaseTimeRepository;


public interface CustomerRepository extends BaseTimeRepository<Customer, UUID> {
	Optional<Customer> findByEmail(String email);

	Optional<Customer> findByNameAndTimeData_DeletedAtIsNull(String name);
	Optional<Customer> findByEmailAndTimeData_DeletedAtIsNull(String email);
	boolean existsByNameAndTimeData_DeletedAtIsNull(String name);
	boolean existsByEmailAndTimeData_DeletedAtIsNull(String email);
	boolean existsByNicknameAndTimeData_DeletedAtIsNull(String nickname);
}
