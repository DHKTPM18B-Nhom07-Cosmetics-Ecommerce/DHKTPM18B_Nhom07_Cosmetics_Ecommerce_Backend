package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity đại diện cho item trong giỏ hàng
 * Quan hệ n-1 với Cart
 * Quan hệ n-1 với ProductVariant
 */
@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"cart", "productVariant"})
@EqualsAndHashCode(exclude = {"cart", "productVariant"})
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Quan hệ n-1 với Cart
     * Nhiều CartItem thuộc về 1 Cart
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    /**
     * Quan hệ n-1 với ProductVariant
     * Nhiều CartItem có thể tham chiếu đến 1 ProductVariant
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;
    
    @Column(nullable = false)
    private int quantity;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;
    

}

