package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Cart;
import iuh.fit.se.cosmeticsecommercebackend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Tìm giỏ hàng dựa trên ID khách hàng
    Optional<Cart> findByCustomer_Id(Long customerId);
}
