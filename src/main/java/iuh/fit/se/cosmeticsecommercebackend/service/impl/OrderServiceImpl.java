package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.*;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.CreateOrderResponse;
import iuh.fit.se.cosmeticsecommercebackend.payload.OrderDetailRequest;
import iuh.fit.se.cosmeticsecommercebackend.repository.AddressRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.VoucherRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.*;
import iuh.fit.se.cosmeticsecommercebackend.service.voucher.DiscountResult;
import iuh.fit.se.cosmeticsecommercebackend.service.voucher.VoucherEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final CustomerService customerService;
    private final AddressService addressService;
    private final ProductVariantService productVariantService;
    private final AddressRepository addressRepository;
    private final OrderDetailService orderDetailService;

    @Autowired private VoucherEngine voucherEngine;
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private RiskService riskService;
    @Autowired private VoucherRedemptionService voucherRedemptionService;


    public OrderServiceImpl(
            OrderRepository orderRepo,
            CustomerService customerService,
            AddressService addressService,
            ProductVariantService productVariantService,
            AddressRepository addressRepository,
            OrderDetailService orderDetailService
    ) {
        this.orderRepo = orderRepo;
        this.customerService = customerService;
        this.addressService = addressService;
        this.productVariantService = productVariantService;
        this.addressRepository = addressRepository;
        this.orderDetailService = orderDetailService;
    }

    /* ===================== ORDER ID ===================== */

    private String generateNewOrderId() {
        String today = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "OD-" + today;

        return orderRepo.findLastOrderIdByDatePrefix(prefix)
                .map(id -> {
                    int seq = Integer.parseInt(id.substring(id.length() - 2)) + 1;
                    return prefix + String.format("%02d", seq);
                })
                .orElse(prefix + "01");
    }

    /* ===================== CREATE ===================== */

    @Override
    @Transactional
    public CreateOrderResponse createOrderFromRequest(CreateOrderRequest request) {

        Customer customer = null;
        if (request.getCustomerId() != null && request.getCustomerId() > 0) {
            customer = customerService.findById(request.getCustomerId());
        }

        /* ---------- ADDRESS ---------- */
        Address address;
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
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Chưa có địa chỉ mặc định"));
        }

        if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng rỗng");
        }

        /* ---------- ORDER BASE (CHƯA ÁP VOUCHER) ---------- */
        Order order = new Order();
        order.setId(generateNewOrderId());
        order.setCustomer(customer);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal shippingFee =
                request.getShippingFee() != null
                        ? request.getShippingFee()
                        : BigDecimal.valueOf(30000);

        order.setShippingFee(shippingFee);

        if (customer == null) {
            String phone = normalizePhone(request.getShippingPhone());

            if (phone == null || phone.length() < 9 || phone.length() > 12) {
                throw new IllegalArgumentException("SĐT guest không hợp lệ");
            }

            order.setGuestPhone(phone);
        }


        order.setSubtotal(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);

        /* ---------- ORDER DETAILS ---------- */
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();

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
            od.setDiscountAmount(BigDecimal.ZERO);
            od.recalc();

            subtotal = subtotal.add(od.getTotalPrice());

            // trừ tồn
            pv.setQuantity(pv.getQuantity() - dr.getQuantity());

            details.add(od);
        }

        order.setOrderDetails(details);
        order.setSubtotal(subtotal);

        // Lưu lần 1 – để có ID cho OrderDetail
        Order saved = orderRepo.save(order);

        /* ---------- LẤY VOUCHER TỪ REQUEST ---------- */
        List<Voucher> vouchers = new ArrayList<>();
        if (request.getVoucherCodes() != null) {
            request.getVoucherCodes().forEach(code ->
                    voucherRepository.findByCodeIgnoreCase(code)
                            .ifPresent(vouchers::add)
            );
        }

        // Nếu không có voucher nào => không cần gọi engine
        if (vouchers.isEmpty()) {
            // chỉ cần tính total = subtotal + shipping
            BigDecimal totalNoVoucher = subtotal.add(shippingFee);
            saved.setTotal(totalNoVoucher);
            saved.setDiscountAmount(BigDecimal.ZERO);
            saved.setShippingFee(shippingFee);
            saved = orderRepo.save(saved);


            CreateOrderResponse res = new CreateOrderResponse();
            res.setId(saved.getId());
            res.setStatus(saved.getStatus().name());
            res.setTotalAmount(saved.getTotal());
            return res;
        }

        /* ---------- ÁP VOUCHER THẬT SỰ ---------- */
        DiscountResult discountResult = voucherEngine.apply(saved, vouchers);

        BigDecimal orderDiscount = discountResult.getOrderDiscount();
        BigDecimal shippingDiscount = discountResult.getShippingDiscount();

        // PHÂN BỔ DISCOUNT ITEM (nếu engine trả về map theo orderDetailId)
        Map<Long, BigDecimal> itemDiscounts = discountResult.getItemDiscounts();
        BigDecimal itemDiscountTotal = BigDecimal.ZERO;

        for (OrderDetail od : saved.getOrderDetails()) {
            BigDecimal d = itemDiscounts.getOrDefault(od.getId(), BigDecimal.ZERO);
            od.setDiscountAmount(d);
            od.recalc();
            itemDiscountTotal = itemDiscountTotal.add(d);
        }

        // Phí ship cuối cùng
        BigDecimal finalShippingFee = shippingFee.subtract(shippingDiscount);
        if (finalShippingFee.compareTo(BigDecimal.ZERO) < 0) {
            finalShippingFee = BigDecimal.ZERO;
        }

        // Tổng tiền cuối
        BigDecimal total =
                saved.getSubtotal()
                        .subtract(orderDiscount)
                        .subtract(itemDiscountTotal)
                        .add(finalShippingFee);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        saved.setDiscountAmount(orderDiscount.add(itemDiscountTotal));
        saved.setShippingFee(finalShippingFee);
        saved.setTotal(total);

        // Lưu lần 2 – sau khi đã áp voucher
        saved = orderRepo.save(saved);


        /* ================= SAVE VOUCHER REDEMPTION ================= */
        for (Voucher v : vouchers) {

            BigDecimal totalDiscountForThisVoucher =
                    discountResult.getOrderDiscount()
                            .add(discountResult.getShippingDiscount());

            if (totalDiscountForThisVoucher.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // không giảm thì không lưu
            }

            VoucherRedemption vr = new VoucherRedemption();
            vr.setVoucher(v);
            vr.setOrder(saved);
            vr.setCustomer(customer); // null nếu guest
            vr.setAmountDiscounted(totalDiscountForThisVoucher);

            voucherRedemptionService.create(vr);
        }



        /* ---------- RESPONSE ---------- */
        CreateOrderResponse res = new CreateOrderResponse();
        res.setId(saved.getId());
        res.setStatus(saved.getStatus().name());
        res.setTotalAmount(saved.getTotal());

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
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy đơn hàng"));
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
            throw new ResourceNotFoundException("Không có quyền truy cập đơn hàng");
        }
        return o;
    }

    /* ===================== SEARCH ===================== */

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

    @Override
    public List<Order> findByStatusAndOrderDateBetween(
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return orderRepo.findByStatusAndOrderDateBetween(status, start, end);
    }

    /* ===================== STATUS ===================== */

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
    public Order cancelByCustomer(String orderId, String reason, Customer customer) {
        Order o = findById(orderId);

        if (o.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ hủy được đơn PENDING");
        }

        orderDetailService.restoreStockForOrder(orderId);

        o.setStatus(OrderStatus.CANCELLED);
        o.setCancelReason(reason);
        o.setCanceledAt(LocalDateTime.now());

        if (customer != null && customer.getAccount() != null) {
            riskService.checkAndAlertOrderSpam(
                    customer.getAccount().getId(),
                    customer.getAccount().getUsername()
            );
        }

        return orderRepo.save(o);
    }

    @Override
    public Order requestReturn(String id, String reason, Employee employee) {
        Order o = findById(id);
        o.setStatus(OrderStatus.RETURNED);
        o.setCancelReason(reason);
        o.setEmployee(employee);
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
