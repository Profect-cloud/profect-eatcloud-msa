package profect.eatcloud.domain.store.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import profect.eatcloud.domain.store.entity.Menu;
import profect.eatcloud.domain.store.entity.Store;
import profect.eatcloud.domain.store.exception.MenuErrorCode;
import profect.eatcloud.domain.store.exception.MenuException;
import profect.eatcloud.domain.store.exception.StoreErrorCode;
import profect.eatcloud.domain.store.exception.StoreException;
import profect.eatcloud.domain.store.repository.MenuRepository_min;
import profect.eatcloud.domain.store.repository.StoreRepository_min;

import java.util.List;
import java.util.UUID;


@Service
public class MenuService {

    private final MenuRepository_min menuRepository;
    private final StoreRepository_min storeRepository;

    @Autowired
    public MenuService(MenuRepository_min menuRepository, StoreRepository_min storeRepository) {
        this.menuRepository = menuRepository;
        this.storeRepository = storeRepository;
    }

    public List<Menu> getMenusByStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        return menuRepository.findAllByStoreAndTimeData_DeletedAtIsNull(store);
    }

    public Menu getMenuById(UUID storeId, UUID menuId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        return menuRepository.findByIdAndStoreAndTimeData_DeletedAtIsNull(menuId, store)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));
    }
}
