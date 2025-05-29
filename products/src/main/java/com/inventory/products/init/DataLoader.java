package com.inventory.products.init;

import com.inventory.products.dto.ProductInfo;
import com.inventory.products.exception.EntityAlreadyExistsException;
import com.inventory.products.model.Category;
import com.inventory.products.service.CategoryService;
import com.inventory.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final CategoryService categoryService;
    private final ProductService productService;

    public DataLoader(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Init Data...");

        createCategoryIfNotFound("Electronics");
        createCategoryIfNotFound("Food");
        createCategoryIfNotFound("Books");
        createCategoryIfNotFound("Clothing");
        createCategoryIfNotFound("Home & Kitchen");
        createCategoryIfNotFound("Sports");
        createCategoryIfNotFound("Beauty");
        createCategoryIfNotFound("Toys");

        createProductIfNotFound("Laptop Pro", "Electronics", new BigDecimal("1500.00"), 10, null);
        createProductIfNotFound("Smartphone X", "Electronics", new BigDecimal("800.00"), 25, null);
        createProductIfNotFound("Wireless Headphones", "Electronics", new BigDecimal("150.00"), 40, null);
        createProductIfNotFound("Smartwatch 5", "Electronics", new BigDecimal("300.00"), 15, null);
        createProductIfNotFound("Gaming Mouse", "Electronics", new BigDecimal("75.00"), 30, null);

        createProductIfNotFound("Organic Apples", "Food", new BigDecimal("2.50"), 100, LocalDate.now().plusDays(7));
        createProductIfNotFound("Whole Wheat Bread", "Food", new BigDecimal("3.20"), 50, LocalDate.now().plusDays(3));
        createProductIfNotFound("Milk Carton", "Food", new BigDecimal("1.80"), 60, LocalDate.now().plusDays(10));
        createProductIfNotFound("Cereal Box", "Food", new BigDecimal("4.50"), 80, LocalDate.now().plusMonths(6));
        createProductIfNotFound("Coffee Beans", "Food", new BigDecimal("12.00"), 35, null);

        createProductIfNotFound("The Great Novel", "Books", new BigDecimal("25.00"), 30, null);
        createProductIfNotFound("Science Textbook", "Books", new BigDecimal("70.00"), 8, null);
        createProductIfNotFound("Fantasy Series Vol. 1", "Books", new BigDecimal("18.00"), 20, null);

        createProductIfNotFound("Summer T-Shirt", "Clothing", new BigDecimal("15.99"), 20, null);
        createProductIfNotFound("Jeans Slim Fit", "Clothing", new BigDecimal("45.00"), 18, null);
        createProductIfNotFound("Winter Jacket", "Clothing", new BigDecimal("89.99"), 5, null);

        createProductIfNotFound("Blender Pro", "Home & Kitchen", new BigDecimal("99.99"), 12, null);
        createProductIfNotFound("Coffee Maker", "Home & Kitchen", new BigDecimal("70.00"), 8, null);

        createProductIfNotFound("Yoga Mat", "Sports", new BigDecimal("29.99"), 25, null);
        createProductIfNotFound("Dumbbell Set", "Sports", new BigDecimal("55.00"), 10, null);

        createProductIfNotFound("Face Moisturizer", "Beauty", new BigDecimal("22.50"), 30, null);
        createProductIfNotFound("Shampoo Large", "Beauty", new BigDecimal("10.00"), 50, null);

        createProductIfNotFound("Building Blocks Set", "Toys", new BigDecimal("35.00"), 40, null);
        createProductIfNotFound("Remote Control Car", "Toys", new BigDecimal("60.00"), 15, null);

        createProductIfNotFound("Expired Milk", "Food", new BigDecimal("1.00"), 0, LocalDate.now().minusDays(1)); // Producto fuera de stock y expirado

        log.info("Finished.");
    }

    private void createCategoryIfNotFound(String categoryName) {
        try {
            categoryService.createCategory(Category.builder().categoryName(categoryName).build());
            log.info("Category '{}' created.", categoryName);
        } catch (EntityAlreadyExistsException e) {
            log.warn("Category '{}' already exists.", categoryName);
        } catch (IllegalArgumentException e) {
            log.error("Error with '{}': {}", categoryName, e.getMessage());
        }
    }

    private void createProductIfNotFound(String name, String categoryName, BigDecimal unitPrice, int inStock, LocalDate expirationDate) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName(name);
        productInfo.setCategoryName(categoryName);
        productInfo.setUnitPrice(unitPrice);
        productInfo.setInStock(inStock);
        productInfo.setExpirationDate(expirationDate);

        try {
            productService.createProduct(productInfo);
            log.info("Product '{}' created.", name);
        } catch (EntityAlreadyExistsException e) {
            log.warn("Product '{}' already exists.", name);
        } catch (Exception e) {
            log.error("Error with '{}': {}", name, e.getMessage());
        }
    }
}
