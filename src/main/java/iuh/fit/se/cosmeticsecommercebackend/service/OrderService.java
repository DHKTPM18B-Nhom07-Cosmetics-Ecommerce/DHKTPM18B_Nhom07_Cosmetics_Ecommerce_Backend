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

    // ====== CREATE ======
    Order createOrder(Order order);
    CreateOrderResponse createOrderFromRequest(CreateOrderRequest request);

    // ====== READ ======
    List<Order> getAll();
    Order findById(String id);

    // ====== SEARCH ======
    List<Order> findByCustomer(Customer customer);
    List<Order> findByEmployee(Employee employee);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStatusAndCustomer(OrderStatus status, Customer customer);
    List<Order> findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal);

    // ====== CUSTOMER ======
    List<Order> getMyOrders(String username);
    Order getCustomerOrderById(String orderId, String username);

    // ====== WORKFLOW ======
    Order cancelByEmployee(String id, String cancelReason, Employee employee);
    Order cancelByCustomer(String orderId, String cancelReason, Customer customer);
    Order requestReturn(String id, String returnReason, Employee employee);
    Order processRefund(String id, Employee employee);
    Order updateStatus(String id, OrderStatus newStatus, String cancelReason, Employee employee);

    // ====== TOTAL ======
    BigDecimal calculateTotal(String orderId);

    // GẮN ĐƠN GUEST SAU KHI ĐĂNG KÝ
    void linkGuestOrders(String phone, Customer customer);
}
