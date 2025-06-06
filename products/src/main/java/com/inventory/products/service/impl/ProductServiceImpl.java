package com.inventory.products.service.impl;

import com.inventory.products.dto.*;
import com.inventory.products.exception.EntityAlreadyExistsException;
import com.inventory.products.exception.EntityNotFoundException;
import com.inventory.products.model.Category;
import com.inventory.products.model.Product;
import com.inventory.products.repository.ProductRepository;
import com.inventory.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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

    public void deleteProductById(String id) {
        if (!hasText(id)) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for deletion");
        }
        boolean deleted = productRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException("Product not found with ID: " + id + " for deletion");
        }
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

    public List<Product> getProductsByCriteria(String nameFilter, List<String> categoryFilter,
                                               Boolean availabilityFilter) {
        return productRepository.findByCriteria(nameFilter, categoryFilter, availabilityFilter);
    }

    public void setProductInStock(String productId){
        Optional<Product> productFound = productRepository.findById(productId);
        productFound.ifPresent(product -> productRepository.updateAvailability
                (product, true));
    }

    public void setProductOutOfStock(String productId){
        Optional<Product> productFound = productRepository.findById(productId);
        productFound.ifPresent(product -> productRepository.updateAvailability
                (product, false));
    }

    private boolean isInStock(Product product){
        return product.getInStock()>0;
    }

    private InventoryMetrics calculateInventoryMetrics() {
        List<Product> inStockProducts = productRepository.findAll().stream()
                .filter(this::isInStock)
                .toList();

        int totalProductsInStock = inStockProducts.size();
        int totalUnitsInStock = 0;
        BigDecimal totalValueOfInventory = BigDecimal.ZERO;
        BigDecimal sumOfUnitPrices = BigDecimal.ZERO;
        Map<String, Integer> productsInStockByCategory = new HashMap<>();
        Map<String, BigDecimal> totalValueOfInventoryByCategory = new HashMap<>();
        Map<String, BigDecimal> sumOfUnitPricesByCategory = new HashMap<>();
        Map<String, Long> countByCategory = new HashMap<>();

        for (Product product : inStockProducts) {
            BigDecimal productValue = product.getUnitPrice().multiply(BigDecimal.valueOf(product.getInStock()));
            totalValueOfInventory = totalValueOfInventory.add(productValue);
            sumOfUnitPrices = sumOfUnitPrices.add(product.getUnitPrice());
            totalUnitsInStock += product.getInStock();

            String categoryName = product.getCategory().getCategoryName();
            productsInStockByCategory.merge(categoryName, product.getInStock(), Integer::sum);
            totalValueOfInventoryByCategory.merge(categoryName, productValue, BigDecimal::add);
            sumOfUnitPricesByCategory.merge(categoryName, product.getUnitPrice(), BigDecimal::add);
            countByCategory.merge(categoryName, 1L, Long::sum);
        }

        BigDecimal averagePriceOfInStockProducts = totalProductsInStock == 0 ? BigDecimal.ZERO :
                sumOfUnitPrices.divide(BigDecimal.valueOf(totalProductsInStock), 2, RoundingMode.HALF_UP);
        Map<String, BigDecimal> averagePriceOfInStockProductsByCategory = new HashMap<>();
        countByCategory.forEach((category, count) -> {
            averagePriceOfInStockProductsByCategory.put
                    (category, sumOfUnitPricesByCategory.get(category).divide(BigDecimal.valueOf(count),
                            2, RoundingMode.HALF_UP));
        });

        return new InventoryMetrics(
                totalUnitsInStock,
                totalValueOfInventory,
                averagePriceOfInStockProducts,
                productsInStockByCategory,
                totalValueOfInventoryByCategory,
                averagePriceOfInStockProductsByCategory
        );
    }

    public InventoryMetricsReport getInventoryReport() {
        InventoryMetrics metrics = calculateInventoryMetrics();
        List<CategoryMetrics> categoryMetricsList = new ArrayList<>();

        metrics.getProductsInStockByCategory().forEach((categoryName, count) -> {
            categoryMetricsList.add(CategoryMetrics.builder()
                    .categoryName(categoryName)
                    .totalProductsInStock(count)
                    .totalValueInStock(metrics.getTotalValueOfInventoryByCategory().getOrDefault(categoryName, BigDecimal.ZERO))
                    .averagePriceInStock(metrics.getAveragePriceOfInStockProductsByCategory().getOrDefault(categoryName, BigDecimal.ZERO))
                    .build());
        });

        categoryMetricsList.sort(Comparator.comparing(CategoryMetrics::getCategoryName));

        OverallMetrics overallMetrics = OverallMetrics.builder()
                .totalProductsInStock(metrics.getTotalProductsInStock())
                .totalValueInStock(metrics.getTotalValueOfInventory())
                .averagePriceInStock(metrics.getAveragePriceOfInStockProducts())
                .build();

        return InventoryMetricsReport.builder()
                .categoryMetrics(categoryMetricsList)
                .overallMetrics(overallMetrics)
                .build();
    }
}

