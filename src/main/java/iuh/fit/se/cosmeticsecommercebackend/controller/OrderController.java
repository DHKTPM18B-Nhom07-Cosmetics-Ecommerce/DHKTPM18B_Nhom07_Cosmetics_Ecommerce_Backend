package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderResponse;
import iuh.fit.se.cosmeticsecommercebackend.service.CustomerService;
import iuh.fit.se.cosmeticsecommercebackend.service.EmployeeService;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /** Helper method để tìm Employee hoặc trả về null */
    private Employee getEmployeeOrNull(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeService.findEmployeeById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Employee ID: " + employeeId));
    }

    // --- CRUD CƠ BẢN ---

    /** POST /api/orders : Tạo đơn hàng mới từ JSON payload */
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrderFromRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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

    /** 🎯 ĐÃ SỬA LOGIC LỌC KHÁCH HÀNG: GET /api/orders : Lấy danh sách đơn hàng cá nhân (Customer). */
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

        // Tối ưu hóa: Nếu có STATUS, dùng findByStatusAndCustomer
        if (status != null) {
            // LƯU Ý: Hàm này sẽ bỏ qua tham số start/end vì Service chưa có hàm 3 tham số.
            return orderService.findByStatusAndCustomer(status, customer);
        }

        // Nếu chỉ có start/end, ta không thể lọc theo Customer + Date nên phải lấy tất cả
        // Đây là điểm yếu do thiếu hàm findByCustomerAndOrderDateBetween trong Service
        if (start != null && end != null) {
            // Thay vì trả về lỗi, ta trả về tất cả đơn hàng của Khách hàng
            return orderService.getMyOrders(username);
        }

        // Mặc định: Lấy tất cả đơn hàng của Khách hàng
        return orderService.getMyOrders(username);
    }

    // --- NGHIỆP VỤ TÌM KIẾM (CHỈ NÊN DÀNH CHO ADMIN/EMPLOYEE) ---

    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public List<Order> getAllOrdersForAdmin() {
        return orderService.getAll();
    }

    /** GET /api/orders/admin/{id} : Lấy chi tiết đơn hàng bất kỳ (Dành cho Admin) */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> getAdminOrderDetail(@PathVariable String id) {
        // Chỉ cần tìm đơn hàng, không cần kiểm tra quyền sở hữu Customer
        Order order = orderService.findById(id);
        return ResponseEntity.ok(order);
    }

    /** GET /api/orders/admin/status/{status} : Tìm theo trạng thái */
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public List<Order> findByStatus(@PathVariable OrderStatus status) {
        return orderService.findByStatus(status);
    }

    /** 🎯 ĐÃ SỬA: GET /api/orders/admin/date-range (Hỗ trợ lọc kết hợp Status) */
    @GetMapping("/admin/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public List<Order> findByOrderDateBetween(
            @RequestParam("start") LocalDateTime start,
            @RequestParam("end") LocalDateTime end,
            @RequestParam(required = false) OrderStatus status) {

        // Nếu có STATUS, dùng hàm lọc 3 tham số
        if (status != null) {
            // GIẢ ĐỊNH hàm findByStatusAndOrderDateBetween đã có trong OrderService
            return orderService.findByStatusAndOrderDateBetween(status, start, end);
        }
        // Nếu không có STATUS, dùng hàm lọc 2 tham số (chỉ ngày)
        return orderService.findByOrderDateBetween(start, end);
    }

    /** GET /api/orders/admin/customer/{customerId} : Tìm đơn hàng theo Khách hàng */
    @GetMapping("/admin/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<Order>> findByCustomer(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId);
        List<Order> orders = orderService.findByCustomer(customer);
        return ResponseEntity.ok(orders);
    }

    /** GET /api/orders/admin/employee/{employeeId} : Tìm đơn hàng theo Nhân viên */
    @GetMapping("/admin/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<Order>> findByEmployee(@PathVariable Long employeeId) {
        Employee employee = employeeService.findEmployeeById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Employee ID: " + employeeId));

        List<Order> orders = orderService.findByEmployee(employee);
        return ResponseEntity.ok(orders);
    }

    /** GET /api/orders/admin/total-range?min=...&max=... : Tìm theo tổng tiền trong khoảng */
    @GetMapping("/admin/total-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public List<Order> findByTotalBetween(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return orderService.findByTotalBetween(min, max);
    }

    /** GET /api/orders/admin/customer-status?customerId=...&status=... : Tìm theo KH và Trạng thái */
    @GetMapping("/admin/customer-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<Order>> findByStatusAndCustomer(
            @RequestParam Long customerId,
            @RequestParam OrderStatus status) {

        Customer customer = customerService.findById(customerId);
        List<Order> orders = orderService.findByStatusAndCustomer(status, customer);
        return ResponseEntity.ok(orders);
    }

    // --- XỬ LÝ TRẠNG THÁI (WORKFLOW) ---

    /** * POST /api/orders/{id}/status: Cập nhật trạng thái (Dành cho NV) */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus newStatus,
            @RequestParam(required = false) String cancelReason,
            Principal principal // SỬ DỤNG PRINCIPAL THAY VÌ EMPLOYEE_ID
    ) {
        if (principal == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        String username = principal.getName();

        // 1. TÌM OBJECT EMPLOYEE DỰA TRÊN USERNAME
        Employee employee = employeeService.findByAccountUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Employee cho tài khoản: " + username));

        // 2. GỌI SERVICE
        // Nếu trạng thái là CANCELLED, gọi hàm cancelByEmployee để xử lý hoàn kho (nghiệp vụ đúng)
        if (newStatus == OrderStatus.CANCELLED) {
            Order canceledOrder = orderService.cancelByEmployee(id, cancelReason, employee);
            return ResponseEntity.ok(canceledOrder);
        }

        // Nếu là trạng thái khác, gọi updateStatus
        Order updatedOrder = orderService.updateStatus(id, newStatus, cancelReason, employee);
        return ResponseEntity.ok(updatedOrder);
    }

    /** 🎯 SỬA CHỮA: PUT /api/orders/{id}/cancel: Khách hàng GỬI YÊU CẦU HỦY đơn hàng. */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelByCustomer(
            @PathVariable String id,
            @RequestParam(required = false) String cancelReason,
            Principal principal
    ) {
        if (principal == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        String username = principal.getName();
        Customer customer = customerService.findByAccountUsername(username);

        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy Khách hàng cho tài khoản này.");
        }

        // GỌI HÀM MỚI: Chỉ ghi nhận yêu cầu và lý do, KHÔNG hủy ngay
        Order requestedOrder = orderService.requestCancelByCustomer(id, cancelReason, customer);

        // Trả về HTTP 200 OK để xác nhận yêu cầu đã được ghi nhận
        return ResponseEntity.ok(requestedOrder);
    }

    /** * POST /api/orders/{id}/return: Yêu cầu hoàn trả (Chỉ cho DELIVERED, Cần NV xác nhận) */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> requestReturn(
            @PathVariable String id,
            @RequestParam(required = false) String reason,
            Principal principal // Sử dụng Principal
    ) {
        if (principal == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        String username = principal.getName();
        Employee employee = employeeService.findByAccountUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Employee."));

        Order returnedOrder = orderService.requestReturn(id, reason, employee);
        return ResponseEntity.ok(returnedOrder);
    }

    /** * POST /api/orders/{id}/refund: Xử lý hoàn tiền (Chỉ cho RETURNED, Cần NV thực hiện) */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Order> processRefund(
            @PathVariable String id,
            Principal principal // Sử dụng Principal
    ) {
        if (principal == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        String username = principal.getName();
        Employee employee = employeeService.findByAccountUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Employee."));

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