package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    
    CreateOrderResponse createOrderFromRequest(CreateOrderRequest request);

    List<Order> getAll();

    Order findById(String id);

   // Order updateOrder(String id, Order orderDetails);

    //PHUONG THUC TIM KIEM
    //tim don hang theo KH
    List<Order> findByCustomer(Customer customer);
    //tim don hang theo NV
    List<Order> findByEmployee(Employee employee);
    //tim don hang theo trang thai
    List<Order> findByStatus(OrderStatus status);
    // tim don hang trong khang thoi gian
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    //tim theo trang thai va khach hang
    List<Order> findByStatusAndCustomer(OrderStatus status, Customer customer);
    //tim theo tong gia tri trong khoang
    List<Order> findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal);
    // Trong OrderService.java
    List<Order> findByStatusAndOrderDateBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
    // Phương thức mới: Lấy đơn hàng của khách hàng dựa trên ID/Username
    List<Order> getMyOrders(String username);
    /**
     * Lấy chi tiết một đơn hàng, có kiểm tra xem đơn hàng đó có thuộc về username hay không.
     */
    Order getCustomerOrderById(String orderId, String username);

    //NGHIEP VU XU LY TRANG THAI
    //huy don hang boi nhan vien
    Order cancelByEmployee(String id, String cancelReason, Employee employee);
    //khach hang yeu cau huy don hang

    Order cancelByCustomer(String orderId, String cancelReason, Customer customer);
    //yeu cau hoan tra
    Order requestReturn(String id, String returnReason, Employee employee);

    //hoan tat xu ly hoan tien
    Order processRefund(String id, Employee employee);
    //thay doi trang thai theo quy trinh nghiep vu

    Order updateStatus(String id, OrderStatus newStatus, String cancelReason, Employee employee);
    //tinh tong tien don hang dua tren chi tiet don hang
    BigDecimal calculateTotal(String orderId);
}
