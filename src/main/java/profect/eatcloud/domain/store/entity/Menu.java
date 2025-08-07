package profect.eatcloud.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;
import profect.eatcloud.domain.store.dto.MenuRequestDto;
import profect.eatcloud.global.timeData.BaseTimeEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "menu_id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "menu_num", nullable = false)
    private int menuNum;

    @Column(name = "menu_name", nullable = false, length = 200)
    private String menuName;

    @Column(name = "menu_category_code", nullable = false, length = 30)
    private String menuCategoryCode;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

}
