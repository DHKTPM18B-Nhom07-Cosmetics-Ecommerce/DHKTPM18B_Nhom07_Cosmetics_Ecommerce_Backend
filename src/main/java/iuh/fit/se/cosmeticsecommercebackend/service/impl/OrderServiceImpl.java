package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.CustomerService;
import iuh.fit.se.cosmeticsecommercebackend.service.EmployeeService;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    @Autowired
    private iuh.fit.se.cosmeticsecommercebackend.service.RiskService riskService;
    public OrderServiceImpl(OrderRepository orderRepo,
                            CustomerService customerService,
                            EmployeeService employeeService) {
        this.orderRepo = orderRepo;
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    // ============================= TẠO ĐƠN HÀNG =============================

    @Override
    public Order createOrder(Order order) {
        // 1️⃣ Kiểm tra khách hàng
        if (order.getCustomer() == null || order.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Đơn hàng phải có khách hàng hợp lệ.");
        }

        Customer customer = customerService.findById(order.getCustomer().getId());
        order.setCustomer(customer);

        // 2️⃣ Gán thông tin mặc định
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        // Lưu ý: ShippingFee nên được gán ở đây nếu có logic phức tạp. Hiện tại, giả định nó được Entity gán mặc định.

        // 3️⃣ Gắn lại quan hệ 2 chiều và tính tổng tiền
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail detail : order.getOrderDetails()) {
                detail.setOrder(order);
                // Đảm bảo totalPrice trong OrderDetail được tính đúng
                if (detail.getUnitPrice() != null && detail.getQuantity() != null && detail.getQuantity() > 0) {
                    BigDecimal price = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                    // Trừ đi discountAmount (nếu có)
                    if (detail.getDiscountAmount() != null) {
                        price = price.subtract(detail.getDiscountAmount());
                    }
                    detail.setTotalPrice(price);
                } else {
                    detail.setTotalPrice(BigDecimal.ZERO);
                }
            }
        }

        // Gọi hàm tính tổng tiền cuối cùng (bao gồm cả phí vận chuyển, nếu có)
        // Lưu ý: Hàm calculateTotal(order) được giả định là có thể tính toán total mà không cần lưu trước.
        // Tuy nhiên, trong môi trường thực tế, ta thường tính toán và gán giá trị trước khi lưu.
        order.setTotal(calculateTotal(order));

        // 4️⃣ Lưu đơn hàng (cascade sẽ tự lưu OrderDetail)
        return orderRepo.save(order);
    }

    // ============================= CRUD CƠ BẢN =============================

    @Override
    public List<Order> getAll() {
        return orderRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng ID: " + id));
        if (order.getOrderDetails() != null) {
            order.getOrderDetails().size();
            order.getOrderDetails().forEach(detail -> {
                if (detail.getProductVariant() != null) {
                    detail.getProductVariant().getId();
                    // Buộc tải Product (nếu Product là LAZY trong ProductVariant)
                    // if (detail.getProductVariant().getProduct() != null) {
                    //    detail.getProductVariant().getProduct().getProductName();
                    // }
                }
            });
        }

        // 2. Buộc tải Address
        if (order.getAddress() != null) {
            // Tải các trường cần thiết cho frontend
            order.getAddress().getId();
            order.getAddress().getFullName();
            order.getAddress().getPhone();
            order.getAddress().getAddress();
            order.getAddress().getCity();
            order.getAddress().getState();
            order.getAddress().getCountry();
        }

        // 3. Buộc tải Customer
        if (order.getCustomer() != null) {
            order.getCustomer().getId();
        }

        return order;
    }

    @Override
    public Order updateOrder(Long id, Order orderDetails) {
        Order existing = findById(id);

        if (existing.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể chỉnh sửa đơn hàng khi trạng thái là PENDING. Trạng thái hiện tại: "
                    + existing.getStatus());
        }

        // Cần cập nhật lại chi tiết đơn hàng (OrderDetails), tổng tiền và có thể là Address.
        // Chỉ đơn giản cập nhật Total là không đủ.
        throw new UnsupportedOperationException("Cập nhật đơn hàng (ngoài Total) cần logic phức tạp (cập nhật OrderDetails, Address, v.v.).");

        // return orderRepo.save(existing);
    }

    // ============================= TRUY VẤN =============================
    // (Các phương thức truy vấn được giữ nguyên vì chúng gọi trực tiếp từ Repository)

    @Override
    public List<Order> findByCustomer(Customer customer) {
        return orderRepo.findByCustomer(customer);
    }

    @Override
    public List<Order> findByEmployee(Employee employee) {
        return orderRepo.findByEmployee(employee);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepo.findByStatus(status);
    }

    @Override
    public List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end) {
        return orderRepo.findByOrderDateBetween(start, end);
    }

    @Override
    public List<Order> findByStatusAndCustomer(OrderStatus status, Customer customer) {
        return orderRepo.findByStatusAndCustomer(status, customer);
    }

    @Override
    public List<Order> findByTotalBetween(BigDecimal min, BigDecimal max) {
        return orderRepo.findByTotalBetween(min, max);
    }

    // ============================= NGHIỆP VỤ TRẠNG THÁI (BỔ SUNG VÀ SỬA LỖI) =============================

    // 4. BỔ SUNG: Tính tổng tiền đơn hàng dựa trên chi tiết đơn hàng
    // Nhận Order thay vì ID để tái sử dụng trong createOrder
    public BigDecimal calculateTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;

        // 1. Tính tổng từ OrderDetails
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                // Kiểm tra null và cộng totalPrice (đã tính ở createOrder)
                if (detail.getTotalPrice() != null) {
                    total = total.add(detail.getTotalPrice());
                }
            }
        }

        // 2. Cộng phí vận chuyển (Shipping Fee)
        // Giả định order.getShippingFee() có sẵn và không null
        if (order.getShippingFee() != null) {
            total = total.add(order.getShippingFee());
        }

        // Lưu ý: Cần thêm logic xử lý giảm giá toàn đơn từ VoucherRedemption ở đây.

        return total;
    }

    //calculateTotal
    @Override
    public BigDecimal calculateTotal(Long orderId) {
        Order order = findById(orderId);
        return calculateTotal(order);
    }

    //  Khách hàng hủy đơn hàng
    // Khách hàng hủy đơn hàng
    @Override
    public Order cancelByCustomer(Long orderId, String cancelReason, Customer customer) {
        Order order = findById(orderId);

        // Kiểm tra quyền sở hữu
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Khách hàng không có quyền hủy đơn hàng này.");
        }

        // Kiểm tra trạng thái cho phép hủy
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Khách hàng chỉ có thể hủy đơn hàng khi trạng thái là PENDING. Trạng thái hiện tại: "
                    + order.getStatus());
        }

        // Thực hiện hủy
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);
        order.setCanceledAt(LocalDateTime.now());

        // Lưu đơn hàng đã hủy xuống DB trước
        Order savedOrder = orderRepo.save(order);

        // ==================================================================
        // [THÊM KÉ]: GỌI RISK SERVICE ĐỂ KIỂM TRA SPAM VÀ BÁO ĐỘNG
        // ==================================================================
        try {
            if (customer.getAccount() != null) {
                // Hàm này chỉ tính toán trong RAM và gửi mail, KHÔNG ghi xuống DB
                riskService.checkAndAlertOrderSpam(
                        customer.getAccount().getId(),
                        customer.getAccount().getUsername()
                );
            }
        } catch (Exception e) {
            // Bắt lỗi để nếu RiskService có vấn đề thì cũng không làm lỗi chức năng hủy đơn
            System.err.println("Lỗi check risk (bỏ qua): " + e.getMessage());
        }
        // ==================================================================

        return savedOrder;
    }


    // Nhân viên hủy đơn hàng
    @Override
    public Order cancelByEmployee(Long id, String cancelReason, Employee employee) {
        // Kiểm tra Employee
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Nhân viên xác nhận hủy đơn hàng không hợp lệ.");
        }
        Employee emp = employeeService.findEmployeeById(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + employee.getId()));

        Order order = findById(id);

        if (order.getStatus() == OrderStatus.SHIPPING ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Không thể hủy đơn hàng đang giao, đã giao hoặc đã hoàn tiền.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(cancelReason);
        order.setCanceledAt(LocalDateTime.now());
        order.setEmployee(emp); // Gán nhân viên hủy

        return orderRepo.save(order);
    }
    //trả đơn
    @Override
    public Order requestReturn(Long id, String reason, Employee employee) {
        Order order = findById(id);

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Chỉ được hoàn trả đơn hàng đã giao thành công.");
        }

        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Yêu cầu hoàn trả phải do nhân viên xác nhận.");
        }

        Employee emp = employeeService.findEmployeeById(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + employee.getId()));
        order.setStatus(OrderStatus.RETURNED);
        order.setEmployee(emp);
        order.setCancelReason(reason); // Dùng lại cancelReason cho lý do hoàn trả

        return orderRepo.save(order);
    }

    //Hoàn tiền đơn hàng
    @Override
    public Order processRefund(Long id, Employee employee) {
        Order order = findById(id);

        if (order.getStatus() != OrderStatus.RETURNED) {
            throw new IllegalStateException("Chỉ hoàn tiền cho đơn hàng đã được hoàn trả (RETURNED).");
        }

        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Hoàn tiền phải do nhân viên thực hiện.");
        }

        Employee emp = employeeService.findEmployeeById(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + employee.getId()));
        order.setStatus(OrderStatus.REFUNDED);
        order.setEmployee(emp);
        return orderRepo.save(order);
    }

    // Cập nhật trạng thái đơn hàng với kiểm tra vai trò và trạng thái hợp lệ
    @Override
    public Order updateStatus(Long id, OrderStatus newStatus, String cancelReason, Employee employee) {
        Order order = findById(id);
        OrderStatus current = order.getStatus();

        // 1. Nếu là HỦY, gọi phương thức hủy chuyên biệt (cancelByEmployee)
        if (newStatus == OrderStatus.CANCELLED) {
            // Yêu cầu phải có nhân viên để cập nhật trạng thái
            if (employee == null || employee.getId() == null) {
                throw new IllegalArgumentException("Hủy đơn hàng phải do nhân viên thực hiện.");
            }
            return cancelByEmployee(id, cancelReason, employee);
        }

        // 2. Kiểm tra vai trò và trạng thái chuyển đổi
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Thay đổi trạng thái sang " + newStatus + " phải do nhân viên thực hiện.");
        }

        Employee emp = employeeService.findEmployeeById(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + employee.getId()));

        // Logic kiểm tra chuyển đổi trạng thái tuần tự
        switch (newStatus) {
            case CONFIRMED -> {
                if (current != OrderStatus.PENDING)
                    throw new IllegalStateException("Chỉ có thể xác nhận từ PENDING.");
            }
            case PROCESSING -> {
                if (current != OrderStatus.CONFIRMED)
                    throw new IllegalStateException("Chỉ có thể xử lý từ CONFIRMED.");
            }
            case SHIPPING -> {
                if (current != OrderStatus.PROCESSING)
                    throw new IllegalStateException("Chỉ có thể giao hàng từ PROCESSING.");
            }
            case DELIVERED -> {
                if (current != OrderStatus.SHIPPING)
                    throw new IllegalStateException("Chỉ có thể đánh dấu giao hàng từ SHIPPING.");
            }
            default -> {
                throw new IllegalStateException("Trạng thái chuyển đổi không hợp lệ hoặc cần sử dụng hàm chuyên biệt.");
            }
        }

        order.setEmployee(emp);
        order.setStatus(newStatus);
        return orderRepo.save(order);
    }
}