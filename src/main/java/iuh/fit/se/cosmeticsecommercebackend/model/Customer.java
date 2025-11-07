package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho khách hàng
 * Quan hệ 1-1 với Account
 * Quan hệ 1-n với Address, WishlistItem, Order, Review
 * Quan hệ 1-1 với Cart
 */
@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"account", "addresses", "cart", "wishlistItems", "orders", "reviews"})
@EqualsAndHashCode(exclude = {"account", "addresses", "cart", "wishlistItems", "orders", "reviews"})
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;
    
    /**
     * Quan hệ 1-1 với Account
     * 1 Customer có 1 Account
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;
    
    /**
     * Quan hệ 1-n với Address
     * 1 Customer có nhiều Address (địa chỉ giao hàng)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();
    
    /**
     * Quan hệ 1-1 với Cart
     * 1 Customer có 1 Cart (giỏ hàng)
     */
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;
    
    /**
     * Quan hệ 1-n với WishlistItem
     * 1 Customer có nhiều WishlistItem (danh sách yêu thích)
     */
//    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<WishlistItem> wishlistItems = new ArrayList<>();
    
    /**
     * Quan hệ 1-n với Order
     * 1 Customer có nhiều Order (đơn hàng)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
    
    /**
     * Quan hệ 1-n với Review
     * 1 Customer có nhiều Review (đánh giá)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}

