package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductVariantRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.ProductVariantService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public ProductVariantServiceImpl(ProductVariantRepository variantRepository, ProductRepository productRepository) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductVariant> getAll() {
        return variantRepository.findAll();
    }

    @Override
    public ProductVariant getById(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy biến thể có ID = " + id));
    }

    @Override
    public List<ProductVariant> getByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Không tìm thấy sản phẩm có ID = " + productId);
        }
        return variantRepository.findByProductId(productId);
    }

    @Override
    public ProductVariant create(ProductVariant variant) {
        // Kiểm tra product tồn tại
        if (variant.getProduct() == null || variant.getProduct().getId() == null) {
            throw new IllegalArgumentException("Product của variant không được null.");
        }

        Product product = productRepository.findById(variant.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm có ID = " + variant.getProduct().getId()));

        variant.setProduct(product);
        return variantRepository.save(variant);
    }

    @Override
    public ProductVariant update(Long id, ProductVariant variantUpdate) {
        ProductVariant existing = variantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy biến thể có ID = " + id));

        existing.setVariantName(variantUpdate.getVariantName());
        existing.setPrice(variantUpdate.getPrice());
        existing.setQuantity(variantUpdate.getQuantity());
        existing.setImageUrl(variantUpdate.getImageUrl());

        // Nếu có thay đổi product
        if (variantUpdate.getProduct() != null && variantUpdate.getProduct().getId() != null) {
            Product newProduct = productRepository.findById(variantUpdate.getProduct().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm có ID = " + variantUpdate.getProduct().getId()));
            existing.setProduct(newProduct);
        }

        return variantRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!variantRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy biến thể có ID = " + id);
        }
        variantRepository.deleteById(id);
    }
}
