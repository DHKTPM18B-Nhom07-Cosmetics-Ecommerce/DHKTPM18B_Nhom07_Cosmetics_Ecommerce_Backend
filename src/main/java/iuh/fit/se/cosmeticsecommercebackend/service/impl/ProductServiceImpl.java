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

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID = " + id));
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Long id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm có ID = " + id));

        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setImages(updatedProduct.getImages());
        existing.setCategory(updatedProduct.getCategory());
        existing.setBrand(updatedProduct.getBrand());
        existing.setActive(updatedProduct.isActive());
        existing.setAverageRating(updatedProduct.getAverageRating());

        if (updatedProduct.getVariants() != null) {
            existing.getVariants().clear();
            existing.getVariants().addAll(updatedProduct.getVariants());
            existing.getVariants().forEach(v -> v.setProduct(existing));
        }

        return productRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm có ID = " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public Page<Product> filterProducts(String search, Long categoryId, Long brandId,
                                        Long minPrice, Long maxPrice, Double rating, Pageable pageable) {

        Specification<Product> spec = Specification.allOf(
                ProductSpecification.nameContains(search),
                ProductSpecification.hasCategory(categoryId),
                ProductSpecification.hasBrand(brandId),
                ProductSpecification.priceBetween(minPrice, maxPrice),
                ProductSpecification.ratingAtLeast(rating)
        );

        return productRepository.findAll(spec, pageable);
    }
}
