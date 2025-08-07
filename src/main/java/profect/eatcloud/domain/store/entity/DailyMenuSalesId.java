package profect.eatcloud.domain.store.entity;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class DailyMenuSalesId implements Serializable {
    private LocalDate saleDate;
    private UUID storeId;
    private UUID menuId;
}