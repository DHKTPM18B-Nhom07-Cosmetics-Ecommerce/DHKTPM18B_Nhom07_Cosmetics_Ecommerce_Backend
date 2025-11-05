package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderDetailService;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    private final OrderService orderService;
    public OrderDetailController(OrderDetailService orderDetailService, OrderService orderService) {
        this.orderDetailService = orderDetailService;
        this.orderService = orderService;
    }

    //CRUD
    @PostMapping
    public ResponseEntity<OrderDetail> createDetail(@RequestBody OrderDetail detail) {
        try {
            OrderDetail newDetail = orderDetailService.createOrderDetail(detail);
            return new ResponseEntity<>(newDetail, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    //lay chi tiet don hang theo id cua orderDetail
    @PutMapping("/{id}")
    public ResponseEntity<OrderDetail> updateDetail(@PathVariable Long id, @RequestBody OrderDetail details) {
        try {
            OrderDetail updatedDetail = orderDetailService.updateOrderDetail(id, details);
            return ResponseEntity.ok(updatedDetail);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    /** GET /api/order-details/order/{orderId} : Lấy tất cả chi tiết của một Order */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetail>> getDetailsByOrderId(@PathVariable Long orderId) {
        try {
            Order order = orderService.findById(orderId);
            List<OrderDetail> details = orderDetailService.findByOrder(order);
            return ResponseEntity.ok(details);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
