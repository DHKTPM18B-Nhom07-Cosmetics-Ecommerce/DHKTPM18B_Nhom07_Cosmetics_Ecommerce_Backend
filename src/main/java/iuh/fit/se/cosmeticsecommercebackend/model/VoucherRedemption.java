package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_redemption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_redemption_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "amount_discounted", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountDiscounted = BigDecimal.ZERO;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt = LocalDateTime.now();

}
