package profect.eatcloud.domain.store.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import profect.eatcloud.domain.store.dto.*;
import profect.eatcloud.domain.store.entity.DailyStoreSales;
import profect.eatcloud.domain.store.exception.StoreAccessDeniedException;
import profect.eatcloud.domain.store.exception.SalesStatisticsException;
import profect.eatcloud.domain.store.repository.DailyStoreSalesRepository;
import profect.eatcloud.domain.store.repository.DailyMenuSalesRepository;
import profect.eatcloud.domain.manager.repository.ManagerRepository;
import profect.eatcloud.domain.manager.exception.ManagerNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional(readOnly = true)
public class StoreSalesService {

    private final DailyStoreSalesRepository dailyStoreSalesRepository;
    private final DailyMenuSalesRepository dailyMenuSalesRepository;
    private final ManagerRepository managerRepository;

    public StoreSalesService(DailyStoreSalesRepository dailyStoreSalesRepository,
                             DailyMenuSalesRepository dailyMenuSalesRepository,
                             ManagerRepository managerRepository) {
        this.dailyStoreSalesRepository = dailyStoreSalesRepository;
        this.dailyMenuSalesRepository = dailyMenuSalesRepository;
        this.managerRepository = managerRepository;
    }

    public List<DailySalesResponseDto> getDailySales(UUID storeId, LocalDate startDate, LocalDate endDate, UUID managerId) {
        validateManagerStoreAccess(managerId, storeId);
        validateDateRange(startDate, endDate);

        List<DailyStoreSales> salesList = dailyStoreSalesRepository
                .findByStoreIdAndDateRangeActive(storeId, startDate, endDate);

        return salesList.stream()
                .map(DailySalesResponseDto::from)
                .toList();
    }

    public List<MenuSalesRankingResponseDto> getMenuSalesRanking(UUID storeId, LocalDate startDate, LocalDate endDate, int limit, UUID managerId) {
        validateManagerStoreAccess(managerId, storeId);
        validateDateRange(startDate, endDate);
        validateLimit(limit);

        List<MenuSalesAggregationDto> aggregations = dailyMenuSalesRepository
                .getMenuSalesRanking(storeId, startDate, endDate, limit);

        return createMenuRankingResponse(aggregations);
    }

    public SalesPeriodSummaryResponseDto getSalesSummary(UUID storeId, LocalDate startDate, LocalDate endDate, UUID managerId) {
        validateManagerStoreAccess(managerId, storeId);
        validateDateRange(startDate, endDate);

        List<DailyStoreSales> salesList = dailyStoreSalesRepository
                .findByStoreIdAndDateRangeActive(storeId, startDate, endDate);

        return createSummaryFromSalesList(storeId, startDate, endDate, salesList);
    }

    public SalesStatisticsResponseDto getSalesStatistics(UUID storeId, LocalDate startDate, LocalDate endDate, UUID managerId) {
        validateManagerStoreAccess(managerId, storeId);
        validateDateRange(startDate, endDate);

        SalesStatisticsData statisticsData = getSalesStatisticsData(storeId, startDate, endDate);

        return SalesStatisticsResponseDto.builder()
                .storeId(storeId)
                .startDate(startDate)
                .endDate(endDate)
                .summary(statisticsData.summary())
                .dailySales(statisticsData.dailySales())
                .topMenus(statisticsData.topMenus())
                .build();
    }

    private SalesStatisticsData getSalesStatisticsData(UUID storeId, LocalDate startDate, LocalDate endDate) {
        List<DailyStoreSales> salesList = dailyStoreSalesRepository
                .findByStoreIdAndDateRangeActive(storeId, startDate, endDate);

        List<MenuSalesAggregationDto> menuAggregations = dailyMenuSalesRepository
                .getMenuSalesRanking(storeId, startDate, endDate, 5);

        List<DailySalesResponseDto> dailySales = salesList.stream()
                .map(DailySalesResponseDto::from)
                .toList();

        List<MenuSalesRankingResponseDto> topMenus = createMenuRankingResponse(menuAggregations);
        SalesPeriodSummaryResponseDto summary = createSummaryFromSalesList(storeId, startDate, endDate, salesList);

        return new SalesStatisticsData(summary, dailySales, topMenus);
    }

    private List<MenuSalesRankingResponseDto> createMenuRankingResponse(List<MenuSalesAggregationDto> aggregations) {
        AtomicInteger rank = new AtomicInteger(1);
        return aggregations.stream()
                .map(aggregation -> MenuSalesRankingResponseDto.builder()
                        .menuId(aggregation.getMenuId())
                        .menuName(aggregation.getMenuName())
                        .totalQuantitySold(aggregation.getTotalQuantitySold().intValue())
                        .totalAmount(aggregation.getTotalAmount())
                        .averagePrice(aggregation.getAveragePrice())
                        .ranking(rank.getAndIncrement())
                        .build())
                .toList();
    }

    private SalesPeriodSummaryResponseDto createSummaryFromSalesList(UUID storeId, LocalDate startDate, LocalDate endDate, List<DailyStoreSales> salesList) {
        if (salesList.isEmpty()) {
            return SalesPeriodSummaryResponseDto.empty(storeId, startDate, endDate);
        }

        BigDecimal totalAmount = salesList.stream()
                .map(DailyStoreSales::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalOrderCount = salesList.stream()
                .mapToInt(sales -> Optional.ofNullable(sales.getOrderCount()).orElse(0))  // ★ 개선: Optional 사용
                .sum();

        BigDecimal averageDailyAmount = totalAmount.divide(BigDecimal.valueOf(salesList.size()), 2, RoundingMode.HALF_UP);

        DailyStoreSales bestDay = salesList.stream()
                .filter(sales -> sales.getTotalAmount() != null)
                .max(Comparator.comparing(DailyStoreSales::getTotalAmount))
                .orElse(null);

        DailyStoreSales worstDay = salesList.stream()
                .filter(sales -> sales.getTotalAmount() != null)
                .min(Comparator.comparing(DailyStoreSales::getTotalAmount))
                .orElse(null);

        return SalesPeriodSummaryResponseDto.builder()
                .storeId(storeId)
                .startDate(startDate)
                .endDate(endDate)
                .totalAmount(totalAmount)
                .totalOrderCount(totalOrderCount)
                .averageDailyAmount(averageDailyAmount)
                .bestSalesDay(bestDay != null ? DailySalesResponseDto.from(bestDay) : null)
                .worstSalesDay(worstDay != null ? DailySalesResponseDto.from(worstDay) : null)
                .salesDays(salesList.size())
                .build();
    }

    private void validateManagerStoreAccess(UUID managerId, UUID storeId) {
        if (managerId == null) {
            throw new IllegalArgumentException("Manager ID cannot be null");
        }
        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        boolean hasAccess = managerRepository.findById(managerId)
                .map(manager -> manager.getStore() != null && manager.getStore().getStoreId().equals(storeId))
                .orElseThrow(() -> new ManagerNotFoundException(managerId.toString()));

        if (!hasAccess) {
            throw new StoreAccessDeniedException(managerId.toString(), storeId.toString());
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        if (startDate.isAfter(endDate)) {
            throw new SalesStatisticsException.InvalidDateRangeException(
                    "Start date cannot be after end date");
        }

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        if (startDate.isBefore(oneYearAgo)) {
            throw new SalesStatisticsException.InvalidDateRangeException(
                    "Start date cannot be more than 1 year ago");
        }

        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today)) {
            throw new SalesStatisticsException.InvalidDateRangeException(
                    "Start date cannot be in the future");
        }
    }

    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new SalesStatisticsException.InvalidLimitException(
                    "Limit must be positive");
        }
        if (limit > 100) {
            throw new SalesStatisticsException.InvalidLimitException(
                    "Limit cannot exceed 100");
        }
    }

    private record SalesStatisticsData(
            SalesPeriodSummaryResponseDto summary,
            List<DailySalesResponseDto> dailySales,
            List<MenuSalesRankingResponseDto> topMenus) {

        public SalesStatisticsData {
            Objects.requireNonNull(summary, "summary cannot be null");
            dailySales = dailySales != null ? List.copyOf(dailySales) : List.of();
            topMenus = topMenus != null ? List.copyOf(topMenus) : List.of();
        }
    }
}