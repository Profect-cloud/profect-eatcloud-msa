package profect.eatcloud.domain.admin.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import profect.eatcloud.domain.admin.entity.ManagerStoreApplication;
import profect.eatcloud.global.timeData.BaseTimeRepository;

@Repository
public interface ManagerStoreApplicationRepository extends BaseTimeRepository<ManagerStoreApplication, UUID> {
	boolean existsByManagerEmail(String managerEmail);
}