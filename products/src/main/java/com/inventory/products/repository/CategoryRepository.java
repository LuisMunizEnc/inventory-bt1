package com.inventory.products.repository;

import com.inventory.products.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Boolean existsByCategoryName(String categoryName);

    Optional<Category> findByCategoryName(String categoryName);
}
