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

    /** GET /api/orders/{id} : Lấy thông tin đơn hàng theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(order);
    }
    /** GET /api/orders : Lấy tất cả đơn hàng */
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAll();
    }

    /** PUT /api/orders/{id} : Cập nhật đơn hàng (Chỉ cho phép PENDING) */
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Order updatedOrder = orderService.updateOrder(id, orderDetails);
        return ResponseEntity.ok(updatedOrder);
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
            @RequestParam("start") LocalDateTime start,
            @RequestParam("end") LocalDateTime end) {
        // Spring Boot có thể tự động parse LocalDateTime nếu dùng format chuẩn ISO 8601
        return orderService.findByOrderDateBetween(start, end);
    }
    /** GET /api/orders/search/customer/{customerId} : Tìm đơn hàng theo Khách hàng */
    @GetMapping("/search/customer/{customerId}")
    public ResponseEntity<List<Order>> findByCustomer(@PathVariable Long customerId) {
        // FindById sẽ ném ResourceNotFoundException nếu không tìm thấy
        Customer customer = customerService.findById(customerId);
        List<Order> orders = orderService.findByCustomer(customer);
        return ResponseEntity.ok(orders);
    }

    /** GET /api/orders/search/employee/{employeeId} : Tìm đơn hàng theo Nhân viên */
    @GetMapping("/search/employee/{employeeId}")
    public ResponseEntity<List<Order>> findByEmployee(@PathVariable Long employeeId) {
        // FindById sẽ ném ResourceNotFoundException nếu không tìm thấy
        Employee employee = employeeService.findEmployeeById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Employee ID: " + employeeId));

        List<Order> orders = orderService.findByEmployee(employee);
        return ResponseEntity.ok(orders);
    }

    /** GET /api/orders/search/total-range?min=...&max=... : Tìm theo tổng tiền trong khoảng */
    @GetMapping("/search/total-range")
    public List<Order> findByTotalBetween(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        // Spring tự động parse BigDecimal
        return orderService.findByTotalBetween(min, max);
    }

    /** GET /api/orders/search/customer-status?customerId=...&status=... : Tìm theo KH và Trạng thái */
    @GetMapping("/search/customer-status")
    public ResponseEntity<List<Order>> findByStatusAndCustomer(
            @RequestParam Long customerId,
            @RequestParam OrderStatus status) {

        Customer customer = customerService.findById(customerId);
        List<Order> orders = orderService.findByStatusAndCustomer(status, customer);
        return ResponseEntity.ok(orders);
    }

    // --- XỬ LÝ TRẠNG THÁI (WORKFLOW) ---

    /** * Helper method để tìm Employee hoặc trả về null
     * Chú ý: Vì logic kiểm tra employeeId đã nằm trong Service, ta chỉ cần tìm nếu ID có.
     */
    private Employee getEmployeeOrNull(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeService.findEmployeeById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Employee ID: " + employeeId));
    }


    /** * POST /api/orders/{id}/status
     * Cập nhật trạng thái (Dành cho NV)
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus newStatus,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String cancelReason
    ) {
        Employee employee = getEmployeeOrNull(employeeId);

        Order updatedOrder = orderService.updateStatus(
                id,
                newStatus,
                cancelReason,
                employee
        );
        return ResponseEntity.ok(updatedOrder);
    }

    /** * POST /api/orders/{id}/cancel/customer
     * Khách hàng tự hủy đơn hàng (Chỉ cho PENDING)
     */
    @PostMapping("/{id}/cancel/customer")
    public ResponseEntity<Order> cancelByCustomer(
            @PathVariable Long id,
            @RequestParam(required = false) String cancelReason,
            @RequestParam Long customerId // Giả định Customer ID được truyền vào hoặc lấy từ token
    ) {
        Customer customer = customerService.findById(customerId);

        Order canceledOrder = orderService.cancelByCustomer(
                id,
                cancelReason,
                customer
        );
        return ResponseEntity.ok(canceledOrder);
    }

    /** * POST /api/orders/{id}/return
     * Yêu cầu hoàn trả (Chỉ cho DELIVERED, Cần NV xác nhận)
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<Order> requestReturn(
            @PathVariable Long id,
            @RequestParam Long employeeId,
            @RequestParam(required = false) String reason
    ) {
        Employee employee = getEmployeeOrNull(employeeId);

        Order returnedOrder = orderService.requestReturn(id, reason, employee);
        return ResponseEntity.ok(returnedOrder);
    }

    /** * POST /api/orders/{id}/refund
     * Xử lý hoàn tiền (Chỉ cho RETURNED, Cần NV thực hiện)
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<Order> processRefund(
            @PathVariable Long id,
            @RequestParam Long employeeId
    ) {
        Employee employee = getEmployeeOrNull(employeeId);

        Order refundedOrder = orderService.processRefund(id, employee);
        return ResponseEntity.ok(refundedOrder);
    }

    /**
     * GET /api/orders/{id}/total
     * Tính toán lại tổng tiền
     */
    @GetMapping("/{id}/total")
    public ResponseEntity<BigDecimal> calculateTotal(@PathVariable Long id) {
        BigDecimal total = orderService.calculateTotal(id);
        return ResponseEntity.ok(total);
    }

}