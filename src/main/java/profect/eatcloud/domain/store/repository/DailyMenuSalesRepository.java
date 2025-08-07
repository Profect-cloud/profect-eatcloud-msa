
package profect.eatcloud.domain.store.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import profect.eatcloud.domain.store.dto.MenuSalesAggregationDto;
import profect.eatcloud.domain.store.entity.*;
import profect.eatcloud.global.timeData.BaseTimeRepository;
import profect.eatcloud.global.queryDSL.SoftDeletePredicates;
import profect.eatcloud.global.queryDSL.SpringContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DailyMenuSalesRepository extends BaseTimeRepository<DailyMenuSales, DailyMenuSalesId> {

    default List<MenuSalesAggregationDto> getMenuSalesRanking(
            UUID storeId, LocalDate startDate, LocalDate endDate, int limit) {

        JPAQueryFactory queryFactory = getQueryFactory();
        QDailyMenuSales menuSales = QDailyMenuSales.dailyMenuSales;
        QStore store = QStore.store;
        QMenu menu = QMenu.menu;

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(SoftDeletePredicates.menuSalesWithStoreAndMenuActive());
        condition.and(store.storeId.eq(storeId));

        if (startDate != null) {
            condition.and(menuSales.saleDate.goe(startDate));
        }
        if (endDate != null) {
            condition.and(menuSales.saleDate.loe(endDate));
        }

        return queryFactory
                .select(Projections.constructor(MenuSalesAggregationDto.class,
                        menu.id,
                        menu.menuName,
                        menuSales.quantitySold.sum(),
                        menuSales.totalAmount.sum()
                ))
                .from(menuSales)
                .join(menuSales.store, store)
                .join(menuSales.menu, menu)
                .where(condition)
                .groupBy(menu.id, menu.menuName)
                .orderBy(menuSales.totalAmount.sum().desc())
                .limit(limit)
                .fetch();
    }

    default JPAQueryFactory getQueryFactory() {
        return SpringContext.getBean(JPAQueryFactory.class);
    }
}