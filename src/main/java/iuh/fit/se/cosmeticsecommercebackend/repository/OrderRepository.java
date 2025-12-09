package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.RevenueStatsRespond;
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

    /**
     * Truy vấn lấy dữ liệu doanh thu chi tiết (ngày, tổng doanh thu, tổng đơn hàng)
     * của các đơn hàng đã DELIVERED trong khoảng thời gian.
     * Trả về List<Object[]>: [date, totalRevenue, totalOrders]
     * @param startDate
     * @param endDate
     */
    @Query(value = """
        SELECT
            DATE(o.order_date) AS order_day,
            SUM(o.total) AS total_revenue,
            COUNT(o.order_id) AS total_orders
        FROM
            orders o
        WHERE
            o.status = 'DELIVERED' 
            AND o.order_date >= :startDate
            AND o.order_date <= :endDate
        GROUP BY
            order_day
        ORDER BY
            order_day
    """, nativeQuery = true) // <--- QUAN TRỌNG: SỬ DỤNG SQL THUẦN
    // KIỂU TRẢ VỀ ĐƠN GIẢN NHẤT ĐỂ TRÁNH LỖI VALIDATION HQL
    List<Object[]> findDailyRevenueAndOrders(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Truy vấn lấy Top 5 Product Variant bán chạy nhất (theo số lượng và doanh thu).
     */
    @Query(value = """
        SELECT
            od.variant_id AS variant_id,
            SUM(od.quantity) AS total_sales,
            SUM(od.final_price) AS total_revenue
        FROM
            order_details od
        JOIN
            orders o ON od.order_id = o.order_id
        WHERE
            o.status = 'DELIVERED' 
            AND o.order_date >= :startDate
            AND o.order_date <= :endDate
        GROUP BY
            od.variant_id
        ORDER BY
            total_sales DESC, total_revenue DESC
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> findTopSellingProductVariants(LocalDateTime startDate, LocalDateTime endDate);
}
