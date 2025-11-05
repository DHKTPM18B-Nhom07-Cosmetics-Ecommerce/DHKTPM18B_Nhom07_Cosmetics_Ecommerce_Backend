package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    //sau nay them customerRepo va Employee repo
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    // --- CRUD CƠ BẢN ---

    /** POST /api/orders : Tạo đơn hàng mới */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        try {
            Order newOrder = orderService.createOrder(order);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /** GET /api/orders/{id} : Lấy thông tin đơn hàng theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.findById(id);
            return ResponseEntity.ok(order);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** GET /api/orders : Lấy tất cả đơn hàng */
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }

    /** PUT /api/orders/{id} : Cập nhật đơn hàng (Chỉ cho phép PENDING) */
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        try {
            Order updatedOrder = orderService.updateOrder(id, orderDetails);
            return ResponseEntity.ok(updatedOrder);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // --- NGHIỆP VỤ TÌM KIẾM ---

    /** GET /api/orders/search/status/{status} : Tìm theo trạng thái */
    @GetMapping("/search/status/{status}")
    public List<Order> findByStatus(@PathVariable OrderStatus status) {
        return orderService.findByStatus(status);
    }

    /** GET /api/orders/search/date-range?start=...&end=... : Tìm kiếm trong khoảng thời gian */
    @GetMapping("/search/date-range")
    public List<Order> findByOrderDateBetween(
            @RequestParam("start") String startDate,
            @RequestParam("end") String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            return orderService.findByOrderDateBetween(start, end);
        } catch (Exception e) {
            System.err.println("Lỗi format ngày tháng: " + e.getMessage());
            return List.of();
        }
    }

    // --- XỬ LÝ TRẠNG THÁI (WORKFLOW) ---

    /** * POST /api/orders/{id}/status?newStatus=CONFIRMED&employeeId=101&cancelReason=...
     * Cập nhật trạng thái bằng Query Parameters
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus newStatus, // Trạng thái mới (Bắt buộc)
            @RequestParam(required = false) Long employeeId, // ID nhân viên (Tùy chọn)
            @RequestParam(required = false) String cancelReason // Lý do hủy (Tùy chọn)
    ) {
        try {
            Employee employee = null;
            if (employeeId != null) {
//                employee = employeeRepository.findById(employeeId)
//                        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy Employee với ID: " + employeeId));
            }

            Order updatedOrder = orderService.updateStatus(
                    id,
                    newStatus,
                    cancelReason,
                    employee
            );
            return ResponseEntity.ok(updatedOrder);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.err.println("Lỗi nghiệp vụ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
