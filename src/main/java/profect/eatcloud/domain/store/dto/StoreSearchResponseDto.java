package profect.eatcloud.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class StoreSearchResponseDto {
    private UUID storeId;
    private String storeName;
    private String storeAddress;
    private Double storeLat;
    private Double storeLon;
    private Integer minCost;
    private Boolean openStatus;
}



