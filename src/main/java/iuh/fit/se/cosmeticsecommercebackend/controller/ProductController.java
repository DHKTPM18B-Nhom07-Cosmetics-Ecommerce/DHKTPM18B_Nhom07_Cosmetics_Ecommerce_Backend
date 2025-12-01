package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ===============================
    // GET ALL
    // ===============================
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAll());
    }

    // ===============================
    // GET BY ID
    // ===============================
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ===============================
    // CREATE
    // ===============================
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ===============================
    // UPDATE
    // ===============================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product updated) {
        try {
            return ResponseEntity.ok(productService.update(id, updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ===============================
    // DELETE
    // ===============================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ===============================
    //  FILTER API
    // ===============================
    @GetMapping("/filter")
    public ResponseEntity<?> filterProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Long brand,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Double rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {

        // ===== SORT CONFIG =====
        Sort sortConfig = switch (sort) {
            case "priceAsc" -> Sort.by("variants.price").ascending();
            case "priceDesc" -> Sort.by("variants.price").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sortConfig);

        // ===== FETCH PRODUCTS =====
        Page<Product> result = productService.filterProducts(
                search, category, brand, minPrice, maxPrice, rating, pageable
        );

        List<HashMap<String, Object>> content = result.getContent().stream().map(product -> {
            HashMap<String, Object> map = new HashMap<>();

            Long min = product.getVariants().stream()
                    .map(v -> v.getPrice().longValue())
                    .min(Long::compareTo).orElse(0L);

            Long max = product.getVariants().stream()
                    .map(v -> v.getPrice().longValue())
                    .max(Long::compareTo).orElse(0L);

            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("description", product.getDescription());
            map.put("images", product.getImages());
            map.put("averageRating", product.getAverageRating());
            map.put("brandName", product.getBrand() != null ? product.getBrand().getName() : null);
            map.put("categoryName", product.getCategory() != null ? product.getCategory().getName() : null);
            map.put("variants", product.getVariants());
            map.put("minPrice", min);
            map.put("maxPrice", max);

            return map;
        }).toList();

        // ===== RETURN =====
        return ResponseEntity.ok(new HashMap<>() {{
            put("content", content);
            put("totalElements", result.getTotalElements());
            put("totalPages", result.getTotalPages());
            put("page", result.getNumber());
        }});
    }
}
