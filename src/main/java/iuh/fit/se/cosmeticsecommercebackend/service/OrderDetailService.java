package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;

import java.util.List;

public interface OrderDetailService {
    //CRUD:
    OrderDetail createOrderDetail(OrderDetail orderDetail);

    OrderDetail findById(Long id);

    List<OrderDetail> findAllDetails();

    OrderDetail updateOrderDetail( Long id, OrderDetail orderDetail);

    //TIM KIEM THEO MQH
    List<OrderDetail> findByOrder(Order order);

    List<OrderDetail> findByProductVariant(ProductVariant variant);
}
