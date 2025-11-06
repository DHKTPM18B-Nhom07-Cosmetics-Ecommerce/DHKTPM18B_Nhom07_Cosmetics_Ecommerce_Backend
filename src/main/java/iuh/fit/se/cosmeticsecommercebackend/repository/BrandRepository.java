package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    //tim theo ten (khong phan biet hoa thuong)
    List<Brand> findByNameContainingIgnoreCase(String name);
    
    //tim theo trang thai active
    List<Brand> findByIsActive(boolean isActive);
    
    //tim theo ten va trang thai
    List<Brand> findByNameContainingIgnoreCaseAndIsActive(String name, boolean isActive);
}