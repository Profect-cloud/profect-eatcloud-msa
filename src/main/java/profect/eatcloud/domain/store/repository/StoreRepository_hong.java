package profect.eatcloud.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import profect.eatcloud.domain.store.entity.Store;

import java.util.UUID;

@Repository
public interface StoreRepository_hong extends JpaRepository<Store, UUID> {

}
