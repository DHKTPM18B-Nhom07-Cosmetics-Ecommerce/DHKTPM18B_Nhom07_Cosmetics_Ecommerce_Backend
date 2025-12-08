package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.dto.request.ProductRequest;
import iuh.fit.se.cosmeticsecommercebackend.model.Brand;
import iuh.fit.se.cosmeticsecommercebackend.model.Category;
import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        try {
            // Manual Mapping DTO -> Entity
            Product p = new Product();
            p.setName(request.getName());
            p.setDescription(request.getDescription());
            p.setImages(request.getImages());
            if (request.getIsActive() != null) {
                p.setActive(request.getIsActive());
            }
            
            if (request.getCategoryId() != null) {
                Category c = new Category();
                c.setId(request.getCategoryId());
                p.setCategory(c);
            }
            
            if (request.getBrandId() != null) {
                Brand b = new Brand();
                b.setId(request.getBrandId());
                p.setBrand(b);
            }
            
            if (request.getVariants() != null) {
                List<ProductVariant> variantEntities = request.getVariants().stream().map(vReq -> {
                    ProductVariant v = new ProductVariant();
                    v.setVariantName(vReq.getVariantName());
                    v.setPrice(vReq.getPrice());
                    v.setQuantity(vReq.getQuantity());
                    v.setSold(vReq.getSold() != null ? vReq.getSold() : 0);
                    v.setImageUrls(vReq.getImageUrls());
                    v.setProduct(p); // Set relationship immediately
                    return v;
                }).collect(Collectors.toList());
                p.setVariants(variantEntities);
            }

            return ResponseEntity.ok(service.create(p));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            // Manual Mapping DTO -> Entity
            Product p = new Product();
            p.setName(request.getName());
            p.setDescription(request.getDescription());
            p.setImages(request.getImages());
            if (request.getIsActive() != null) {
                p.setActive(request.getIsActive());
            }
            
            if (request.getCategoryId() != null) {
                Category c = new Category();
                c.setId(request.getCategoryId());
                p.setCategory(c);
            }
            
            if (request.getBrandId() != null) {
                Brand b = new Brand();
                b.setId(request.getBrandId());
                p.setBrand(b);
            }
            
            if (request.getVariants() != null) {
                List<ProductVariant> variantEntities = request.getVariants().stream().map(vReq -> {
                    ProductVariant v = new ProductVariant();
                    v.setVariantName(vReq.getVariantName());
                    v.setPrice(vReq.getPrice());
                    v.setQuantity(vReq.getQuantity());
                    v.setSold(vReq.getSold() != null ? vReq.getSold() : 0);
                    v.setImageUrls(vReq.getImageUrls());
                    v.setProduct(p); // Set relationship immediately
                    return v;
                }).collect(Collectors.toList());
                p.setVariants(variantEntities);
            }

            return ResponseEntity.ok(service.update(id, p));
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating product: " + e.getMessage());
                }
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
                    @RequestParam(required = false) Boolean active,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "12") int size,
                    @RequestParam(defaultValue = "newest") String sort
            ) {

                Sort sortConfig = switch (sort) {
                    case "oldest" -> Sort.by("createdAt").ascending();
                    case "priceAsc" -> Sort.by("variants.price").ascending();
                    case "priceDesc" -> Sort.by("variants.price").descending();
                    case "az" -> Sort.by("name").ascending();
                    case "za" -> Sort.by("name").descending();
                    case "bestSelling" -> Sort.unsorted();
                    case "all" -> Sort.unsorted();
                    default -> Sort.by("createdAt").descending();
                };

                Pageable pageable = PageRequest.of(page, size, sortConfig);

                var result = service.filterProducts(
                        search, categories, brands, minPrice, maxPrice, rating, stocks, active, pageable, sort
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
                    
                    // Add IDs for Edit Modal
                    map.put("brandId", product.getBrand() != null ? product.getBrand().getId() : null);
                    map.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
                    
                    map.put("createdAt", product.getCreatedAt());

                    map.put("variants", product.getVariants());
                    map.put("minPrice", min);
                    map.put("maxPrice", max);

                    map.put("inStock", inStock);
                    map.put("lowStock", lowStock);
                    map.put("isActive", product.isActive());
                    map.put("quantity", totalQty); // Ensure quantity is also sum of variants if needed for table
                    map.put("totalSold", product.getTotalSold());

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

