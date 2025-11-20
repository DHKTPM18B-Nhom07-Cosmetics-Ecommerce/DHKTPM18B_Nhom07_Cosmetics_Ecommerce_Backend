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
    /**
     * Quan hệ n-1 với Address
     * Nhiều Order có thể cùng 1 Address
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(length = 500)
    private String cancelReason;
    
    private LocalDateTime canceledAt;
    
    @Column(nullable = false)
    private LocalDateTime orderDate=LocalDateTime.now();
    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee = new BigDecimal("30000.00");

    /**
     * Quan hệ 1-n với OrderDetail
     * 1 Order có nhiều OrderDetail (chi tiết đơn hàng)
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VoucherRedemption> voucherRedemptions = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, Customer customer, Employee employee, Address address, BigDecimal total, OrderStatus status, String cancelReason, LocalDateTime canceledAt, LocalDateTime orderDate, BigDecimal shippingFee, List<OrderDetail> orderDetails, List<VoucherRedemption> voucherRedemptions) {
        this.id = id;
        this.customer = customer;
        this.employee = employee;
        this.address = address;
        this.total = total;
        this.status = status;
        this.cancelReason = cancelReason;
        this.canceledAt = canceledAt;
        this.orderDate = orderDate;
        this.shippingFee = shippingFee;
        this.orderDetails = orderDetails;
        this.voucherRedemptions = voucherRedemptions;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Address getAddress() {
        return address;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public List<VoucherRedemption> getVoucherRedemptions() {
        return voucherRedemptions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public void setVoucherRedemptions(List<VoucherRedemption> voucherRedemptions) {
        this.voucherRedemptions = voucherRedemptions;
    }
}

