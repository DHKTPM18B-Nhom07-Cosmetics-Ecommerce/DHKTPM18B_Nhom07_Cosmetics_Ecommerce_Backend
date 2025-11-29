package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho sản phẩm
 * Quan hệ n-1 với Category và Brand
 * Quan hệ 1-n với ProductVariant và Review
 */
@Entity
@Table(name = "products")
@ToString(exclude = {"variants", "reviews"})
@EqualsAndHashCode(exclude = {"variants", "reviews"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String image;

    /**
     * Quan hệ n-1 với Category
     * Nhiều Product thuộc về 1 Category
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    /**
     * Quan hệ n-1 với Brand
     * Nhiều Product thuộc về 1 Brand
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    @JsonIgnore
    private Brand brand;

    /**
     * Quan hệ 1-n với ProductVariant
     * 1 Product có nhiều ProductVariant (biến thể sản phẩm)
     * mappedBy = "product" tham chiếu đến thuộc tính product trong ProductVariant entity
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("product")
    private List<ProductVariant> variants = new ArrayList<>();

    /**
     * Quan hệ 1-n với Review
     * 1 Product có nhiều Review (đánh giá)
     * mappedBy = "product" tham chiếu đến thuộc tính product trong Review entity
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "average_rating")
    private double averageRating = 0.0;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Gán giá trị tự động khi entity được lưu lần đầu
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    /**
     * Helper method để tính toán lại averageRating
     * Được gọi sau khi thêm/xóa/cập nhật review
     */
//    public void calculateAverageRating() {
//        if (reviews.isEmpty()) {
//            this.averageRating = 0.0;
//        } else {
//            double sum = reviews.stream()
//                    .mapToDouble(Review::getRating)
//                    .sum();
//            this.averageRating = sum / reviews.size();
//        }
//    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
