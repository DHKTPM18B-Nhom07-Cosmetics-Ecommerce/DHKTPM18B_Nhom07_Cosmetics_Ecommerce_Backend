package iuh.fit.se.cosmeticsecommercebackend.model;

import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho đơn hàng
 * Quan hệ n-1 với Customer và Employee
 * Quan hệ 1-n với OrderDetail và Review
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "employee", "orderDetails", "reviews"})
@EqualsAndHashCode(exclude = {"customer", "employee", "orderDetails", "reviews"})
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;
    
    /**
     * Quan hệ n-1 với Customer
     * Nhiều Order thuộc về 1 Customer
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    /**
     * Quan hệ n-1 với Employee
     * Nhiều Order được xử lý bởi 1 Employee
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(length = 500)
    private String cancelReason;
    
    private LocalDateTime canceledAt;
    
    @Column(nullable = false)
    private LocalDateTime orderDate;

    /**
     * Quan hệ 1-n với OrderDetail
     * 1 Order có nhiều OrderDetail (chi tiết đơn hàng)
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();


}

