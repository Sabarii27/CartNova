package com.sabari.cartnova.repository;

import com.sabari.cartnova.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Builds the dynamic WHERE clause for product search and filtering. */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> filter(String search, String category, BigDecimal minPrice,
                                                BigDecimal maxPrice, Boolean available) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (StringUtils.hasText(search)) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category").get("name")), category.trim().toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (available != null) {
                predicates.add(available ? cb.greaterThan(root.get("stock"), 0) : cb.equal(root.get("stock"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
