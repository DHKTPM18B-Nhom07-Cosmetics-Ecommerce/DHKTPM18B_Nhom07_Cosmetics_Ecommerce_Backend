package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductSpecification;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductService;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;

    public ProductServiceImpl(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Product> getAll() {
        return repo.findAll();
    }

    @Override
    public List<Product> findByIsActive(boolean isActive) {
        return repo.findByIsActive(isActive);
    }

    @Override
    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID = " + id));
    }

    @Override
    public Product create(Product product) {
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> v.setProduct(product));
        }
        // Default active = true is handled in Entity or DTO, but safe to set here if needed
        // product.setActive(true); 
        return repo.save(product);
    }

    @Override
    public Product update(Long id, Product updated) {
        Product existing = getById(id);

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setImages(updated.getImages());
        existing.setBrand(updated.getBrand());
        existing.setCategory(updated.getCategory());
        existing.setAverageRating(updated.getAverageRating());
        
        // BrandServiceImpl doesn't update isActive in generic update, but Product probably should or user might want separate method.
        // Keeping it here for now as removing it might break 'edit status' feature if built that way.
        existing.setActive(updated.isActive());

        if (updated.getVariants() != null) {
            existing.getVariants().clear();
            existing.getVariants().addAll(updated.getVariants());
            existing.getVariants().forEach(v -> v.setProduct(existing));
        }

        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        Product product = getById(id);
        // Soft delete implementation: Set active to false and save
        product.setActive(false);
        repo.save(product);
    }

    @Override
    public Page<Product> filterProducts(
            String search,
            String categories,
            String brands,
            Long minPrice,
            Long maxPrice,
            Double rating,
            String stocks,
            Boolean active,
            Pageable pageable
    ) {
        Specification<Product> spec = Specification.allOf(
                active != null ? ProductSpecification.isActive(active) : (root, query, cb) -> cb.conjunction(),
                ProductSpecification.nameContains(search),
                ProductSpecification.hasCategories(categories),
                ProductSpecification.hasBrands(brands),
                ProductSpecification.priceBetween(minPrice, maxPrice),
                ProductSpecification.ratingAtLeast(rating),
                ProductSpecification.stockStatuses(stocks)
        );

        return repo.findAll(spec, pageable);
    }
}
