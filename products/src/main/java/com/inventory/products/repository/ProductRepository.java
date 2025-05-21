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
        if (products.get(id) == null) {
            product.setCreatedAt(LocalDate.now());
        } else {
            product.setUpdatedAt(LocalDate.now());
        }
        products.put(id, product);
        return product;
    }

    public Product findById(String id) {
        return products.get(id);
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
                .filter(product ->
                        (!hasText(nameFilter) || product.getName().toLowerCase().contains(nameFilter.toLowerCase())) &&
                                (categoryFilter == null || categoryFilter.isEmpty() || categoryFilter.contains(product.getCategory().getCategoryName())) &&
                                (!availabilityFilter || product.getInStock() > 0)
                )
                .collect(Collectors.toList());
    }

    // Toggle availability
    public void updateAvailability(Product product, boolean inStock) {
        product.setInStock(inStock ? 0 : 10);
        product.setUpdatedAt(LocalDate.now());
        products.put(product.getId(), product);
    }

    public int getTotalProductsInStock() {
        return products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .mapToInt(Product::getInStock)
                .sum();
    }

    public double getTotalValueOfInventory() {
        return products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .mapToDouble(product -> product.getUnitPrice() * product.getInStock())
                .sum();
    }

    public double getAveragePriceOfInStockProducts() {
        long count = products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .count();
        if (count == 0) {
            return 0;
        }
        return products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .mapToDouble(Product::getUnitPrice)
                .sum() / count;
    }

    public Map<String, Integer> getTotalProductsInStockByCategory() {
        return products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .collect(Collectors.groupingBy(p -> p.getCategory().getCategoryName(), Collectors.summingInt(Product::getInStock)));
    }

    public Map<String, Double> getTotalValueOfInventoryByCategory() {
        return products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .collect(Collectors.groupingBy(p -> p.getCategory().getCategoryName(), Collectors.summingDouble(p -> p.getUnitPrice() * p.getInStock())));
    }

    public Map<String, Double> getAveragePriceOfInStockProductsByCategory() {
        Map<String, Long> counts = products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .collect(Collectors.groupingBy(p -> p.getCategory().getCategoryName(), Collectors.counting()));

        Map<String, Double> sums = products.values().stream()
                .filter(product -> product.getInStock() > 0)
                .collect(Collectors.groupingBy(p -> p.getCategory().getCategoryName(), Collectors.summingDouble(Product::getUnitPrice)));

        Map<String, Double> averages = new HashMap<>();
        sums.forEach((categoryName, sum) -> {
            Long count = counts.get(categoryName);
            averages.put(categoryName, count == 0 ? 0 : sum / count);
        });
        return averages;
    }
}
