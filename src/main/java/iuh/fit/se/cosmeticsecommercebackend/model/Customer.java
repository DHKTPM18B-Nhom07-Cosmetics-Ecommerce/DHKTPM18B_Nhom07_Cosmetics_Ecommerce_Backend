package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    /**
     * Tên khách hàng
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Quan hệ 1-1 với Account
     * 1 Customer có 1 Account
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Account account;

    /**
     * Quan hệ 1-n với Address
     * 1 Customer có nhiều Address (địa chỉ giao hàng)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Address> addresses = new ArrayList<>();

    /**
     * Quan hệ 1-1 với Cart
     * 1 Customer có 1 Cart (giỏ hàng)
     */
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;

    /**
     * Quan hệ 1-n với Order
     * 1 Customer có nhiều Order (đơn hàng)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonBackReference("order-customer")
    private List<Order> orders = new ArrayList<>();

    /**
     * Quan hệ 1-n với Review
     * 1 Customer có nhiều Review (đánh giá)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    /**
     * Quan hệ 1-n với CustomerVoucher
     * 1 Customer có nhiều CustomerVoucher (lưu nhiều voucher)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CustomerVoucher> customerVouchers;

    /**
     * Quan hệ 1-n với VoucherRedemption
     * 1 Customer có nhiều VoucherRedemption (dùng nhiều voucher)
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<VoucherRedemption> voucherRedemptions;

    /**
     * Customer có 1 danh sách WishList
     * Danh sách lưu id của các ProductVarian mà khách hàng thm vào wishlist
     */
    @ElementCollection
    @CollectionTable(
            name = "customer_wishlist",
            joinColumns = @JoinColumn(name = "customer_id")
    )
    @Column(name = "product_variant_id")
    private List<Long> wishList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public List<CustomerVoucher> getCustomerVouchers() {
        return customerVouchers;
    }

    public void setCustomerVouchers(List<CustomerVoucher> customerVouchers) {
        this.customerVouchers = customerVouchers;
    }

    public List<VoucherRedemption> getVoucherRedemptions() {
        return voucherRedemptions;
    }

    public void setVoucherRedemptions(List<VoucherRedemption> voucherRedemptions) {
        this.voucherRedemptions = voucherRedemptions;
    }

    public List<Long> getWishList() {
        return wishList;
    }

    public void setWishList(List<Long> wishList) {
        this.wishList = wishList;
    }
}
