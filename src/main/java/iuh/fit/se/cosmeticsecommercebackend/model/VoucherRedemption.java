package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "voucher_redemptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"voucher_id", "order_id"})
)
// với thuộc tính uniqueContraints => không thể apply 1 voucher nhiều lần cho cùng đơn hàng

@Builder
public class VoucherRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_redemption_id")
    private Long id;

    /**
     * Voucher đã được sử dụng
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    /**
     * Đơn hàng áp dụng voucher
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * (Tuỳ chọn) Khách hàng đã sử dụng voucher.
     * Giúp thống kê hoặc kiểm tra giới hạn "per_user_limit"
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * Số tiền thực tế được giảm trong đơn này.
     */
    @Column(name = "amount_discounted", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountDiscounted = BigDecimal.ZERO;

    /**
     * Thời điểm voucher được ghi nhận sử dụng (persist).
     */
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    public VoucherRedemption() {
    }

    public VoucherRedemption(Long id, Voucher voucher, Order order, Customer customer, BigDecimal amountDiscounted, LocalDateTime redeemedAt) {
        this.id = id;
        this.voucher = voucher;
        this.order = order;
        this.customer = customer;
        this.amountDiscounted = amountDiscounted;
        this.redeemedAt = redeemedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BigDecimal getAmountDiscounted() {
        return amountDiscounted;
    }

    public void setAmountDiscounted(BigDecimal amountDiscounted) {
        this.amountDiscounted = amountDiscounted;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    /**
     * Tự động set thời gian khi persist vào DB.
     */
    @PrePersist
    protected void onRedeem() {
        this.redeemedAt = LocalDateTime.now();
    }

}
