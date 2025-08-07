package profect.eatcloud.domain.manager.entity;

import jakarta.persistence.*;
import lombok.*;
import profect.eatcloud.domain.store.entity.Store;
import profect.eatcloud.global.timeData.BaseTimeEntity;

import java.util.UUID;

@Entity
@Table(name = "p_managers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manager extends BaseTimeEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "phone_number", length = 18)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
}

