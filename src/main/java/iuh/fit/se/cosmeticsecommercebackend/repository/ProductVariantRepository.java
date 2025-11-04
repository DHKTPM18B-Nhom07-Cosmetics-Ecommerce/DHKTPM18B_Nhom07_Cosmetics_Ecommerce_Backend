package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
}
