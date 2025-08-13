package com.inventory.products.repository;

import com.inventory.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Boolean existsByName(String name);

    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryNames IS NULL OR p.category.categoryName IN :categoryNames) AND " +
            "(:available IS NULL OR (:available = true AND p.inStock > 0) OR (:available = false AND p.inStock = 0))")
    Page<Product> findByCriteria(@Param("name") String name,
                                 @Param("categoryNames") List<String> categoryNames,
                                 @Param("available") Boolean available,
                                 Pageable pageable);
}
