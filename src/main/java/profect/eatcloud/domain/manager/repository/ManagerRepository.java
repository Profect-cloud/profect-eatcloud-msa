package profect.eatcloud.domain.manager.repository;

import profect.eatcloud.domain.manager.entity.Manager;
import profect.eatcloud.global.timeData.BaseTimeRepository;

import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository extends BaseTimeRepository<Manager, UUID> {
	Optional<Manager> findByEmail(String email);
}
