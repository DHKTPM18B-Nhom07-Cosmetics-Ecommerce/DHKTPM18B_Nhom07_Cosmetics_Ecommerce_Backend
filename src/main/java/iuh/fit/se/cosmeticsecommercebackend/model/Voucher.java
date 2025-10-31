package iuh.fit.se.cosmeticsecommercebackend.model;

import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherType type;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal value;

    @Column(name = "min_order_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(name = "is_stackable", nullable = false)
    private boolean isStackable = false;

    private boolean status = false;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "voucher")
    private List<VoucherRedemption> redemptions;

    @OneToMany(mappedBy = "voucher")
    private List<CustomerVoucher> customerVouchers;


}
