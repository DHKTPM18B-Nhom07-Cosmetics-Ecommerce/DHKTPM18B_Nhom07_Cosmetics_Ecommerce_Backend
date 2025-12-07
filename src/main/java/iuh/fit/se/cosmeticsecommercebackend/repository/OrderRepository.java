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

public interface OrderRepository extends JpaRepository<Order, String> {

    @EntityGraph(value = "order-full-details-graph", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Order> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findAll();

    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findByCustomer(Customer customer);

    List<Order> findByEmployee(Employee employee);

    @EntityGraph(attributePaths = {"customer", "address"})
    List<Order> findByStatus(OrderStatus orderStatus);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByStatusAndCustomer(OrderStatus orderStatus, Customer customer);

    List<Order> findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal);

    @Query(value = """
        SELECT o.order_id 
        FROM orders o 
        WHERE o.order_id LIKE CONCAT(?1, '%') 
        ORDER BY o.order_id DESC 
        LIMIT 1
    """, nativeQuery = true)
    Optional<String> findLastOrderIdByDatePrefix(String prefix);

    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.customer.account.id = :accountId
          AND o.status = :status
          AND o.orderDate >= :startTime
    """)
    long countOrdersByStatusAndDate(
            @Param("accountId") Long accountId,
            @Param("status") OrderStatus status,
            @Param("startTime") LocalDateTime startTime
    );

    // GẮN ĐƠN GUEST SAU KHI ĐĂNG KÝ
    @Modifying
    @Query("""
        UPDATE Order o
        SET o.customer = :customer
        WHERE o.customer IS NULL
          AND o.address.phone = :phone
    """)
    int linkGuestOrdersToCustomer(
            @Param("customer") Customer customer,
            @Param("phone") String phone
    );
}
