package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

        List<Product> getAll();

        List<Product> findByIsActive(boolean isActive);

        Product getById(Long id);

        Product create(Product product);

        Product update(Long id, Product product);

        void delete(Long id);

        Page<Product> filterProducts(
                String search,
                String categories,
                String brands,
                Long minPrice,
                Long maxPrice,
                Double rating,
                String stocks,
                Boolean active,
                Pageable pageable,
                String sort
        );
    }
