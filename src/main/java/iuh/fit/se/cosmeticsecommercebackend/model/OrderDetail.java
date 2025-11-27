package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity đại diện cho chi tiết đơn hàng
 * Quan hệ n-1 với Order
 * Quan hệ n-1 với ProductVariant
 */
@Entity
@Table(name = "order_details")
@Getter
@Setter
@ToString(exclude = {"order", "productVariant"})
@EqualsAndHashCode(exclude = {"order", "productVariant"})
public class OrderDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long id;
    
    /**
     * Quan hệ n-1 với Order
     * Nhiều OrderDetail thuộc về 1 Order
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;
    
    /**
     * Quan hệ n-1 với ProductVariant
     * Nhiều OrderDetail có thể tham chiếu đến 1 ProductVariant
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
//    @JsonIgnore
    private ProductVariant productVariant;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2, name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(nullable = false, precision = 12, scale = 2, name = "total_price")
    private BigDecimal totalPrice;

    /**
     * Số tiền được giảm giá cho sản phẩm này (nếu có).
     * Trường này hỗ trợ trường hợp voucher áp dụng theo brand/category.
     * Nếu giảm toàn đơn, discountAmount ở đây = 0.
     */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public OrderDetail() {
    }

    public OrderDetail(Long id, Order order, ProductVariant productVariant, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice, BigDecimal discountAmount) {
        this.id = id;
        this.order = order;
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}

