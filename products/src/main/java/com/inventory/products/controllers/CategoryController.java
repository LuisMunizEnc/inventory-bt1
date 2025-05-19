package com.inventory.products.controllers;

import com.inventory.products.model.Category;
import com.inventory.products.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        log.info("Received request to create category: {}", category);
        Category createdCategory = categoryService.createCategory(category);
        log.info("Category created successfully: {}", createdCategory);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        log.info("Received request to get all categories");
        List<Category> categories = categoryService.getAllCategories();
        log.info("Returning {} categories", categories.size());
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        log.info("Received request to get category by name: {}", name);
        Category category = categoryService.getCategoryByName(name);
        log.info("Returning category: {}", category);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }
}
