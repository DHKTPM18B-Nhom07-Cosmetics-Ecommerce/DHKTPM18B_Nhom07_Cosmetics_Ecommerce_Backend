package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.*;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderResponse;
import iuh.fit.se.cosmeticsecommercebackend.payload.OrderDetailRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.OrderDetailResponse;
import iuh.fit.se.cosmeticsecommercebackend.repository.AddressRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final OrderDetailService orderDetailService;

    @Autowired
    private RiskService riskService;

    @Autowired(required = false)
    private MailService mailService; // optional

    public OrderServiceImpl(
            OrderRepository orderRepo,
            CustomerService customerService,
            EmployeeService employeeService,
            AddressService addressService,
            ProductVariantService productVariantService,
            AddressRepository addressRepository,
            CartItemService cartItemService,
            OrderDetailService orderDetailService
    ) {
        this.orderRepo = orderRepo;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.addressService = addressService;
        this.productVariantService = productVariantService;
        this.addressRepository = addressRepository;
        this.cartItemService = cartItemService;
        this.orderDetailService = orderDetailService;
    }

    /* ===================== ORDER ID ===================== */

    private String generateNewOrderId() {
        String today = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "OD-" + today;

        Optional<String> lastId = orderRepo.findLastOrderIdByDatePrefix(prefix);
        int seq = lastId
                .map(id -> Integer.parseInt(id.substring(id.length() - 2)) + 1)
                .orElse(1);

        return prefix + String.format("%02d", seq);
    }

    /* ===================== CREATE ===================== */

    @Override
    public CreateOrderResponse createOrderFromRequest(CreateOrderRequest request) {

        Customer customer = null;
        Address address;

        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            customer = customerService.findById(request.getCustomerId());
        }

        // ===== ADDRESS =====
        if (customer == null) {
            address = new Address();
            address.setId(Address.generateAddressId());
            address.setFullName(request.getShippingFullName());
            address.setPhone(request.getShippingPhone());
            address.setAddress(request.getShippingAddress());
            address.setCity(request.getShippingCity());
            address.setState(request.getShippingState());
            address.setCountry(request.getShippingCountry());
            address = addressRepository.save(address);
        } else if (request.getAddressId() != null) {
            address = addressService.findById(request.getAddressId());
        } else {
            address = customer.getAddresses().stream()
                    .filter(Address::isDefault)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Chưa có địa chỉ mặc định"));
        }

        if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng rỗng");
        }

        Order order = new Order();
        order.setId(generateNewOrderId());
        order.setCustomer(customer);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingFee(
                request.getShippingFee() != null ? request.getShippingFee() : new BigDecimal("30000")
        );

        // ✅ guest phone
        if (customer == null) {
            order.setGuestPhone(normalizePhone(request.getShippingPhone()));
        }

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDetailRequest dr : request.getOrderDetails()) {

            ProductVariant pv = productVariantService.getById(dr.getProductVariantId());
            if (pv.getQuantity() < dr.getQuantity()) {
                throw new IllegalStateException("Không đủ tồn kho");
            }

            OrderDetail od = new OrderDetail();
            od.setOrder(order);
            od.setProductVariant(pv);
            od.setQuantity(dr.getQuantity());
            od.setUnitPrice(pv.getPrice());

            BigDecimal lineTotal = pv.getPrice()
                    .multiply(BigDecimal.valueOf(dr.getQuantity()));
            od.setTotalPrice(lineTotal);

            subtotal = subtotal.add(lineTotal);
            pv.setQuantity(pv.getQuantity() - dr.getQuantity());

            details.add(od);
        }

        order.setOrderDetails(details);

        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        order.setTotal(subtotal.add(order.getShippingFee()).subtract(discount));

        Order saved = orderRepo.save(order);

        // mail optional
        if (mailService != null && saved.getCustomer() != null && saved.getCustomer().getAccount() != null) {
            try {
                mailService.sendOrderConfirmationEmail(
                        saved.getCustomer().getAccount().getUsername(),
                        saved
                );
            } catch (Exception ignored) {}
        }

        CreateOrderResponse res = new CreateOrderResponse();
        res.setId(saved.getId());
        res.setStatus(saved.getStatus().name());
        res.setTotalAmount(saved.getTotal());

        List<OrderDetailResponse> drs = new ArrayList<>();
        for (OrderDetail d : saved.getOrderDetails()) {
            OrderDetailResponse r = new OrderDetailResponse();
            r.setId(d.getId());
            r.setProductVariantId(d.getProductVariant().getId());
            r.setQuantity(d.getQuantity());
            r.setPrice(d.getUnitPrice());
            r.setSubtotal(d.getTotalPrice());
            drs.add(r);
        }
        res.setOrderDetails(drs);

        if (request.getCartItemIds() != null) {
            request.getCartItemIds().forEach(cartItemService::deleteCartItemById);
        }

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
    }

    @Override
    public List<Order> getMyOrders(String username) {
        Customer c = customerService.findByAccountUsername(username);
        return orderRepo.findByCustomer(c);
    }

    @Override
    public Order getCustomerOrderById(String orderId, String username) {
        Order o = findById(orderId);
        Customer c = customerService.findByAccountUsername(username);
        if (o.getCustomer() == null || !o.getCustomer().getId().equals(c.getId())) {
            throw new ResourceNotFoundException("Không có quyền");
        }
        return o;
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
    @Override public List<Order> findByStatusAndOrderDateBetween(OrderStatus s, LocalDateTime st, LocalDateTime ed) {
        return orderRepo.findByStatusAndOrderDateBetween(s, st, ed);
    }

    /* ===================== STATUS ===================== */

    @Override
    public Order cancelByCustomer(String orderId, String reason, Customer customer) {

        Order o = findById(orderId);

        if (o.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn PENDING");
        }

        orderDetailService.restoreStockForOrder(orderId);

        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(reason);
        o.setCanceledAt(LocalDateTime.now());

        if (customer.getAccount() != null) {
            riskService.checkAndAlertOrderSpam(
                    customer.getAccount().getId(),
                    customer.getAccount().getUsername()
            );
        }
        return orderRepo.save(o);
    }

    @Override
    public Order cancelByEmployee(String id, String reason, Employee employee) {
        Order o = findById(id);
        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(reason);
        o.setEmployee(employee);
        o.setCanceledAt(LocalDateTime.now());
        return orderRepo.save(o);
    }

    @Override
    public Order requestReturn(String id, String reason, Employee employee) {
        Order o = findById(id);
        o.setStatus(OrderStatus.RETURNED);
        o.setEmployee(employee);
        o.setCancelReason(reason);
        return orderRepo.save(o);
    }

    @Override
    public Order processRefund(String id, Employee employee) {
        Order o = findById(id);
        o.setStatus(OrderStatus.REFUNDED);
        o.setEmployee(employee);
        return orderRepo.save(o);
    }

    @Override
    public Order updateStatus(String id, OrderStatus status, String reason, Employee employee) {
        Order o = findById(id);
        o.setStatus(status);
        o.setCancelReason(reason);
        o.setEmployee(employee);
        return orderRepo.save(o);
    }

    /* ===================== TOTAL ===================== */

    @Override
    public BigDecimal calculateTotal(String orderId) {
        return findById(orderId).getTotal();
    }

    /* ===================== GUEST LINK ===================== */

    @Override
    public void linkGuestOrders(String phone, Customer customer) {
        if (phone == null || phone.isBlank() || customer == null) return;
        orderRepo.linkGuestOrdersToCustomer(customer, normalizePhone(phone));
    }

    /* ===================== HELPER ===================== */
    private String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }
}
