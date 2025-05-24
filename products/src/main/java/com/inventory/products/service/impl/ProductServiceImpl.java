package com.inventory.products.service.impl;

import com.inventory.products.dto.CategoryMetrics;
import com.inventory.products.dto.InventoryMetricsReport;
import com.inventory.products.dto.OverallMetrics;
import com.inventory.products.exception.EntityAlreadyExistsException;
import com.inventory.products.exception.EntityNotFoundException;
import com.inventory.products.model.Category;
import com.inventory.products.model.Product;
import com.inventory.products.repository.ProductRepository;
import com.inventory.products.service.ProductService;
import com.inventory.products.dto.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDate;

import static org.springframework.util.StringUtils.hasText;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryServiceImpl categoryService;

    @Autowired
    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryServiceImpl categoryService
    ){
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    private void validateProductInfo(ProductInfo productInfo) {
        if (productInfo == null) {
            throw new IllegalArgumentException("Product information cannot be null");
        }
        if (!hasText(productInfo.getName())) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (!hasText(productInfo.getCategoryName())) {
            throw new IllegalArgumentException("Product category cannot be null");
        }
        if (productInfo.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product unit price must be greater than zero");
        }
        if (productInfo.getInStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }

    public Product createProduct(ProductInfo productInfo) {
        validateProductInfo(productInfo);

        Category category = categoryService.getCategoryByName(productInfo.getCategoryName());
        if (category == null) {
            throw new EntityNotFoundException("Category does not exist: " + productInfo.getCategoryName());
        }

        Product product = Product.builder()
                .name(productInfo.getName())
                .category(category)
                .unitPrice(productInfo.getUnitPrice())
                .expirationDate(productInfo.getExpirationDate())
                .inStock(productInfo.getInStock())
                .build();

        if (productRepository.existsByName(product.getName())) {
            throw new EntityAlreadyExistsException("Product with name " + product.getName() + " already exists");
        }
        return productRepository.save(product);
    }

    public Product updateProduct(ProductInfo productInfo) {
        validateProductInfo(productInfo);
        if (productInfo.getId() == null) {
            throw new IllegalArgumentException("Product ID is required for updating");
        }
        Optional<Product> productFound = productRepository.findById(productInfo.getId());

        if (productFound.isEmpty()) {
            throw new EntityNotFoundException("Product not found with ID: " + productInfo.getId());
        }

        Category category = categoryService.getCategoryByName(productInfo.getCategoryName());
        if (category == null) {
            throw new EntityNotFoundException("Category does not exist: " + productInfo.getCategoryName());
        }

        Product existingProduct = productFound.get();

        existingProduct.setName(productInfo.getName());
        existingProduct.setCategory(category);
        existingProduct.setUnitPrice(productInfo.getUnitPrice());
        existingProduct.setExpirationDate(productInfo.getExpirationDate());
        existingProduct.setInStock(productInfo.getInStock());

        return productRepository.save(existingProduct);
    }

    public Product getProductById(String productId){
        if(!hasText(productId)) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        Optional<Product> productFound =  productRepository.findById(productId);
        return productFound.orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCriteria(String nameFilter, List<String> categoryFilter, boolean availabilityFilter) {
        return productRepository.findByCriteria(nameFilter, categoryFilter, availabilityFilter);
    }

    public void updateProductAvailability(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Product IDs list cannot be null or empty");
        }

        for (String id : productIds) {
            Optional<Product> productFound = productRepository.findById(id);
            productFound.ifPresent(product -> productRepository.updateAvailability(product, product.getInStock() > 0));
        }
    }

    public InventoryMetricsReport getInventoryReport() {
        List<CategoryMetrics> categoryMetricsList = getCategoryMetrics();

        categoryMetricsList.sort(Comparator.comparing(CategoryMetrics::getCategoryName));

        int overallTotalProductsInStock = productRepository.getTotalProductsInStock();
        BigDecimal overallTotalValueOfInventory = productRepository.getTotalValueOfInventory();
        BigDecimal overallAveragePriceOfInStockProducts = productRepository.getAveragePriceOfInStockProducts();

        OverallMetrics overallMetrics = OverallMetrics.builder()
                .totalProductsInStock(overallTotalProductsInStock)
                .totalValueInStock(overallTotalValueOfInventory)
                .averagePriceInStock(overallAveragePriceOfInStockProducts)
                .build();

        return InventoryMetricsReport.builder()
                .categoryMetrics(categoryMetricsList)
                .overallMetrics(overallMetrics)
                .build();
    }

    private List<CategoryMetrics> getCategoryMetrics() {
        Map<String, Integer> productsInStockByCategory = productRepository.getTotalProductsInStockByCategory();
        Map<String, BigDecimal> totalValueOfInventoryByCategory = productRepository.getTotalValueOfInventoryByCategory();
        Map<String, BigDecimal> averagePriceOfInStockProductsByCategory = productRepository.getAveragePriceOfInStockProductsByCategory();

        List<CategoryMetrics> categoryMetricsList = new ArrayList<>();
        productsInStockByCategory.forEach((categoryName, count) -> {
            categoryMetricsList.add(CategoryMetrics.builder()
                    .categoryName(categoryName)
                    .totalProductsInStock(count)
                    .totalValueInStock(totalValueOfInventoryByCategory.getOrDefault(categoryName, BigDecimal.ZERO))
                    .averagePriceInStock(averagePriceOfInStockProductsByCategory.getOrDefault(categoryName, BigDecimal.ZERO))
                    .build());
        });
        return categoryMetricsList;
    }

}
