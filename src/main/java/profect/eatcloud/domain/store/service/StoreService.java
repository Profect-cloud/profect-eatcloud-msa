package profect.eatcloud.domain.store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import profect.eatcloud.domain.store.dto.StoreSearchByMenuCategoryRequestDto;
import profect.eatcloud.domain.store.dto.StoreSearchRequestDto;
import profect.eatcloud.domain.store.dto.StoreSearchResponseDto;
import profect.eatcloud.domain.store.repository.StoreRepository_min;

import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

    private final StoreRepository_min storeRepository;

    @Autowired
    public StoreService(StoreRepository_min storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreSearchResponseDto> searchStoresByCategoryAndDistance(StoreSearchRequestDto condition) {
        return storeRepository.findStoresByCategoryWithinDistance(
                condition.getCategoryId(),
                condition.getUserLat(),
                condition.getUserLon(),
                condition.getDistanceKm()
        );
    }

    public List<StoreSearchResponseDto> searchStoresByMenuCategory(StoreSearchByMenuCategoryRequestDto condition) {
        return storeRepository.findStoresByMenuCategoryWithinDistance(
                condition.getCategoryCode(),
                condition.getUserLat(),
                condition.getUserLon(),
                condition.getDistanceKm()
        );
    }
}
