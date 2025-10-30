package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho sản phẩm
 * Quan hệ n-1 với Category và Brand
 * Quan hệ 1-n với ProductVariant và Review
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private Category category;

    /**
     * Quan hệ n-1 với Brand
     * Nhiều Product thuộc về 1 Brand
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    /**
     * Quan hệ 1-n với ProductVariant
     * 1 Product có nhiều ProductVariant (biến thể sản phẩm)
     * mappedBy = "product" tham chiếu đến thuộc tính product trong ProductVariant entity
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    /**
     * Quan hệ 1-n với Review
     * 1 Product có nhiều Review (đánh giá)
     * mappedBy = "product" tham chiếu đến thuộc tính product trong Review entity
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    private double averageRating = 0.0;

    @Column(name = "is_active")
    private boolean isAactive = true;



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
}
