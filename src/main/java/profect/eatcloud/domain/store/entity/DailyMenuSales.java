package profect.eatcloud.domain.store.entity;

import profect.eatcloud.global.timeData.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_menu_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(DailyMenuSalesId.class)
public class DailyMenuSales extends BaseTimeEntity {

    @Id
    @Column(name = "sale_date")
    private LocalDate saleDate;

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @Id
    @Column(name = "menu_id")
    private UUID menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", insertable = false, updatable = false)
    private Menu menu;

    @Column(name = "quantity_sold", nullable = false)
    private Integer quantitySold;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
}