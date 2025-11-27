package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
//import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderDetailService;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductVariantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    private final OrderService orderService;
    private final ProductVariantService productVariantService;

    public OrderDetailController(OrderDetailService orderDetailService, OrderService orderService, ProductVariantService productVariantService) {
        this.orderDetailService = orderDetailService;
        this.orderService = orderService;
        this.productVariantService = productVariantService;
    }

    // --- CRUD ---

    /** POST /api/order-details : Tạo chi tiết đơn hàng mới */
    @PostMapping
    public ResponseEntity<OrderDetail> createDetail(@RequestBody OrderDetail detail) {
        OrderDetail newDetail = orderDetailService.createOrderDetail(detail);
        return new ResponseEntity<>(newDetail, HttpStatus.CREATED);
    }

    /** GET /api/order-details/{id} : Lấy thông tin chi tiết đơn hàng theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetail> getDetailById(@PathVariable Long id) {
        OrderDetail detail = orderDetailService.findById(id);
        return ResponseEntity.ok(detail);
    }

    /** GET /api/order-details : Lấy tất cả chi tiết đơn hàng */
    @GetMapping
    public List<OrderDetail> getAllDetails() {
        return orderDetailService.findAllDetails();
    }


    /** PUT /api/order-details/{id} : Cập nhật chi tiết đơn hàng (Chỉ cho phép PENDING) */
    @PutMapping("/{id}")
    public ResponseEntity<OrderDetail> updateDetail(@PathVariable Long id, @RequestBody OrderDetail details) {
        // GlobalException sẽ xử lý:
        // - ResourceNotFoundException (404)
        // - IllegalStateException (400) khi Order không phải PENDING.
        // - IllegalArgumentException (400) nếu quantity < 0.
        OrderDetail updatedDetail = orderDetailService.updateOrderDetail(id, details);
        return ResponseEntity.ok(updatedDetail);
    }
}