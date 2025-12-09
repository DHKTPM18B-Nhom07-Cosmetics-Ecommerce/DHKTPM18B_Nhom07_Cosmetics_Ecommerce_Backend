package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho đánh giá sản phẩm
 * Quan hệ n-1 với Customer, Product và Order
 */
@Entity
@Table(name = "reviews")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "product", "order"})
@EqualsAndHashCode(exclude = {"customer", "product", "order"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    /**
     * Quan hệ n-1 với Customer
     * Nhiều Review thuộc về 1 Customer
     */
    @JsonIgnoreProperties({"reviews", "orders", "addresses", "cart", "account"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Quan hệ n-1 với Product
     * Nhiều Review thuộc về 1 Product
     */
    @JsonIgnoreProperties({"reviews", "variants", "category", "brand"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Đánh giá từ 1-5 sao
     */
    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Tự động set reviewDate khi tạo mới
     */
    @PrePersist
    protected void onCreate() {
        if (reviewDate == null) {
            reviewDate = LocalDateTime.now();
        }
    }

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
