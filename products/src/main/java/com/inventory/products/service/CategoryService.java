package com.inventory.products.service;

import com.inventory.products.model.Category;

import java.util.List;

public interface CategoryService {

    Category createCategory(Category category);

    List<Category> getAllCategories();

    Category getCategoryByName(String categoryName);
}
