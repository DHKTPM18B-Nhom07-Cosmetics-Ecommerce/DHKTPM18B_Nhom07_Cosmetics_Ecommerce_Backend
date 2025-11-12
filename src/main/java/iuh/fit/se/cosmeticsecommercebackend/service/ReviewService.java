package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.Review;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewService {
    //CRUD co ban
    Review createReview(Review review);
    Review findById(Long id);
    List<Review> getAll();
    Review updateReview(Long id, Review reviewDetails);
    void deleteReview(Long id);  //chi xoa mem (set active = false)
    
    //Tim kiem
    List<Review> findByCustomer(Customer customer);
    List<Review> findByProduct(Product product);
    List<Review> findByRating(int rating);
    List<Review> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<Review> findByProductAndActive(Product product, boolean active);
    List<Review> findByCustomerAndActive(Customer customer, boolean active);
    
    //Nghiep vu khac
    Review hideReview(Long id);      //an review
    Review unhideReview(Long id);    //hien thi lai review
    double calculateAverageRating(Product product); //tinh sao trung binh
}