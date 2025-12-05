package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderDetailRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderDetailService;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderService;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductVariantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository repo;
    private final OrderService orderService;
    private final ProductVariantService productVariantService;

    public OrderDetailServiceImpl(OrderDetailRepository repo,
                                  OrderService orderService,
                                  ProductVariantService productVariantService) {
        this.repo = repo;
        this.orderService = orderService;
        this.productVariantService = productVariantService;
    }

    //CRUD

    @Override
    public OrderDetail createOrderDetail(OrderDetail orderDetail) {
        Order order = orderDetail.getOrder();

        // Kiểm tra trạng thái Order
        if (order != null && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Không thể thêm chi tiết khi Order ở trạng thái " + order.getStatus());
        }

        // Tính toán totalPrice và thiết lập
        if (orderDetail.getUnitPrice() != null && orderDetail.getQuantity() != null && orderDetail.getQuantity() > 0) {
            orderDetail.setTotalPrice(
                    orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
            );
        } else {
            orderDetail.setTotalPrice(BigDecimal.ZERO);
        }

        // Lưu chi tiết đơn hàng
        OrderDetail savedDetail = repo.save(orderDetail);
        // Cập nhật lại tổng tiền cho Order chính (nếu Order đã tồn tại)
        if (order != null && order.getId() != null) {
            orderService.calculateTotal(order.getId());
        }

        return savedDetail;
    }

    @Override
    public OrderDetail findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết đơn hàng với ID " + id));
    }

    @Override
    public List<OrderDetail> findAllDetails() {
        return repo.findAll();
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetail orderDetail) {
        OrderDetail existingDetail = findById(id);
        Order order = existingDetail.getOrder();

        // Kiểm tra Order: Chỉ sửa khi ở trạng thái PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Không thể cập nhật chi tiết khi đơn hàng đã được xác nhận.");
        }

        // Kiểm tra và cập nhật thuộc tính
        if (orderDetail.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn hoặc bằng 0.");
        }

        existingDetail.setQuantity(orderDetail.getQuantity());
        existingDetail.setUnitPrice(orderDetail.getUnitPrice());

        // Tính toán totalPrice mới
        existingDetail.setTotalPrice(calculateDetailTotal(existingDetail.getQuantity(), existingDetail.getUnitPrice()));

        // Lưu chi tiết và cập nhật lại tổng tiền Order
        OrderDetail updatedDetail = repo.save(existingDetail);
        orderService.calculateTotal(order.getId());

        return updatedDetail;
    }
    //Nghiep vu
    @Override
    public BigDecimal calculateDetailTotal(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || unitPrice == null || quantity < 0 || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            // Ném lỗi tham số nếu không hợp lệ
            throw new IllegalArgumentException("Số lượng và đơn giá phải hợp lệ.");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    @Transactional
    public void restoreStockForOrder(String orderId) {
        // Lấy đơn hàng
        Order order = orderService.findById(orderId);

        // Lấy tất cả chi tiết đơn hàng
        List<OrderDetail> details = getDetailsByOrder(order);

        if (details.isEmpty()) return;

        // Lặp qua từng chi tiết và hoàn trả tồn kho
        for (OrderDetail detail : details) {
            Long variantId = detail.getProductVariant().getId();
            int quantityToRestore = detail.getQuantity();

            // GỌI SERVICE HOÀN KHO ĐÃ TRIỂN KHAI
            productVariantService.increaseStock(variantId, quantityToRestore);
        }
    }
    //tim kiem theo mqh
    @Override
    public List<OrderDetail> getDetailsByOrder(Order order) {
        return repo.findByOrder(order);
    }

    @Override
    public List<OrderDetail> findByProductVariant(ProductVariant variant) {
        return repo.findByProductVariant(variant);
    }

}