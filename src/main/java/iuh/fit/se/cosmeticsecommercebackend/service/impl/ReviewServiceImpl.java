package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.Review;
import iuh.fit.se.cosmeticsecommercebackend.repository.ReviewRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.ReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review createReview(Review review) {
        // Mac dinh active = true khi tao moi
        review.setActive(true);
        
        // Tu dong set thoi gian review
        if (review.getReviewDate() == null) {
            review.setReviewDate(LocalDateTime.now());
        }
        
        return reviewRepository.save(review);
    }

    @Override
    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đánh giá với ID: " + id));
    }

    @Override
    public List<Review> getAll() {
        return reviewRepository.findAll();
    }

    @Override
    public Review updateReview(Long id, Review reviewDetails) {
        Review existingReview = findById(id);
        
        // Cap nhat thong tin
        existingReview.setRating(reviewDetails.getRating());
        existingReview.setComment(reviewDetails.getComment());
        // Khong cap nhat customer, product, reviewDate va active
        
        return reviewRepository.save(existingReview);
    }

    @Override
    public void deleteReview(Long id) {
        Review review = findById(id);
        // Xoa mem bang cach set active = false
        review.setActive(false);
        reviewRepository.save(review);
    }

    @Override
    public List<Review> findByCustomer(Customer customer) {
        return reviewRepository.findByCustomer(customer);
    }

    @Override
    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    @Override
    public List<Review> findByRating(int rating) {
        return reviewRepository.findByRating(rating);
    }

    @Override
    public List<Review> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return reviewRepository.findByReviewDateBetween(start, end);
    }

    @Override
    public List<Review> findByProductAndActive(Product product, boolean active) {
        return reviewRepository.findByProductAndActive(product, active);
    }

    @Override
    public List<Review> findByCustomerAndActive(Customer customer, boolean active) {
        return reviewRepository.findByCustomerAndActive(customer, active);
    }

    @Override
    public Review hideReview(Long id) {
        Review review = findById(id);
        review.setActive(false);
        return reviewRepository.save(review);
    }

    @Override
    public Review unhideReview(Long id) {
        Review review = findById(id);
        review.setActive(true);
        return reviewRepository.save(review);
    }

    @Override
    public double calculateAverageRating(Product product) {
        List<Review> reviews = findByProductAndActive(product, true);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream()
                .mapToInt(Review::getRating)
                .sum();
        return sum / reviews.size();
    }
}