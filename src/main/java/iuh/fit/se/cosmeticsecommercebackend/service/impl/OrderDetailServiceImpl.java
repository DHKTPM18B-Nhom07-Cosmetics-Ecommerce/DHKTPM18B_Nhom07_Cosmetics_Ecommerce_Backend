package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderDetailRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.OrderDetailService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {
    private final  OrderDetailRepository repo;
    public OrderDetailServiceImpl(OrderDetailRepository repo){
        this.repo = repo;
    }
    //CRUD:
    @Override
    public OrderDetail createOrderDetail(OrderDetail orderDetail) {
        Order order= orderDetail.getOrder();
        if(order!=null && order.getStatus()!= OrderStatus.PENDING){
            throw new IllegalStateException("Không thể thm chi tiết khi Order ở trạng thái "+order.getStatus());

        }
        return repo.save(orderDetail);
    }
    @Override
    public OrderDetail findById(Long id) {
        return repo.findById(id).orElseThrow(()-> new NoSuchElementException("Không tim thấy chi tiết đơn hàng với ID "+id));
    }
    @Override
    public List<OrderDetail> findAllDetails(){
        return repo.findAll();
    }
    @Override
    public OrderDetail updateOrderDetail(Long id,OrderDetail orderDetail) {
        OrderDetail existingDetail =findById(id);
        //kiem tra Order: Sua order khi o trang thai PENDING
        if(existingDetail.getOrder().getStatus()!= OrderStatus.PENDING){
            throw new IllegalStateException("Khong the cap nhat chi tiet khi don hang da duoc xac nhan");
        }
        existingDetail.setQuantity(orderDetail.getQuantity());
        existingDetail.setUnitPrice(orderDetail.getUnitPrice());
//        existingDetail.setTotalPrice(orderDetail.getTotalPrice());
//        //ghi đè tính toán totalPrice dựa trên quantity mới
        //tinh totalprice tren server
        BigDecimal quantityDecimal = new BigDecimal(existingDetail.getQuantity());
        existingDetail.setTotalPrice(existingDetail.getUnitPrice().multiply(quantityDecimal));
        return repo.save(existingDetail);
    }
    //TIM KIEM THEO MQH
    @Override
    public List<OrderDetail> findByOrder(Order order) {
        return repo.findByOrder(order);
    }
    @Override
    public List<OrderDetail> findByProductVariant(ProductVariant variant) {
        return repo.findByProductVariant(variant);
    }

}
