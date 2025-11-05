package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    //tim chi tiet don hang thuoc 1 order
    List<OrderDetail> findByOrder(Order order);
    //tim chi tiet don hang dua vao bien the sp: dung khi bao cao doanh so/so luong ban ra cua 1 bien the
    List<OrderDetail> findByProductVariant(ProductVariant productVariant);

}
