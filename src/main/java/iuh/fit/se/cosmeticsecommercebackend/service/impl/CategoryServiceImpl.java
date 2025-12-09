package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.exception.ResourceNotFoundException;
import iuh.fit.se.cosmeticsecommercebackend.model.Category;
import iuh.fit.se.cosmeticsecommercebackend.repository.CategoryRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.ProductRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.CategoryService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục có ID = " + id));
    }

    @Override
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, Category categoryUpdate) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục có ID = " + id));

        existing.setName(categoryUpdate.getName());
        existing.setImageUrl(categoryUpdate.getImageUrl());
        if (categoryUpdate.getParent() != null && categoryUpdate.getParent().getId() != null) {
            Category parentCat = categoryRepository.findById(categoryUpdate.getParent().getId()).orElse(null);
            existing.setParent(parentCat);
        } else {
            existing.setParent(null);
        }
        
        if (categoryUpdate.getIsActive() != null) {
            // Logic validation: If disabling (active -> inactive)
            if (!categoryUpdate.getIsActive() && (existing.getIsActive() == null || existing.getIsActive())) {
                checkProductsRecursively(existing);
            }
            existing.setIsActive(categoryUpdate.getIsActive());
        }
        
        return categoryRepository.save(existing);
    }

    private void checkProductsRecursively(Category category) {
        // Check products of current category
        long productCount = productRepository.countByCategoryId(category.getId());
        if (productCount > 0) {
            throw new IllegalStateException("Không thể vô hiệu hóa danh mục \"" + category.getName() + "\" vì vẫn còn sản phẩm.");
        }

        // Check children recursively
        // Note: Children are fetched lazily. Ensure transactional context access.
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                checkProductsRecursively(child);
            }
        }
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục có ID = " + id);
        }
        categoryRepository.deleteById(id);
    }
}
