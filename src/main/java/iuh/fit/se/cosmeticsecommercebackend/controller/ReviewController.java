package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.Review;
import iuh.fit.se.cosmeticsecommercebackend.payload.ReviewRequest;
import iuh.fit.se.cosmeticsecommercebackend.repository.CustomerRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.ReviewService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public ReviewController(ReviewService reviewService, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.reviewService = reviewService;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // --- CRUD CO BAN ---
    
    /** POST /api/reviews : Tao danh gia moi */
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody ReviewRequest request) {
        try {
            // Lay Customer va Product tu database
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new NoSuchElementException("Customer not found"));
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Product not found"));
            
            // Tao Review entity
            Review review = new Review();
            review.setCustomer(customer);
            review.setProduct(product);
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setActive(request.isActive());
            
            Review newReview = reviewService.createReview(review);
            return new ResponseEntity<>(newReview, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /** GET /api/reviews/{id} : Lay thong tin danh gia theo ID */
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        try {
            Review review = reviewService.findById(id);
            return ResponseEntity.ok(review);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** GET /api/reviews : Lay tat ca danh gia */
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAll();
    }

    /** PUT /api/reviews/{id} : Cap nhat noi dung danh gia */
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review reviewDetails) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDetails);
            return ResponseEntity.ok(updatedReview);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** DELETE /api/reviews/{id} : Xoa mem danh gia */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // --- TIM KIEM ---
    
    /** GET /api/reviews/customer/{customerId} : Lay danh gia theo khach hang */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getReviewsByCustomer(@PathVariable Long customerId) {
        Customer customer = new Customer();
        customer.setId(customerId);
        
        List<Review> reviews = reviewService.findByCustomer(customer);
        
        // Trả về danh sách Map đơn giản để tránh circular reference
        List<Map<String, Object>> simplifiedReviews = reviews.stream()
            .map(review -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", review.getId());
                map.put("rating", review.getRating());
                map.put("comment", review.getComment());
                map.put("reviewDate", review.getReviewDate());
                map.put("active", review.isActive());
                
                // Chỉ lấy productId, không lấy toàn bộ object
                if (review.getProduct() != null) {
                    map.put("productId", review.getProduct().getId());
                }
                
                return map;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(simplifiedReviews);
    }

    /** GET /api/reviews/product/{productId} : Lay danh gia theo san pham */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Product product) {
        List<Review> reviews = reviewService.findByProduct(product);
        return ResponseEntity.ok(reviews);
    }

    /** GET /api/reviews/rating/{rating} : Loc theo so sao */
    @GetMapping("/rating/{rating}")
    public List<Review> getReviewsByRating(@PathVariable int rating) {
        return reviewService.findByRating(rating);
    }

    /** 
     * GET /api/reviews/date-range?start=...&end=... : Tim kiem trong khoang thoi gian
     * Format ngay: ISO (2025-11-03T10:15:30)
     */
    @GetMapping("/date-range")
    public List<Review> getReviewsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return reviewService.findByDateRange(start, end);
    }

    // --- AN/HIEN REVIEW ---
    
    /** POST /api/reviews/{id}/hide : An danh gia */
    @PostMapping("/{id}/hide")
    public ResponseEntity<Review> hideReview(@PathVariable Long id) {
        try {
            Review review = reviewService.hideReview(id);
            return ResponseEntity.ok(review);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** POST /api/reviews/{id}/unhide : Hien thi lai danh gia */
    @PostMapping("/{id}/unhide")
    public ResponseEntity<Review> unhideReview(@PathVariable Long id) {
        try {
            Review review = reviewService.unhideReview(id);
            return ResponseEntity.ok(review);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** GET /api/reviews/product/{productId}/average : Tinh sao trung binh cua san pham */
    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Double> getProductAverageRating(@PathVariable Product product) {
        double avgRating = reviewService.calculateAverageRating(product);
        return ResponseEntity.ok(avgRating);
    }
}