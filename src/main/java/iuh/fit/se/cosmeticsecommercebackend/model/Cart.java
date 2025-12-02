package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho giỏ hàng
 * Quan hệ 1-1 với Customer
 * Quan hệ 1-n với CartItem
 */
@Entity
@Table(name = "carts")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "items"})
@EqualsAndHashCode(exclude = {"customer", "items"})
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;
    
    /**
     * Quan hệ 1-1 với Customer
     * 1 Cart thuộc về 1 Customer
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;
    
    @Column(nullable = false, precision = 12, scale = 2, name = "total_price")
    private BigDecimal totalPrice = BigDecimal.ZERO;
    
    /**
     * Quan hệ 1-n với CartItem
     * 1 Cart có nhiều CartItem
     */

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}

