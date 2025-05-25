package com.inventory.products.repository;

import com.inventory.products.model.Product;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Repository
public class ProductRepository {

    private final Map<String, Product> products = new HashMap<>();

    public Product save(Product product) {
        String id = product.getId() == null ? UUID.randomUUID().toString() : product.getId();
        product.setId(id);
        if (!products.containsKey(id)) {
            product.setCreatedAt(LocalDate.now());
        } else {
            product.setUpdatedAt(LocalDate.now());
        }
        products.put(id, product);
        return product;
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public List<Product> findAll() {
        return products.values().stream()
                .sorted(Comparator.comparing(Product::getName))
                .collect(Collectors.toList());
    }

    public boolean existsByName(String name) {
        return products.values().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }

    public List<Product> findByCriteria(String nameFilter, List<String> categoryFilter, boolean availabilityFilter) {
        return products.values().stream()
                .filter(product -> isNameMatching(product, nameFilter))
                .filter(product -> isCategoryMatching(product, categoryFilter))
                .filter(product -> isAvailabilityMatching(product, availabilityFilter))
                .collect(Collectors.toList());
    }

    private boolean isNameMatching(Product product, String nameFilter) {
        return !hasText(nameFilter) || product.getName().toLowerCase().contains(nameFilter.toLowerCase());
    }

    private boolean isCategoryMatching(Product product, List<String> categoryFilter) {
        return categoryFilter == null || categoryFilter.isEmpty() || categoryFilter.contains(product.getCategory().getCategoryName());
    }

    private boolean isAvailabilityMatching(Product product, boolean availabilityFilter) {
        return !availabilityFilter || product.getInStock() > 0;
    }

    public void updateAvailability(Product product, boolean setStock) {
        product.setInStock(setStock ? 10 : 0);
        product.setUpdatedAt(LocalDate.now());
        products.put(product.getId(), product);
    }
}
