package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Product;
import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSpecification {

    public static Specification<Product> nameContains(String search) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (search == null || search.isEmpty()) return cb.conjunction();

            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategories(String csv) {
        if (csv == null || csv.isEmpty()) return null;

        List<Long> ids = Arrays.stream(csv.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return (root, query, cb) ->
                root.get("category").get("id").in(ids);
    }

    public static Specification<Product> hasBrands(String csv) {
        if (csv == null || csv.isEmpty()) return null;

        List<Long> ids = Arrays.stream(csv.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return (root, query, cb) ->
                root.get("brand").get("id").in(ids);
    }

    public static Specification<Product> priceBetween(Long min, Long max) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (min == null && max == null) return cb.conjunction();

            Join<Object, Object> v = root.join("variants", JoinType.LEFT);

            if (min != null && max != null)
                return cb.between(v.get("price"), min, max);

            if (min != null)
                return cb.greaterThanOrEqualTo(v.get("price"), min);

            return cb.lessThanOrEqualTo(v.get("price"), max);
        };
    }

    public static Specification<Product> ratingAtLeast(Double rating) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (rating == null) return cb.conjunction();

            return cb.greaterThanOrEqualTo(root.get("averageRating"), rating);
        };
    }

    //STOCK FILTER
    public static Specification<Product> stockStatuses(String csv) {
        if (csv == null || csv.isEmpty()) return null;

        List<String> statuses = Arrays.asList(csv.split(","));

        return (root, query, cb) -> {
            query.distinct(true);

            // SUBQUERY: SUM(quantity)
            var sub = query.subquery(Integer.class);
            var variantRoot = sub.from(ProductVariant.class);
            sub.select(cb.sum(variantRoot.get("quantity")))
                    .where(cb.equal(variantRoot.get("product"), root));

            var totalQty = sub;

            var inStock = cb.greaterThan(totalQty, 10);
            var lowStock = cb.and(cb.greaterThan(totalQty, 0), cb.lessThanOrEqualTo(totalQty, 10));
            var outStock = cb.or(cb.equal(totalQty, 0), cb.isNull(totalQty));

            return cb.or(
                    statuses.contains("in") ? inStock : cb.disjunction(),
                    statuses.contains("low") ? lowStock : cb.disjunction(),
                    statuses.contains("out") ? outStock : cb.disjunction()
            );
        };
    }
    public static Specification<Product> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("isActive"), active);
    }

    public static Specification<Product> sortByTotalSold() {
        return (root, query, cb) -> {
            query.distinct(true);
            var sub = query.subquery(Integer.class);
            var variantRoot = sub.from(ProductVariant.class);
            sub.select(cb.coalesce(cb.sum(variantRoot.get("sold")), 0))
                    .where(cb.equal(variantRoot.get("product"), root));
            
            // Apply sorting logic
            query.orderBy(cb.desc(sub));
            
            return cb.conjunction();
        };
    }
}
