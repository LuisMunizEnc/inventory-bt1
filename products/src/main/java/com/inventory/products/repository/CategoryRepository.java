package com.inventory.products.repository;

import com.inventory.products.model.Category;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class CategoryRepository {

    HashMap<String, Category> categories = new HashMap<>();

    public Category save(Category category){
        categories.put(category.getCategoryName(), category);
        return category;
    }

    public List<Category> findAll(){
        return categories.values().stream()
                .sorted(Comparator.comparing(Category::getCategoryName))
                .collect(Collectors.toList());
    }

    public Optional<Category> findByName(String categoryName) {
        if (!hasText(categoryName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(categories.get(categoryName));
    }

    public boolean existsByName(String categoryName) {
        return hasText(categoryName) && categories.containsKey(categoryName.trim());
    }

}
