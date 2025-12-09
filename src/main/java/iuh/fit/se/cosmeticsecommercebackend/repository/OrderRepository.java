package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ORDER REPOSITORY
 * Spring Data JPA sẽ tự động sinh implementation
 */
public interface OrderRepository extends JpaRepository<Order, String> {

    /* ===================== READ ===================== */

    // Load FULL order (customer, address, orderDetails, product, voucher...)
    @EntityGraph(value = "order-full-details-graph", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Order> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findAll();

    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findByCustomer(Customer customer);

    List<Order> findByEmployee(Employee employee);

    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Order> findByStatusAndCustomer(
            OrderStatus status,
            Customer customer
    );

    List<Order> findByTotalBetween(
            BigDecimal minTotal,
            BigDecimal maxTotal
    );

    List<Order> findByStatusAndOrderDateBetween(
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /* ===================== ORDER ID ===================== */

    // Lấy order_id cuối cùng theo prefix (OD-20241203xx)
    @Query(value = """
        SELECT o.order_id
        FROM orders o
        WHERE o.order_id LIKE CONCAT(?1, '%')
        ORDER BY o.order_id DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findLastOrderIdByDatePrefix(String prefix);

    /* ===================== GUEST LINK ===================== */

    // Gắn đơn GUEST → CUSTOMER sau khi đăng ký
    @Modifying
    @Query("""
        UPDATE Order o
        SET o.customer = :customer
        WHERE o.customer IS NULL
          AND o.guestPhone = :phone
    """)
    int linkGuestOrdersToCustomer(
            @Param("customer") Customer customer,
            @Param("phone") String phone
    );

    /* ===================== STATISTICS ===================== */

    // Đếm đơn theo trạng thái + thời gian (anti spam / báo cáo)
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.customer.account.id = :accountId
          AND o.status = :status
          AND o.orderDate >= :fromDate
    """)
    long countOrdersByStatusAndDate(
            @Param("accountId") Long accountId,
            @Param("status") OrderStatus status,
            @Param("fromDate") LocalDateTime fromDate
    );
}
