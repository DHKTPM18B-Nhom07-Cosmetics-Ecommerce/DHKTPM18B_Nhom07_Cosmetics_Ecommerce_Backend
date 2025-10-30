package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho biến thể sản phẩm
 * Quan hệ n-1 với Product
 */
@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product")
@EqualsAndHashCode(exclude = "product")
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;
    
    @Column(nullable = false, length = 100, name = "variant_name")
    private String variantName;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(length = 500)
    private String imageUrl;
    
    /**
     * Quan hệ n-1 với Product
     * Nhiều ProductVariant thuộc về 1 Product
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Quan hệ 1-n với OrderDetail
     * Nhiều OrderDetail thuộc về 1 ProductVariant
     */
    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();
    /**
     * Quan hệ 1-n với CartItem
     * Nhiêều CartItem thuộc về 1 ProductVariant
     */
    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();
    /**
     * Kiểm tra xem variant có còn hàng không
     */
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }


}

