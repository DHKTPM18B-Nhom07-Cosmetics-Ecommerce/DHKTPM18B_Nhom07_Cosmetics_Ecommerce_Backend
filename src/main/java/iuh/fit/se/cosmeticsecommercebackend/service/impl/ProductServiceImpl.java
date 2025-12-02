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
    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID = " + id));
    }

    @Override
    public Product create(Product product) {
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
        if (!repo.existsById(id))
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm ID = " + id);
        repo.deleteById(id);
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
            Pageable pageable
    ) {
        Specification<Product> spec = Specification.allOf(
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
