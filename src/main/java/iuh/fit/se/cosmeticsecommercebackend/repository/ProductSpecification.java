package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> nameContains(String search) {
        return (root, query, cb) ->
                (search == null || search.trim().isEmpty())
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                (categoryId == null)
                        ? cb.conjunction()
                        : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasBrand(Long brandId) {
        return (root, query, cb) ->
                (brandId == null)
                        ? cb.conjunction()
                        : cb.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<Product> priceBetween(Long minPrice, Long maxPrice) {
        return (root, query, cb) -> {

            if (minPrice == null && maxPrice == null)
                return cb.conjunction();

            var variantJoin = root.join("variants");

            if (minPrice != null && maxPrice != null)
                return cb.between(variantJoin.get("price"), minPrice, maxPrice);

            if (minPrice != null)
                return cb.greaterThanOrEqualTo(variantJoin.get("price"), minPrice);

            return cb.lessThanOrEqualTo(variantJoin.get("price"), maxPrice);
        };
    }

    public static Specification<Product> ratingAtLeast(Double rating) {
        return (root, query, cb) ->
                (rating == null)
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(root.get("averageRating"), rating);
    }
}
