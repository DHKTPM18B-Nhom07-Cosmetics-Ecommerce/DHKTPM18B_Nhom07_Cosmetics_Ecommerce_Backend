package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    //tim theo khach hang
    List<Review> findByCustomer(Customer customer);
    
    //tim theo san pham
    List<Review> findByProduct(Product product);
    
    //tim theo so sao
    List<Review> findByRating(int rating);
    
    //tim theo khoang thoi gian
    List<Review> findByReviewDateBetween(LocalDateTime start, LocalDateTime end);
    
    //tim theo san pham va trang thai
    List<Review> findByProductAndActive(Product product, boolean active);
    
    //tim theo khach hang va trang thai
    List<Review> findByCustomerAndActive(Customer customer, boolean active);
}