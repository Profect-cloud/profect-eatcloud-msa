package profect.eatcloud.domain.globalCategory.entity;

import jakarta.persistence.*;
import lombok.*;

import profect.eatcloud.global.timeData.BaseTimeEntity;

@Entity
@Table(name = "payment_method_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodCode extends BaseTimeEntity {
    @Id
    @Column(name = "code", length = 30)
    private String code;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
} 