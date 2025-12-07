package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.*;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.*;
import iuh.fit.se.cosmeticsecommercebackend.repository.AddressRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final AddressService addressService;
    private final ProductVariantService productVariantService;
    private final AddressRepository addressRepository;
    private final CartItemService cartItemService;

    public OrderServiceImpl(
            OrderRepository orderRepo,
            CustomerService customerService,
            EmployeeService employeeService,
            AddressService addressService,
            ProductVariantService productVariantService,
            AddressRepository addressRepository,
            CartItemService cartItemService
    ) {
        this.orderRepo = orderRepo;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.addressService = addressService;
        this.productVariantService = productVariantService;
        this.addressRepository = addressRepository;
        this.cartItemService = cartItemService;
    }

    /* ===================== ORDER ID ===================== */

    private String generateNewOrderId() {
        String today = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "OD-" + today;

        Optional<String> lastId = orderRepo.findLastOrderIdByDatePrefix(prefix);
        int seq = 1;

        if (lastId.isPresent()) {
            String num = lastId.get().substring(lastId.get().length() - 2);
            seq = Integer.parseInt(num) + 1;
        }

        return prefix + String.format("%02d", seq);
    }

    /* ===================== CREATE ORDER ===================== */

    @Override
    @Deprecated
    public Order createOrder(Order order) {
        throw new UnsupportedOperationException(
                "Không sử dụng createOrder(Order). " +
                        "Hãy dùng createOrderFromRequest(CreateOrderRequest)"
        );
    }

    @Override
    public CreateOrderResponse createOrderFromRequest(CreateOrderRequest request) {

        /* ===== CUSTOMER (CÓ THỂ NULL) ===== */
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerService.findById(request.getCustomerId());
            if (customer == null) {
                throw new ResourceNotFoundException("Không tìm thấy khách hàng");
            }
        }

        /* ===== ADDRESS ===== */
        Address address;

        if (customer == null) {
            // GUEST
            address = new Address();
            address.setFullName(request.getShippingFullName());
            address.setPhone(request.getShippingPhone());
            address.setAddress(request.getShippingAddress());
            address.setCity(request.getShippingCity());
            address.setState(request.getShippingState());
            address.setCountry(request.getShippingCountry());
            address.setCustomer(null);
            address.setDefault(false);
            address = addressRepository.save(address);
        } else {
            // CUSTOMER
            if (request.getAddressId() != null) {
                address = addressService.findById(request.getAddressId());
                if (address == null) {
                    throw new ResourceNotFoundException("Không tìm thấy địa chỉ");
                }
            } else {
                address = addressService.getDefaultAddressByCustomerId(customer.getId());
                if (address == null) {
                    throw new IllegalStateException("Khách hàng chưa có địa chỉ mặc định");
                }
            }
        }

        /* ===== VALIDATE ITEMS ===== */
        if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất 1 sản phẩm");
        }

        /* ===== CREATE ORDER ===== */
        Order order = new Order();
        order.setId(generateNewOrderId());
        order.setCustomer(customer);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal shippingFee =
                request.getShippingFee() != null
                        ? request.getShippingFee()
                        : new BigDecimal("30000");
        order.setShippingFee(shippingFee);

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (OrderDetailRequest d : request.getOrderDetails()) {

            ProductVariant variant = productVariantService.getById(d.getProductVariantId());
            if (variant == null) {
                throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
            }
            if (variant.getQuantity() < d.getQuantity()) {
                throw new IllegalStateException("Không đủ tồn kho");
            }

            OrderDetail od = new OrderDetail();
            od.setOrder(order);
            od.setProductVariant(variant);
            od.setQuantity(d.getQuantity());
            od.setUnitPrice(variant.getPrice());

            BigDecimal subtotal =
                    variant.getPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            od.setTotalPrice(subtotal);
            od.setDiscountAmount(BigDecimal.ZERO);

            details.add(od);
            itemsTotal = itemsTotal.add(subtotal);

            // trừ tồn kho
            variant.setQuantity(variant.getQuantity() - d.getQuantity());
        }

        order.setOrderDetails(details);

        BigDecimal discount =
                request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;

        order.setTotal(itemsTotal.add(shippingFee).subtract(discount));

        Order saved = orderRepo.save(order);

        // xoá cart item nếu có
        if (request.getCartItemIds() != null) {
            request.getCartItemIds()
                    .forEach(cartItemService::deleteCartItemById);
        }

        /* ===== RESPONSE ===== */
        CreateOrderResponse res = new CreateOrderResponse();
        res.setId(saved.getId());
        res.setCustomerId(customer != null ? customer.getId() : null);
        res.setAddressId(saved.getAddress().getId());
        res.setOrderDate(saved.getOrderDate());
        res.setStatus(saved.getStatus().name());
        res.setTotalAmount(saved.getTotal());
        res.setShippingFee(saved.getShippingFee());
        res.setDiscount(discount);

        List<OrderDetailResponse> resDetails = new ArrayList<>();
        for (OrderDetail od : saved.getOrderDetails()) {
            OrderDetailResponse r = new OrderDetailResponse();
            r.setId(od.getId());
            r.setProductVariantId(od.getProductVariant().getId());
            r.setQuantity(od.getQuantity());
            r.setPrice(od.getUnitPrice());
            r.setSubtotal(od.getTotalPrice());
            resDetails.add(r);
        }
        res.setOrderDetails(resDetails);

        return res;
    }

    /* ===================== READ ===================== */

    @Override
    public List<Order> getAll() {
        return orderRepo.findAll();
    }

    @Override
    public Order findById(String id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    }

    @Override
    public List<Order> getMyOrders(String username) {
        Customer customer = customerService.findByAccountUsername(username);
        if (customer == null) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng");
        }
        return orderRepo.findByCustomer(customer);
    }

    @Override
    public Order getCustomerOrderById(String orderId, String username) {
        Order order = findById(orderId);
        Customer customer = customerService.findByAccountUsername(username);

        if (customer == null
                || order.getCustomer() == null
                || !order.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng");
        }
        return order;
    }

    /* ===================== SEARCH ===================== */

    @Override public List<Order> findByCustomer(Customer c) { return orderRepo.findByCustomer(c); }
    @Override public List<Order> findByEmployee(Employee e) { return orderRepo.findByEmployee(e); }
    @Override public List<Order> findByStatus(OrderStatus s) { return orderRepo.findByStatus(s); }
    @Override public List<Order> findByOrderDateBetween(LocalDateTime s, LocalDateTime e) {
        return orderRepo.findByOrderDateBetween(s, e);
    }
    @Override public List<Order> findByStatusAndCustomer(OrderStatus s, Customer c) {
        return orderRepo.findByStatusAndCustomer(s, c);
    }
    @Override public List<Order> findByTotalBetween(BigDecimal min, BigDecimal max) {
        return orderRepo.findByTotalBetween(min, max);
    }

    /* ===================== STATUS ===================== */

    @Override
    public Order cancelByCustomer(String id, String reason, Customer customer) {
        Order o = findById(id);

        if (o.getCustomer() == null || !o.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Không có quyền huỷ đơn");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Không thể huỷ đơn ở trạng thái hiện tại");
        }

        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(reason);
        o.setCanceledAt(LocalDateTime.now());
        return orderRepo.save(o);
    }

    @Override
    public Order cancelByEmployee(String id, String reason, Employee employee) {
        Order o = findById(id);
        o.setStatus(OrderStatus.CANCELLED);
        o.setEmployee(employee);
        o.setCancelReason(reason);
        o.setCanceledAt(LocalDateTime.now());
        return orderRepo.save(o);
    }

    @Override
    public Order requestReturn(String id, String reason, Employee e) {
        Order o = findById(id);
        o.setStatus(OrderStatus.RETURNED);
        o.setEmployee(e);
        o.setCancelReason(reason);
        return orderRepo.save(o);
    }

    @Override
    public Order processRefund(String id, Employee e) {
        Order o = findById(id);
        o.setStatus(OrderStatus.REFUNDED);
        o.setEmployee(e);
        return orderRepo.save(o);
    }

    @Override
    public Order updateStatus(String id, OrderStatus s, String reason, Employee e) {
        Order o = findById(id);
        o.setStatus(s);
        o.setEmployee(e);
        o.setCancelReason(reason);
        return orderRepo.save(o);
    }

    /* ===================== TOTAL ===================== */

    @Override
    public BigDecimal calculateTotal(String id) {
        return findById(id).getTotal();
    }

    /* ===================== GẮN ĐƠN GUEST ===================== */

    @Override
    @Transactional
    public void linkGuestOrders(String phone, Customer customer) {
        if (phone == null || phone.isBlank() || customer == null) return;
        orderRepo.linkGuestOrdersToCustomer(customer, phone);
    }
}
