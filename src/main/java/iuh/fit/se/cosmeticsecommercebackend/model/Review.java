package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho đánh giá sản phẩm
 * Quan hệ n-1 với Customer, Product và Order
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "product", "order"})
@EqualsAndHashCode(exclude = {"customer", "product", "order"})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    /**
     * Quan hệ n-1 với Customer
     * Nhiều Review thuộc về 1 Customer
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Quan hệ n-1 với Product
     * Nhiều Review thuộc về 1 Product
     */
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

}
