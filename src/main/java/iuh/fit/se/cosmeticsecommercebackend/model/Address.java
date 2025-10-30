package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho địa chỉ giao hàng
 * Quan hệ n-1 với Customer
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer")
@EqualsAndHashCode(exclude = "customer")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;
    
    /**
     * Quan hệ n-1 với Customer
     * Nhiều Address thuộc về 1 Customer
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false, length = 100, name = "full_name")
    private String fullName;
    
    @Column(nullable = false, length = 20)
    private String phone;
    
    @Column(nullable = false, length = 255)
    private String address;
    
    @Column(nullable = false, length = 100)
    private String city;
    
    @Column(nullable = false, length = 100)
    private String state;
    
    @Column(nullable = false, length = 100)
    private String country;
    
    /**
     * Đánh dấu địa chỉ mặc định
     * Chỉ có 1 địa chỉ mặc định cho mỗi customer
     */
    @Column(name = "is_default")
    private boolean isDefault = false;

}

