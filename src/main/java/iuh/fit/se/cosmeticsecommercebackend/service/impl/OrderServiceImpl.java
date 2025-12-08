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

    @Autowired
    private RiskService riskService;

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

        Optional<String> lastIdOptional = orderRepo.findLastOrderIdByDatePrefix(prefix);
        int sequence = 1;

        if (lastIdOptional.isPresent()) {
            String lastId = lastIdOptional.get();
            String seqStr = lastId.substring(lastId.length() - 2);
            sequence = Integer.parseInt(seqStr) + 1;
        }
        return prefix + String.format("%02d", sequence);
    }

    /* ===================== FORCE LOAD ===================== */

    private void forceLoadOrderListDetails(List<Order> orders) {
        for (Order order : orders) {
            if (order.getCustomer() != null && order.getCustomer().getAccount() != null) {
                order.getCustomer().getAccount().getFullName();
            }
            if (order.getOrderDetails() != null) {
                order.getOrderDetails().forEach(d -> {
                    if (d.getProductVariant() != null) {
                        d.getProductVariant().getId();
                        if (d.getProductVariant().getProduct() != null) {
                            d.getProductVariant().getProduct().getName();
                        }
                        if (d.getProductVariant().getImageUrls() != null) {
                            d.getProductVariant().getImageUrls().size();
                        }
                    }
                });
            }
            if (order.getAddress() != null) {
                order.getAddress().getAddress();
            }
            if (order.getEmployee() != null) {
                order.getEmployee().getId();
            }
        }
    }

    /* ===================== CREATE ORDER ===================== */

    @Override
    public CreateOrderResponse createOrderFromRequest(CreateOrderRequest request) {

        Customer customer = null;
        Address address;

        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            customer = customerService.findById(request.getCustomerId());
        }

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

        // lưu phone guest
        if (customer == null) {
            order.setGuestPhone(request.getShippingPhone());
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

            BigDecimal total = pv.getPrice().multiply(BigDecimal.valueOf(dr.getQuantity()));
            od.setTotalPrice(total);

            subtotal = subtotal.add(total);
            pv.setQuantity(pv.getQuantity() - dr.getQuantity());
            details.add(od);
        }

        order.setOrderDetails(details);
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        order.setTotal(subtotal.add(order.getShippingFee()).subtract(discount));

        Order saved = orderRepo.save(order);

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

    /* ===================== BASIC QUERY ===================== */

    @Override
    public List<Order> getAll() {
        List<Order> orders = orderRepo.findAll();
        forceLoadOrderListDetails(orders);
        return orders;
    }

    @Override
    public Order findById(String id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
    }

    @Override
    public List<Order> getMyOrders(String username) {
        Customer c = customerService.findByAccountUsername(username);
        List<Order> orders = orderRepo.findByCustomer(c);
        forceLoadOrderListDetails(orders);
        return orders;
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

    /* ===================== GUEST LINK ===================== */

    @Override
    @Transactional
    public void linkGuestOrders(String phone, Customer customer) {
        if (phone == null || customer == null) return;
        orderRepo.linkGuestOrdersToCustomer(customer, phone);
    }

    /* ===================== STATUS ===================== */

    @Override
    public Order cancelByCustomer(String orderId, String reason, Customer customer) {
        Order o = findById(orderId);
        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(reason);
        o.setCanceledAt(LocalDateTime.now());
        return orderRepo.save(o);
    }

    @Override
    public Order cancelByEmployee(String id, String reason, Employee employee) {
        Order o = findById(id);
        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(reason);
        o.setEmployee(employee);
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
    public Order updateStatus(String id, OrderStatus status, String cancelReason, Employee employee) {
        Order o = findById(id);
        o.setStatus(status);
        o.setEmployee(employee);
        return orderRepo.save(o);
    }

    /* ===================== TOTAL ===================== */

    @Override
    public BigDecimal calculateTotal(String orderId) {
        Order o = findById(orderId);
        return o.getTotal();
    }

    /* ===================== FILTER ===================== */

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
    public List<Order> findByOrderDateBetween(LocalDateTime s, LocalDateTime e) {
        return orderRepo.findByOrderDateBetween(s, e);
    }

    @Override
    public List<Order> findByStatusAndCustomer(OrderStatus s, Customer c) {
        return orderRepo.findByStatusAndCustomer(s, c);
    }

    @Override
    public List<Order> findByTotalBetween(BigDecimal min, BigDecimal max) {
        return orderRepo.findByTotalBetween(min, max);
    }

    @Override
    public List<Order> findByStatusAndOrderDateBetween(OrderStatus s, LocalDateTime st, LocalDateTime ed) {
        return orderRepo.findByStatusAndOrderDateBetween(s, st, ed);
    }
}
