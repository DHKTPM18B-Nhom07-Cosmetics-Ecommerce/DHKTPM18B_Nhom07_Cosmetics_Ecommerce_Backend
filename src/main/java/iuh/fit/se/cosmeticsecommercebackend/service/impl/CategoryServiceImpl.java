package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Category;
import iuh.fit.se.cosmeticsecommercebackend.repository.CategoryRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục có ID = " + id));
    }

    @Override
    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, Category categoryUpdate) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục có ID = " + id));

        existing.setName(categoryUpdate.getName());
        return categoryRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy danh mục có ID = " + id);
        }
        categoryRepository.deleteById(id);
    }
}
