package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("""
SELECT p FROM Product p
JOIN p.variants v
WHERE v.id = :variantId
""")
    Optional<Product> findByVariantId(@Param("variantId") Long variantId);

    // Tim theo trang thai active 
    List<Product> findByIsActive(boolean isActive);

}
