package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entity đại diện cho chi tiết đơn hàng
 */
@Entity
@Table(name = "order_details")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false, precision = 10, scale = 2, name = "unit_price")
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, name = "total_price")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, name = "final_price")
    private BigDecimal finalPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2, name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public OrderDetail() {}

    /* ====== CORE LOGIC – CHỐNG NULL ====== */
    public void recalc() {
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;
        if (quantity == null) quantity = 0;
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;

        totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        finalPrice = totalPrice.subtract(discountAmount);

        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
    }

    /* ====== SETTER AUTO RECALC ====== */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        recalc();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalc();
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        recalc();
    }

    /* ====== GET / SET ====== */
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }

    public void setOrder(Order order) { this.order = order; }

    public ProductVariant getProductVariant() { return productVariant; }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public Integer getQuantity() { return quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }

    public BigDecimal getFinalPrice() { return finalPrice; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
}
