package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //tim don hang theo customer:
    List<Order> findByCustomer(Customer customer);
    //tim don hang theo employee
    List<Order> findByEmployee(Employee employee);
    //tim don hang theo trang thai: cho giao, dang giao, da hoan thanh, da huy...
    List<Order> findByStatus(OrderStatus orderStatus);
    //tim kiem don trong khoang thoi gian
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    //tim don hang cua 1 khach hang co trang thai cu the:
    List<Order> findByStatusAndCustomer(OrderStatus orderStatus, Customer customer);
    //tim don hang co total nam trong 1 khoang
    List<Order>findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal);

}
