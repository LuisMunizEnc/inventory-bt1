package com.inventory.products.repository;

import com.inventory.products.model.Category;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class CategoryRepository {

    HashMap<String, Category> categories = new HashMap<>();

    public Category save(Category category){
        categories.put(category.getCategoryName(), category);
        return category;
    }

    public List<Category> findAll(){
        return new ArrayList<>(categories.values());
    }

    public Category findByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return null;
        }
        return categories.get(categoryName.trim());
    }

    public boolean existsByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        return categories.containsKey(categoryName.trim());
    }

}
