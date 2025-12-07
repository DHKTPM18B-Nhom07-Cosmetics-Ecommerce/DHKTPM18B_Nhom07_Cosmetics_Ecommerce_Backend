package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.*;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderResponse;
import iuh.fit.se.cosmeticsecommercebackend.payload.OrderDetailRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.OrderDetailResponse;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final AddressService addressService;
    private final ProductVariantService productVariantService;

    @Autowired
    private iuh.fit.se.cosmeticsecommercebackend.service.RiskService riskService;

    @Autowired
    private iuh.fit.se.cosmeticsecommercebackend.service.CartItemService cartItemService;

    @Autowired
    private iuh.fit.se.cosmeticsecommercebackend.repository.AddressRepository addressRepository;

    public OrderServiceImpl(OrderRepository orderRepo,
                            CustomerService customerService,
                            EmployeeService employeeService,
                            AddressService addressService,
                            ProductVariantService productVariantService) {
        this.orderRepo = orderRepo;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.addressService = addressService;
        this.productVariantService = productVariantService;
    }

    /**
     * Logic phát sinh ID đơn hàng theo format OD-yyyymmdd[số thứ tự 2 chữ số].
     * Hàm này phải được gọi trong @Transactional để đảm bảo tính nhất quán.
     */
    private String generateNewOrderId() {
        // Định dạng ngày: yyyyMMdd
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "OD-" + today; // Ví dụ: OD-20251205

        Optional<String> lastIdOptional = orderRepo.findLastOrderIdByDatePrefix(prefix);

        int sequence = 1;

        if (lastIdOptional.isPresent()) {
            String lastId = lastIdOptional.get();
            try {
                String sequenceStr = lastId.substring(lastId.length() - 2);
                sequence = Integer.parseInt(sequenceStr) + 1;
            } catch (NumberFormatException e) {
                System.err.println("Lỗi parse ID đơn hàng: " + lastId);
                sequence = 1;
            }
        }

        String newSequence = String.format("%02d", sequence);

        return prefix + newSequence;
    }

    /**
     * Buộc tải các thuộc tính cần thiết cho danh sách đơn hàng (list view).
     * Dùng chung cho tất cả các phương thức trả về List<Order>.
     */
    private void forceLoadOrderListDetails(List<Order> orders) {
        for (Order order : orders) {
            // 1. Buộc tải Customer và Account (để lấy tên Khách hàng)
            if (order.getCustomer() != null && order.getCustomer().getAccount() != null) {
                order.getCustomer().getAccount().getFullName();
            }

            // 2. Buộc tải OrderDetails và Product/Image
            if (order.getOrderDetails() != null) {
                order.getOrderDetails().size();

                order.getOrderDetails().forEach(detail -> {
                    if (detail.getProductVariant() != null) {
                        detail.getProductVariant().getId();

                        // Bổ sung: Buộc tải Product (chứa tên)
                        if (detail.getProductVariant().getProduct() != null) {
                            detail.getProductVariant().getProduct().getName();
                        }
                        // Bổ sung: Buộc tải ImageUrls (ElementCollection trong ProductVariant)
                        if (detail.getProductVariant().getImageUrls() != null) {
                            detail.getProductVariant().getImageUrls().size();
                        }
                    }
                });
            }
            // 3. Buộc tải Address
            if (order.getAddress() != null) {
                order.getAddress().getAddress();
            }
            // 4. Buộc tải Employee (nếu có)
            if (order.getEmployee() != null) {
                order.getEmployee().getId();
            }
        }
    }


    // ============================= TẠO ĐƠN HÀNG =============================

    @Override
    public Order createOrder(Order order) {
        // ... (Giữ nguyên logic tạo Order)
        if (order.getCustomer() == null || order.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Đơn hàng phải có khách hàng hợp lệ.");
        }

        Customer customer = customerService.findById(order.getCustomer().getId());
        order.setCustomer(customer);

        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        order.setId(generateNewOrderId());

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail detail : order.getOrderDetails()) {
                detail.setOrder(order);
                if (detail.getUnitPrice() != null && detail.getQuantity() != null && detail.getQuantity() > 0) {
                    BigDecimal price = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                    if (detail.getDiscountAmount() != null) {
                        price = price.subtract(detail.getDiscountAmount());
                    }
                    detail.setTotalPrice(price);
                } else {
                    detail.setTotalPrice(BigDecimal.ZERO);
                }
            }
        }

        order.setTotal(calculateTotal(order));

        return orderRepo.save(order);
    }

    /**
     * Tạo đơn hàng từ DTO request (dành cho Frontend gửi JSON payload)
     */
    @Override
    @Transactional
    public CreateOrderResponse createOrderFromRequest(CreateOrderRequest request) {
        Customer customer = null;
        Address address = null;
        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            customer = customerService.findById(request.getCustomerId());
            if (customer == null) {
                throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + request.getCustomerId());
            }

        }
        if (request.getCustomerId() == null || request.getCustomerId() == 0) {
            address = new Address();
            address.setId(Address.generateAddressId());
            address.setFullName(request.getShippingFullName());
            address.setPhone(request.getShippingPhone());
            address.setAddress(request.getShippingAddress());
            address.setCity(request.getShippingCity());
            address.setState(request.getShippingState());
            address.setCountry(request.getShippingCountry());
            address.setCustomer(null);
            address.setDefault(false);
            address = addressRepository.save(address);
        } else if (request.getAddressId() != null) {
            address = addressService.findById(request.getAddressId());
            if (address == null) {
                throw new ResourceNotFoundException("Không tìm thấy địa chỉ với ID: " + request.getAddressId());
            }
        } else {
            address = new Address();
            // Tìm địa chỉ mặc định của khách
            address = customer.getAddresses().stream()
                    .filter(Address::isDefault)
                    .findFirst()
                    .orElse(null);
            if (address == null) {
                throw new ResourceNotFoundException("Khách hàng chưa có địa chỉ mặc định. Vui lòng cập nhật địa chỉ mặc định trước khi đặt hàng.");
            }
        }
        // Nếu không đăng nhập thì bỏ qua address (address = null)

        // 3️⃣ Validation: Kiểm tra orderDetails không rỗng
        if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }

        // 4️⃣ Tạo Order entity
        Order order = new Order();
        order.setId(generateNewOrderId());
        order.setCustomer(customer); // null nếu không đăng nhập hoặc customerId = 0
        order.setAddress(address);   // luôn lưu address
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // Xử lý shipping fee và discount
        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : new BigDecimal("30000.00");
        order.setShippingFee(shippingFee);

        // 5️⃣ Xử lý OrderDetails với validation
        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (OrderDetailRequest detailRequest : request.getOrderDetails()) {
            // Validation: Kiểm tra productVariantId
            if (detailRequest.getProductVariantId() == null) {
                throw new IllegalArgumentException("productVariantId không được để trống");
            }

            ProductVariant productVariant = productVariantService.getById(detailRequest.getProductVariantId());
            if (productVariant == null) {
                throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + detailRequest.getProductVariantId());
            }

            // Validation: Kiểm tra số lượng
            if (detailRequest.getQuantity() == null || detailRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }

            // Validation: Kiểm tra tồn kho
            if (productVariant.getQuantity() < detailRequest.getQuantity()) {
                throw new IllegalArgumentException(
                        String.format("Sản phẩm '%s' không đủ số lượng. Còn lại: %d, yêu cầu: %d",
                                productVariant.getVariantName(),
                                productVariant.getQuantity(),
                                detailRequest.getQuantity())
                );
            }

            // Tạo OrderDetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProductVariant(productVariant);
            orderDetail.setQuantity(detailRequest.getQuantity());

            // Sử dụng giá từ ProductVariant thay vì tin tưởng hoàn toàn FE
            BigDecimal unitPrice = productVariant.getPrice();
            orderDetail.setUnitPrice(unitPrice);

            // Tính totalPrice cho OrderDetail
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(detailRequest.getQuantity()));
            orderDetail.setTotalPrice(totalPrice);
            orderDetail.setDiscountAmount(BigDecimal.ZERO);

            orderDetails.add(orderDetail);
            calculatedTotal = calculatedTotal.add(totalPrice);

            // Giảm tồn kho
            productVariant.setQuantity(productVariant.getQuantity() - detailRequest.getQuantity());
        }

        // 6️⃣ Gán OrderDetails vào Order
        order.setOrderDetails(orderDetails);

        // 7️⃣ Tính tổng tiền cuối cùng (bao gồm shipping fee, trừ discount nếu có)
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalTotal = calculatedTotal.add(shippingFee).subtract(discount);
        order.setTotal(finalTotal);

        // 8️⃣ Lưu đơn hàng (cascade sẽ tự lưu OrderDetail)
        Order savedOrder = orderRepo.save(order);

        // 9️⃣ Tạo response DTO
        CreateOrderResponse response = new CreateOrderResponse();
        response.setId(savedOrder.getId());
        response.setCustomerId(savedOrder.getCustomer() != null ? savedOrder.getCustomer().getId() : null);
        response.setAddressId(savedOrder.getAddress() != null ? savedOrder.getAddress().getId() : null);
        response.setOrderDate(savedOrder.getOrderDate());
        response.setStatus(savedOrder.getStatus().name());
        response.setTotalAmount(savedOrder.getTotal());
        response.setShippingFee(savedOrder.getShippingFee());
        response.setDiscount(discount);

        // Map OrderDetails sang OrderDetailResponse
        List<OrderDetailResponse> detailResponses = new ArrayList<>();
        for (OrderDetail detail : savedOrder.getOrderDetails()) {
            OrderDetailResponse detailResponse = new OrderDetailResponse();
            detailResponse.setId(detail.getId());
            detailResponse.setProductVariantId(detail.getProductVariant().getId());
            detailResponse.setQuantity(detail.getQuantity());
            detailResponse.setPrice(detail.getUnitPrice());
            detailResponse.setSubtotal(detail.getTotalPrice());
            detailResponses.add(detailResponse);
        }
        response.setOrderDetails(detailResponses);

        // Xóa các CartItem tương ứng sau khi tạo đơn hàng thành công
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            for (Long cartItemId : request.getCartItemIds()) {
                cartItemService.deleteCartItemById(cartItemId);
            }
        }

        return response;
    }

    @Override
    public List<Order> findByStatusAndOrderDateBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepo.findByStatusAndOrderDateBetween(status, startDate, endDate);
        // Rất quan trọng: Phải gọi hàm buộc tải để có Tên và Ảnh sản phẩm
        forceLoadOrderListDetails(orders);
        return orders;
    }
    // ============================= CRUD CƠ BẢN =============================

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAll() {
        List<Order> orders = orderRepo.findAll();
        // SỬA: Sử dụng hàm buộc tải chung
        forceLoadOrderListDetails(orders);
        return orders;
    }

    /**
     * Phương thức dùng nội bộ hoặc Admin: Lấy đơn hàng theo ID và buộc tải Product.
     */
    @Override
    @Transactional(readOnly = true)
    public Order findById(String id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng ID: " + id));
        if (order.getOrderDetails() != null) {
            order.getOrderDetails().size();
            order.getOrderDetails().forEach(detail -> {
                if (detail.getProductVariant() != null) {
                    detail.getProductVariant().getId();

                    // ĐÃ SỬA: Buộc tải Product (nơi chứa tên sản phẩm)
                    if (detail.getProductVariant().getProduct() != null) {
                        // Truy cập getName() để buộc tải Product Entity
                        detail.getProductVariant().getProduct().getName();
                    }
                    // ĐÃ SỬA: Buộc tải danh sách ảnh (imageUrls trong ProductVariant)
                    if (detail.getProductVariant().getImageUrls() != null) {
                        detail.getProductVariant().getImageUrls().size();
                    }
                }
            });
        }

        // 2. Buộc tải Address
        if (order.getAddress() != null) {
            order.getAddress().generateAddressId();
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

    /**
     * TRIỂN KHAI PHƯƠNG THỨC BỊ THIẾU 1: Lấy chi tiết đơn hàng cho Khách hàng, có kiểm tra quyền sở hữu.
     */
    @Override
    @Transactional(readOnly = true)
    public Order getCustomerOrderById(String orderId, String username) {
        // 1. Tìm Order bằng findById (đã có logic buộc tải)
        Order order = findById(orderId);

        // 2. Lấy Customer Entity từ username (từ JWT)
        Customer customer = customerService.findByAccountUsername(username);

        if (customer == null || !order.getCustomer().getId().equals(customer.getId())) {
            // Ném lỗi 404 để không tiết lộ sự tồn tại của đơn hàng khác
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng ID: " + orderId);
        }

        return order; // Trả về đơn hàng sau khi xác minh quyền sở hữu
    }


    // ============================= TRUY VẤN =============================

    @Override
    public List<Order> findByCustomer(Customer customer) {
        List<Order> orders = orderRepo.findByCustomer(customer);
        forceLoadOrderListDetails(orders);
        return orders;
    }

    @Override
    public List<Order> findByEmployee(Employee employee) {
        List<Order> orders = orderRepo.findByEmployee(employee);
        forceLoadOrderListDetails(orders);
        return orders;
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        List<Order> orders = orderRepo.findByStatus(status);
        forceLoadOrderListDetails(orders);
        return orders;
    }

    @Override
    public List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepo.findByOrderDateBetween(start, end);
        forceLoadOrderListDetails(orders);
        return orders;
    }

    @Override
    public List<Order> findByStatusAndCustomer(OrderStatus status, Customer customer) {
        List<Order> orders = orderRepo.findByStatusAndCustomer(status, customer);
        forceLoadOrderListDetails(orders);
        return orders;
    }

    @Override
    public List<Order> findByTotalBetween(BigDecimal min, BigDecimal max) {
        List<Order> orders = orderRepo.findByTotalBetween(min, max);
        forceLoadOrderListDetails(orders);
        return orders;
    }

    /**
     * TRIỂN KHAI PHƯƠNG THỨC BỊ THIẾU 2: Lấy danh sách đơn hàng cá nhân, có buộc tải Product.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> getMyOrders(String username) {

        // Giả định: customerService.findByAccountUsername(username) hoạt động
        Customer customer = customerService.findByAccountUsername(username);

        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy Khách hàng với Username: " + username);
        }

        List<Order> orders = orderRepo.findByCustomer(customer);

        // SỬA: Sử dụng hàm buộc tải chung
        forceLoadOrderListDetails(orders);

        return orders;
    }

    // ============================= NGHIỆP VỤ TRẠNG THÁI =============================

    // 4. Tính tổng tiền đơn hàng dựa trên chi tiết đơn hàng
    public BigDecimal calculateTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;

        // 1. Tính tổng từ OrderDetails
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                if (detail.getTotalPrice() != null) {
                    total = total.add(detail.getTotalPrice());
                }
            }
        }

        // 2. Cộng phí vận chuyển (Shipping Fee)
        if (order.getShippingFee() != null) {
            total = total.add(order.getShippingFee());
        }

        // Lưu ý: Cần thêm logic xử lý giảm giá toàn đơn từ VoucherRedemption ở đây.

        return total;
    }

    //calculateTotal
    @Override
    public BigDecimal calculateTotal(String orderId) {
        Order order = findById(orderId);
        return calculateTotal(order);
    }

    // Khách hàng hủy đơn hàng
    @Override
    public Order cancelByCustomer(String orderId, String cancelReason, Customer customer) {
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


        Order savedOrder = orderRepo.save(order);
        try {
            if (customer.getAccount() != null) {
                // Hàm này chỉ tính toán trong RAM và gửi mail, KHÔNG ghi xuống DB
                riskService.checkAndAlertOrderSpam(
                        customer.getAccount().getId(),
                        customer.getAccount().getUsername()
                );
            }
        } catch (Exception e) {

            System.err.println("Lỗi check risk (bỏ qua): " + e.getMessage());
        }
        // ==================================================================

        return savedOrder;
    }


    // Nhân viên hủy đơn hàng
    @Override
    public Order cancelByEmployee(String id, String cancelReason, Employee employee) {
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
    public Order requestReturn(String id, String reason, Employee employee) {
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
    public Order processRefund(String id, Employee employee) {
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
    public Order updateStatus(String id, OrderStatus newStatus, String cancelReason, Employee employee) {
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