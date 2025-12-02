package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product p) {
        return ResponseEntity.ok(service.create(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product p) {
        return ResponseEntity.ok(service.update(id, p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // FILTER
    // ============================================
    @GetMapping("/filter")
    public ResponseEntity<?> filter(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String brands,
            @RequestParam(required = false) String stocks,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Double rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {

        Sort sortConfig = switch (sort) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "priceAsc" -> Sort.by("variants.price").ascending();
            case "priceDesc" -> Sort.by("variants.price").descending();
            case "all" -> Sort.unsorted();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sortConfig);

        var result = service.filterProducts(
                search, categories, brands, minPrice, maxPrice, rating, stocks, pageable
        );

        // mapping
        List<?> content = result.getContent().stream().map(product -> {
            HashMap<String, Object> map = new HashMap<>();

            long min = product.getVariants().stream()
                    .map(v -> v.getPrice().longValue())
                    .min(Long::compareTo)
                    .orElse(0L);

            long max = product.getVariants().stream()
                    .map(v -> v.getPrice().longValue())
                    .max(Long::compareTo)
                    .orElse(0L);

            int totalQty = product.getVariants().stream()
                    .mapToInt(v -> v.getQuantity() == null ? 0 : v.getQuantity())
                    .sum();

            boolean inStock = totalQty > 0;
            boolean lowStock = totalQty > 0 && totalQty <= 10;

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

            map.put("inStock", inStock);
            map.put("lowStock", lowStock);

            return map;
        }).toList();

        return ResponseEntity.ok(new HashMap<>() {{
            put("content", content);
            put("totalElements", result.getTotalElements());
            put("totalPages", result.getTotalPages());
            put("page", result.getNumber());
        }});
    }
}
