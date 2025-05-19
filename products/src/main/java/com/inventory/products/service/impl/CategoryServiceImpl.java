package com.inventory.products.service.impl;

import com.inventory.products.exception.EntityAlreadyExistsException;
import com.inventory.products.exception.EntityInvalidArguments;
import com.inventory.products.exception.EntityNotFoundException;
import com.inventory.products.model.Category;
import com.inventory.products.repository.CategoryRepository;
import com.inventory.products.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(Category category){
        if (category == null) {
            throw new IllegalArgumentException("Category must exist");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new EntityInvalidArguments("Category name can't be null or empty");
        }
        if (categoryRepository.existsByName(category.getCategoryName())) {
            throw new EntityAlreadyExistsException("Category already exist: " + category.getCategoryName());
        }
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryByName(String categoryName){
        Category foundCategory =  categoryRepository.findByName(categoryName);
        if(foundCategory == null){
            throw new EntityNotFoundException("Category with name " + categoryName + " doesn't exist");
        }
        return foundCategory;
    }
}
