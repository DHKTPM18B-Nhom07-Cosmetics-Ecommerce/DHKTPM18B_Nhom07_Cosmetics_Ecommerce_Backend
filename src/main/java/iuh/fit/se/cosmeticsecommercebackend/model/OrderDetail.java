package iuh.fit.se.cosmeticsecommercebackend.model;

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
@NoArgsConstructor
@AllArgsConstructor
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
    private Order order;
    
    /**
     * Quan hệ n-1 với ProductVariant
     * Nhiều OrderDetail có thể tham chiếu đến 1 ProductVariant
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2, name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(nullable = false, precision = 12, scale = 2, name = "total_price")
    private BigDecimal totalPrice;

}

