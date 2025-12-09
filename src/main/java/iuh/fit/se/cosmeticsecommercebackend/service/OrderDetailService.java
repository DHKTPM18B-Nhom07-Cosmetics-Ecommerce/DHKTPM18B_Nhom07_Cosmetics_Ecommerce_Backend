package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;

import java.math.BigDecimal;
import java.util.List;

public interface OrderDetailService {
    //CRUD:
    OrderDetail createOrderDetail(OrderDetail orderDetail);

    OrderDetail findById(Long id);

    List<OrderDetail> findAllDetails();

    OrderDetail updateOrderDetail( Long id, OrderDetail orderDetail);
    // Tính thành tiền cho một dòng sản phẩm (unitPrice * quantity)
    BigDecimal calculateDetailTotal(Integer quantity, BigDecimal unitPrice);

    // Hoàn trả tồn kho cho tất cả ProductVariant khi Order bị hủy
    void restoreStockForOrder(String orderId);

    //TIM KIEM THEO MQH
    List<OrderDetail> getDetailsByOrder(Order order);

    List<OrderDetail> findByProductVariant(ProductVariant variant);
}
