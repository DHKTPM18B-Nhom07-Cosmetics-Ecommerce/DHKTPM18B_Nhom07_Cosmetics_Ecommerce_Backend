package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "products")
@ToString(exclude = {"variants", "reviews", "vouchers", "category", "brand"})
@EqualsAndHashCode(exclude = {"variants", "reviews", "vouchers", "category", "brand"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    /* ================= CATEGORY (MANY-TO-ONE) ================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    /* ================= BRAND (MANY-TO-ONE) ================= */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    @JsonIgnore
    private Brand brand;

    /* ================= VARIANTS (ONE-TO-MANY) ================= */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProductVariant> variants = new ArrayList<>();

    /* ================= REVIEWS (ONE-TO-MANY) ================= */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();


    @Column(name = "average_rating")
    private double averageRating = 0.0;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /* ================= TRANSIENT FIELDS ================= */
    @Transient
    private Integer minPrice;

    @Transient
    private Integer maxPrice;

    @Transient
    private Boolean inStock;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /* ================= VOUCHERS (MANY-TO-MANY) ================= */
    @ManyToMany
    @JoinTable(
            name = "voucher_products",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "voucher_id")
    )
    @JsonIgnoreProperties({"products", "categories", "brands"})
    private Set<Voucher> vouchers = new HashSet<>();


    /* ================= GETTERS / SETTERS ================= */

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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
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

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public Set<Voucher> getVouchers() {
        return vouchers;
    }

    public void setVouchers(Set<Voucher> vouchers) {
        this.vouchers = vouchers;
    }
}
