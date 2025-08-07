package profect.eatcloud.domain.admin.repository;

import java.util.Optional;
import java.util.UUID;

import profect.eatcloud.domain.admin.entity.Admin;
import profect.eatcloud.global.timeData.BaseTimeRepository;

public interface AdminRepository extends BaseTimeRepository<Admin, UUID> {
	Optional<Admin> findByEmail(String email);
}