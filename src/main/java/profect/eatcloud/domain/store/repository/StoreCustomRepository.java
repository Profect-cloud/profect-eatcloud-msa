package profect.eatcloud.domain.store.repository;

import profect.eatcloud.domain.store.dto.StoreSearchResponseDto;

import java.util.List;
import java.util.UUID;

public interface StoreCustomRepository {
    List<StoreSearchResponseDto> findStoresByCategoryWithinDistance(
            UUID categoryId, double userLat, double userLon, double distanceKm
    );

    List<StoreSearchResponseDto> findStoresByMenuCategoryWithinDistance(
            String menuCategoryCode, double userLat, double userLon, double distanceKm);
}

