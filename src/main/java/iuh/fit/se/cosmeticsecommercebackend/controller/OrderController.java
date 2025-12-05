package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.service.CustomerService;
import iuh.fit.se.cosmeticsecommercebackend.service.EmployeeService;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;

    public OrderController(OrderService orderService, EmployeeService employeeService, CustomerService customerService) {
        this.orderService = orderService;
        this.employeeService = employeeService;
        this.customerService = customerService;
    }

    // --- CRUD CƠ BẢN ---

    /** POST /api/orders : Tạo đơn hàng mới */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order newOrder = orderService.createOrder(order);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    /** * GET /api/orders/{id} : Lấy thông tin chi tiết đơn hàng (Dành cho Khách hàng). */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getCustomerOrderDetail(@PathVariable String id, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        String username = principal.getName();
        Order order = orderService.getCustomerOrderById(id, username);
        return ResponseEntity.ok(order);
    }

    /** * GET /api/orders : Lấy danh sách đơn hàng cá nhân (Customer). */
    @GetMapping
    public List<Order> getCustomerOrders(
            Principal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end
    ) {
        if (principal == null) {
            throw new ResourceNotFoundException("Yêu cầu xác thực để xem đơn hàng.");
        }
        String username = principal.getName();
        Customer customer = customerService.findByAccountUsername(username);

        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin Khách hàng cho tài khoản này.");
        }

        if (status != null) {
            return orderService.findByStatusAndCustomer(status, customer);
        }

        // Logic lọc theo ngày tháng cho Customer sẽ cần thêm code ở đây nếu không có status.
        return orderService.getMyOrders(username);
    }

    // --- NGHIỆP VỤ TÌM KIẾM (CHỈ NÊN DÀNH CHO ADMIN/EMPLOYEE) ---

    @GetMapping("/admin/all")
    public List<Order> getAllOrdersForAdmin() {
        return orderService.getAll();
    }

    /** GET /api/orders/admin/status/{status} : Tìm theo trạng thái */
    @GetMapping("/admin/status/{status}")
    public List<Order> findByStatus(@PathVariable OrderStatus status) {
        return orderService.findByStatus(status);
    }

    /** 🎯 SỬA ĐỔI: GET /api/orders/admin/date-range (Hỗ trợ lọc kết hợp Status) */
    @GetMapping("/admin/date-range")
    public List<Order> findByOrderDateBetween(
            @RequestParam("start") LocalDateTime start,
            @RequestParam("end") LocalDateTime end,
            @RequestParam(required = false) OrderStatus status) { // THÊM status option

        // Nếu có Status, gọi Service có logic lọc kết hợp
        if (status != null) {
            // Service cần có phương thức findByOrderDateBetweenAndStatus
            // return orderService.findByOrderDateBetweenAndStatus(start, end, status);
        }
        return orderService.findByOrderDateBetween(start, end);
    }

    /** GET /api/orders/admin/customer/{customerId} : Tìm đơn hàng theo Khách hàng */
    @GetMapping("/admin/customer/{customerId}")
    public ResponseEntity<List<Order>> findByCustomer(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId);
        List<Order> orders = orderService.findByCustomer(customer);
        return ResponseEntity.ok(orders);
    }

    /** GET /api/orders/admin/employee/{employeeId} : Tìm đơn hàng theo Nhân viên */
    @GetMapping("/admin/employee/{employeeId}")
    public ResponseEntity<List<Order>> findByEmployee(@PathVariable Long employeeId) {
        Employee employee = employeeService.findEmployeeById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Employee ID: " + employeeId));

        List<Order> orders = orderService.findByEmployee(employee);
        return ResponseEntity.ok(orders);
    }

    /** GET /api/orders/admin/total-range?min=...&max=... : Tìm theo tổng tiền trong khoảng */
    @GetMapping("/admin/total-range")
    public List<Order> findByTotalBetween(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return orderService.findByTotalBetween(min, max);
    }

    /** GET /api/orders/admin/customer-status?customerId=...&status=... : Tìm theo KH và Trạng thái */
    @GetMapping("/admin/customer-status")
    public ResponseEntity<List<Order>> findByStatusAndCustomer(
            @RequestParam Long customerId,
            @RequestParam OrderStatus status) {

        Customer customer = customerService.findById(customerId);
        List<Order> orders = orderService.findByStatusAndCustomer(status, customer);
        return ResponseEntity.ok(orders);
    }

    // --- XỬ LÝ TRẠNG THÁI (WORKFLOW) ---

    /** * Helper method để tìm Employee hoặc trả về null */
    private Employee getEmployeeOrNull(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeService.findEmployeeById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Employee ID: " + employeeId));
    }


    /** * POST /api/orders/{id}/status: Cập nhật trạng thái (Dành cho NV) */
    @PostMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus newStatus,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String cancelReason
    ) {
        Employee employee = getEmployeeOrNull(employeeId);
        Order updatedOrder = orderService.updateStatus(id, newStatus, cancelReason, employee);
        return ResponseEntity.ok(updatedOrder);
    }

    /** * PUT /api/orders/{id}/cancel: Khách hàng tự hủy đơn hàng (Chỉ cho PENDING). */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelByCustomer(
            @PathVariable String id,
            @RequestParam(required = false) String cancelReason,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResourceNotFoundException("Yêu cầu xác thực để hủy đơn hàng.");
        }
        String username = principal.getName();
        Customer customer = customerService.findByAccountUsername(username);

        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy Khách hàng cho tài khoản này.");
        }

        Order canceledOrder = orderService.cancelByCustomer(id, cancelReason, customer);
        return ResponseEntity.ok(canceledOrder);
    }

    /** * POST /api/orders/{id}/return: Yêu cầu hoàn trả (Chỉ cho DELIVERED, Cần NV xác nhận) */
    @PostMapping("/{id}/return")
    public ResponseEntity<Order> requestReturn(
            @PathVariable String id,
            @RequestParam Long employeeId,
            @RequestParam(required = false) String reason
    ) {
        Employee employee = getEmployeeOrNull(employeeId);
        Order returnedOrder = orderService.requestReturn(id, reason, employee);
        return ResponseEntity.ok(returnedOrder);
    }

    /** * POST /api/orders/{id}/refund: Xử lý hoàn tiền (Chỉ cho RETURNED, Cần NV thực hiện) */
    @PostMapping("/{id}/refund")
    public ResponseEntity<Order> processRefund(
            @PathVariable String id,
            @RequestParam Long employeeId
    ) {
        Employee employee = getEmployeeOrNull(employeeId);
        Order refundedOrder = orderService.processRefund(id, employee);
        return ResponseEntity.ok(refundedOrder);
    }

    /** GET /api/orders/{id}/total: Tính toán lại tổng tiền */
    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calculateTotal(@PathVariable String id) {
        BigDecimal total = orderService.calculateTotal(id);
        return ResponseEntity.ok(total);
    }
}