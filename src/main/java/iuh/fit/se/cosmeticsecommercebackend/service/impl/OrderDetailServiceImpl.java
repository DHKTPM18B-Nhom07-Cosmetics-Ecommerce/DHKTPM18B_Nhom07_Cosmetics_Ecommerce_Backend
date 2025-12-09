package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderDetailRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderDetailService;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductVariantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository repo;
    private final ProductVariantService productVariantService;

    public OrderDetailServiceImpl(
            OrderDetailRepository repo,
            ProductVariantService productVariantService
    ) {
        this.repo = repo;
        this.productVariantService = productVariantService;
    }

    /* ================= CRUD ================= */

    @Override
    public OrderDetail createOrderDetail(OrderDetail orderDetail) {
        Order order = orderDetail.getOrder();

        if (order != null && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Không thể thêm chi tiết khi Order ở trạng thái " + order.getStatus()
            );
        }

        // ✅ KHÔNG tự tính ở đây – entity tự lo
        orderDetail.recalc();
        return repo.save(orderDetail);
    }

    @Override
    public OrderDetail findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy chi tiết đơn hàng ID: " + id));
    }

    @Override
    public List<OrderDetail> findAllDetails() {
        return repo.findAll();
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetail data) {
        OrderDetail existing = findById(id);

        if (existing.getOrder().getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Không thể cập nhật khi đơn hàng đã xác nhận.");
        }

        existing.setQuantity(data.getQuantity());
        existing.setUnitPrice(data.getUnitPrice());
        existing.setDiscountAmount(
                data.getDiscountAmount() != null
                        ? data.getDiscountAmount()
                        : BigDecimal.ZERO
        );

        // ✅ ENTITY TỰ CALC
        existing.recalc();
        return repo.save(existing);
    }

    /* ================= BUSINESS ================= */

    @Override
    public BigDecimal calculateDetailTotal(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không hợp lệ");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public void restoreStockForOrder(String orderId) {
        List<OrderDetail> details = repo.findAll()
                .stream()
                .filter(d -> d.getOrder().getId().equals(orderId))
                .toList();

        for (OrderDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            productVariantService.increaseStock(
                    variant.getId(),
                    detail.getQuantity()
            );
        }
    }

    @Override
    public List<OrderDetail> getDetailsByOrder(Order order) {
        return repo.findByOrder(order);
    }

    @Override
    public List<OrderDetail> findByProductVariant(ProductVariant variant) {
        return repo.findByProductVariant(variant);
    }
}
