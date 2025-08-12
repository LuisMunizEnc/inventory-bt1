package com.inventory.products.service.impl;

import com.inventory.products.dto.*;
import com.inventory.products.exception.EntityAlreadyExistsException;
import com.inventory.products.exception.EntityNotFoundException;
import com.inventory.products.model.Category;
import com.inventory.products.model.Product;
import com.inventory.products.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryServiceImpl categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    // --- Tests for createProduct ---

    @Test
    public void givenValidProductInfo_whenCreateProduct_thenProductIsCreated() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("Laptop");
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(new BigDecimal("1200.00"));
        productInfo.setInStock(10);
        productInfo.setExpirationDate(null);

        Category existingCategory = Category.builder().categoryName("Electronics").build();
        Product expectedProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .name("Laptop")
                .category(existingCategory)
                .unitPrice(new BigDecimal("1200.00"))
                .inStock(10)
                .expirationDate(null)
                .createdAt(LocalDate.now())
                .build();

        when(categoryService.getCategoryByName("Electronics")).thenReturn(existingCategory);
        when(productRepository.existsByName("Laptop")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(expectedProduct);

        // when
        Product createdProduct = productService.createProduct(productInfo);

        // then
        assertNotNull(createdProduct);
        assertEquals("Laptop", createdProduct.getName());
        assertEquals("Electronics", createdProduct.getCategory().getCategoryName());
        verify(categoryService).getCategoryByName("Electronics");
        verify(productRepository).existsByName("Laptop");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void givenNullProductInfo_whenCreateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = null;

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product information cannot be null");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenProductInfoWithNullName_whenCreateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName(null);
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(new BigDecimal("100.00"));
        productInfo.setInStock(5);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name cannot be null or empty");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenProductInfoWithEmptyName_whenCreateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("");
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(new BigDecimal("100.00"));
        productInfo.setInStock(5);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name cannot be null or empty");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenProductInfoWithNullCategoryName_whenCreateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("Monitor");
        productInfo.setCategoryName(null);
        productInfo.setUnitPrice(new BigDecimal("200.00"));
        productInfo.setInStock(5);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product category cannot be null");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenProductInfoWithEmptyCategoryName_whenCreateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("Monitor");
        productInfo.setCategoryName("");
        productInfo.setUnitPrice(new BigDecimal("200.00"));
        productInfo.setInStock(5);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product category cannot be null");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenProductInfoWithZeroUnitPrice_whenCreateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("Mouse");
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(BigDecimal.ZERO);
        productInfo.setInStock(10);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product unit price must be greater than zero");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenProductInfoWithNegativeInStock_whenCreateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("Keyboard");
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(new BigDecimal("50.00"));
        productInfo.setInStock(-1);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product stock cannot be negative");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenNonExistingCategory_whenCreateProduct_thenThrowEntityNotFoundException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("Tablet");
        productInfo.setCategoryName("NonExistentCategory");
        productInfo.setUnitPrice(new BigDecimal("600.00"));
        productInfo.setInStock(5);

        when(categoryService.getCategoryByName("NonExistentCategory")).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category does not exist: NonExistentCategory");
        verify(categoryService).getCategoryByName("NonExistentCategory");
        verifyNoInteractions(productRepository);
    }

    @Test
    public void givenExistingProductName_whenCreateProduct_thenThrowEntityAlreadyExistsException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("ExistingProduct");
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(new BigDecimal("100.00"));
        productInfo.setInStock(5);

        Category existingCategory = Category.builder().categoryName("Electronics").build();
        when(categoryService.getCategoryByName("Electronics")).thenReturn(existingCategory);
        when(productRepository.existsByName("ExistingProduct")).thenReturn(true);

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(productInfo))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Product with name ExistingProduct already exists");
        verify(categoryService).getCategoryByName("Electronics");
        verify(productRepository).existsByName("ExistingProduct");
        verify(productRepository, never()).save(any(Product.class));
    }

    // --- Tests for updateProduct ---

    @Test
    public void givenValidProductInfo_whenUpdateProduct_thenProductIsUpdated() {
        // given
        String productId = UUID.randomUUID().toString();
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(productId);
        productInfo.setName("Updated Laptop");
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(new BigDecimal("1300.00"));
        productInfo.setInStock(12);
        productInfo.setExpirationDate(LocalDate.now().plusDays(30));

        Product existingProduct = Product.builder()
                .id(productId)
                .name("Laptop")
                .category(Category.builder().categoryName("Electronics").build())
                .unitPrice(new BigDecimal("1200.00"))
                .inStock(10)
                .createdAt(LocalDate.now().minusDays(10))
                .updatedAt(LocalDate.now())
                .build();

        Category updatedCategory = Category.builder().categoryName("Electronics").build();

        when(productRepository.findById(productId)).thenReturn(Optional.ofNullable(existingProduct));
        when(categoryService.getCategoryByName("Electronics")).thenReturn(updatedCategory);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // when
        Product updatedProduct = productService.updateProduct(productInfo);

        // then
        assertNotNull(updatedProduct);
        assertEquals(productId, updatedProduct.getId());
        assertEquals("Updated Laptop", updatedProduct.getName());
        assertEquals(0, updatedProduct.getUnitPrice().compareTo(new BigDecimal("1300.00")));
        assertEquals(12, updatedProduct.getInStock());
        assertEquals("Electronics", updatedProduct.getCategory().getCategoryName());
        assertNotNull(updatedProduct.getUpdatedAt());

        verify(productRepository).findById(productId);
        verify(categoryService).getCategoryByName("Electronics");
        assertNotNull(existingProduct);
        verify(productRepository).save(existingProduct);
    }

    @Test
    public void givenNullProductInfoForUpdate_whenUpdateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = null;

        // when
        // then
        assertThatThrownBy(() -> productService.updateProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product information cannot be null");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenProductInfoWithoutIdForUpdate_whenUpdateProduct_thenThrowIllegalArgumentException() {
        // given
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName("Test");
        productInfo.setCategoryName("Test");
        productInfo.setUnitPrice(BigDecimal.ONE);
        productInfo.setInStock(1);
        productInfo.setId(null);

        // when
        // then
        assertThatThrownBy(() -> productService.updateProduct(productInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID is required for updating");
        verifyNoInteractions(productRepository, categoryService);
    }

    @Test
    public void givenNonExistingProductId_whenUpdateProduct_thenThrowEntityNotFoundException() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(nonExistentId);
        productInfo.setName("NonExistent Product");
        productInfo.setCategoryName("Electronics");
        productInfo.setUnitPrice(new BigDecimal("100.00"));
        productInfo.setInStock(5);

        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> productService.updateProduct(productInfo))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with ID: " + nonExistentId);
        verify(productRepository).findById(nonExistentId);
        verifyNoInteractions(categoryService);
    }

    @Test
    public void givenNonExistingCategoryForUpdate_whenUpdateProduct_thenThrowEntityNotFoundException() {
        // given
        String productId = UUID.randomUUID().toString();
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(productId);
        productInfo.setName("Product");
        productInfo.setCategoryName("NonExistentCategory");
        productInfo.setUnitPrice(new BigDecimal("100.00"));
        productInfo.setInStock(5);

        Product existingProduct = Product.builder()
                .id(productId)
                .name("Product")
                .category(Category.builder().categoryName("OldCategory").build())
                .unitPrice(new BigDecimal("100.00"))
                .inStock(5)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.ofNullable(existingProduct));
        when(categoryService.getCategoryByName("NonExistentCategory")).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> productService.updateProduct(productInfo))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category does not exist: NonExistentCategory");
        verify(productRepository).findById(productId);
        verify(categoryService).getCategoryByName("NonExistentCategory");
        verify(productRepository, never()).save(any(Product.class));
    }

    // --- Tests for deleteProductById ---

    @Test
    public void givenExistingProductId_whenDeleteProductById_thenProductIsDeleted() {
        // given
        String productId = UUID.randomUUID().toString();

        // when
        productService.deleteProductById(productId);

        // then
        verify(productRepository).deleteById(productId);
    }

    @Test
    public void givenNullProductId_whenDeleteProductById_thenThrowIllegalArgumentException() {
        // given
        String productId = null;

        // when
        // then
        assertThatThrownBy(() -> productService.deleteProductById(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty for deletion");
        verifyNoInteractions(productRepository);
    }

    @Test
    public void givenEmptyProductId_whenDeleteProductById_thenThrowIllegalArgumentException() {
        // given
        String productId = "";

        // when
        // then
        assertThatThrownBy(() -> productService.deleteProductById(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty for deletion");
        verifyNoInteractions(productRepository);
    }

    // --- Tests for getAllProducts ---

    @Test
    public void givenNoProducts_whenGetAllProducts_thenReturnEmptyList() {
        // given
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        List<Product> products = productService.getAllProducts();

        // then
        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(productRepository).findAll();
    }

    @Test
    public void givenSomeProducts_whenGetAllProducts_thenReturnListOfProducts() {
        // given
        List<Product> expectedProducts = Arrays.asList(
                Product.builder().id(UUID.randomUUID().toString()).name("Product A").build(),
                Product.builder().id(UUID.randomUUID().toString()).name("Product B").build()
        );
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // when
        List<Product> products = productService.getAllProducts();

        // then
        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals("Product A", products.get(0).getName());
        assertEquals("Product B", products.get(1).getName());
        verify(productRepository).findAll();
    }

    // --- Tests for getProductById ---

    @Test
    public void givenValidId_whenGetProductById_thenReturnProduct() {
        // given
        String productId = UUID.randomUUID().toString();
        Product expectedProduct = Product.builder().id(productId).name("Test Product").build();
        when(productRepository.findById(productId)).thenReturn(Optional.ofNullable(expectedProduct));

        // when
        Product foundProduct = productService.getProductById(productId);

        // then
        assertNotNull(foundProduct);
        assertEquals(productId, foundProduct.getId());
        verify(productRepository).findById(productId);
    }

    @Test
    public void givenNullId_whenGetProductById_thenThrowIllegalArgumentException() {
        // given
        String productId = null;

        // when
        // then
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty");
        verifyNoInteractions(productRepository);
    }

    @Test
    public void givenEmptyId_whenGetProductById_thenThrowIllegalArgumentException() {
        // given
        String productId = "";

        // when
        // then
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product ID cannot be null or empty");
        verifyNoInteractions(productRepository);
    }

    @Test
    public void givenNonExistingId_whenGetProductById_thenThrowEntityNotFoundException() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> productService.getProductById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with ID: " + nonExistentId);
        verify(productRepository).findById(nonExistentId);
    }

    // --- Tests for getProductsByCriteria ---

    @Test
    public void givenNameFilter_whenGetProductsByCriteria_thenReturnMatchingProducts() {
        // given
        String nameFilter = "lap";
        List<Product> allProducts = Arrays.asList(
                Product.builder().name("Laptop Pro").category(Category.builder().categoryName("Electronics").build()).inStock(5).build(),
                Product.builder().name("Mouse").category(Category.builder().categoryName("Electronics").build()).inStock(10).build(),
                Product.builder().name("Tablet").category(Category.builder().categoryName("Electronics").build()).inStock(0).build(),
                Product.builder().name("Apple Pie").category(Category.builder().categoryName("Food").build()).inStock(15).build()
        );
        when(productRepository.findByCriteria(nameFilter, null, false)).thenReturn(Arrays.asList(allProducts.get(0)));

        // when
        List<Product> filteredProducts = productService.getProductsByCriteria(nameFilter, null, false);

        // then
        assertNotNull(filteredProducts);
        assertEquals(1, filteredProducts.size());
        assertEquals("Laptop Pro", filteredProducts.getFirst().getName());
        verify(productRepository).findByCriteria(nameFilter, null, false);
    }

    @Test
    public void givenCategoryFilter_whenGetProductsByCriteria_thenReturnMatchingProducts() {
        // given
        List<String> categoryFilter = List.of("Electronics");
        List<Product> allProducts = Arrays.asList(
                Product.builder().name("Laptop Pro").category(Category.builder().categoryName("Electronics").build()).inStock(5).build(),
                Product.builder().name("Mouse").category(Category.builder().categoryName("Electronics").build()).inStock(10).build(),
                Product.builder().name("Tablet").category(Category.builder().categoryName("Electronics").build()).inStock(0).build(),
                Product.builder().name("Apple Pie").category(Category.builder().categoryName("Food").build()).inStock(15).build()
        );
        when(productRepository.findByCriteria(null, categoryFilter, false)).thenReturn(Arrays.asList(allProducts.get(0), allProducts.get(1), allProducts.get(2)));

        // when
        List<Product> filteredProducts = productService.getProductsByCriteria(null, categoryFilter, false);

        // then
        assertNotNull(filteredProducts);
        assertEquals(3, filteredProducts.size());
        assertTrue(filteredProducts.stream().allMatch(p -> p.getCategory().getCategoryName().equals("Electronics")));
        verify(productRepository).findByCriteria(null, categoryFilter, false);
    }

    @Test
    public void givenNameAndAvailabilityFilter_whenGetProductsByCriteria_thenReturnMatchingProducts() {
        // given
        String nameFilter = "lap";
        boolean availabilityFilter = true;
        List<Product> allProducts = Arrays.asList(
                Product.builder().name("Laptop Pro").category(Category.builder().categoryName("Electronics").build()).inStock(5).build(),
                Product.builder().name("Laptop Basic").category(Category.builder().categoryName("Electronics").build()).inStock(0).build(),
                Product.builder().name("Mouse").category(Category.builder().categoryName("Electronics").build()).inStock(10).build(),
                Product.builder().name("Apple Pie").category(Category.builder().categoryName("Food").build()).inStock(15).build()
        );
        when(productRepository.findByCriteria(nameFilter, null, availabilityFilter)).thenReturn(Arrays.asList(allProducts.get(0)));

        // when
        List<Product> filteredProducts = productService.getProductsByCriteria(nameFilter, null, availabilityFilter);

        // then
        assertNotNull(filteredProducts);
        assertEquals(1, filteredProducts.size());
        assertEquals("Laptop Pro", filteredProducts.getFirst().getName());
        assertTrue(filteredProducts.getFirst().getInStock()>0);
        verify(productRepository).findByCriteria(nameFilter, null, availabilityFilter);
    }

    // --- Tests for setProductInStock ---

    @Test
    public void givenExistingProductId_whenSetProductInStock_thenProductStockIsUpdated() {
        // given
        String productId = UUID.randomUUID().toString();
        Product product = Product.builder()
                .id(productId)
                .inStock(0)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        productService.setProductInStock(productId);

        // then
        verify(productRepository).findById(productId);
        verify(productRepository).save(argThat(p -> p.getInStock() == 10));
    }

    @Test
    public void givenNonExistingProductId_whenSetProductInStock_thenThrowEntityNotFoundException() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> productService.setProductInStock(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with ID: " + nonExistentId);
        verify(productRepository).findById(nonExistentId);
    }

    // --- Tests for setProductOutOfStock ---

    @Test
    public void givenExistingProductId_whenSetProductOutOfStock_thenProductStockIsUpdated() {
        // given
        String productId = UUID.randomUUID().toString();
        Product product = Product.builder()
                .id(productId)
                .inStock(10)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        productService.setProductOutOfStock(productId);
        // then
        verify(productRepository).findById(productId);
        verify(productRepository).save(argThat(p -> p.getInStock() == 0));
    }

    @Test
    public void givenProductAlreadyOutOfStock_whenSetProductOutOfStock_thenStockRemainsZero() {
        // given
        String productId = UUID.randomUUID().toString();
        Product product = Product.builder()
                .id(productId)
                .inStock(0)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        productService.setProductOutOfStock(productId);

        // then
        verify(productRepository).findById(productId);
        verify(productRepository).save(argThat(p -> p.getInStock() == 0));
        assertEquals(0, product.getInStock());
    }

    @Test
    public void givenNonExistingProductId_whenSetProductOutOfStock_thenThrowEntityNotFoundException() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> productService.setProductOutOfStock(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with ID: "+ nonExistentId);
        verify(productRepository).findById(nonExistentId);
    }

    // --- Tests for getInventoryReport ---

    @Test
    public void givenEmptyInventory_whenGetInventoryReport_thenReturnEmptyMetrics() {
        // given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        InventoryMetricsReport report = productService.getInventoryReport();

        // then
        assertNotNull(report);
        assertNotNull(report.getCategoryMetrics());
        assertTrue(report.getCategoryMetrics().isEmpty());

        OverallMetrics overall = report.getOverallMetrics();
        assertNotNull(overall);
        assertEquals(0, overall.getTotalProductsInStock());
        assertEquals(BigDecimal.ZERO, overall.getTotalValueInStock());
        assertEquals(BigDecimal.ZERO, overall.getAveragePriceInStock());

        verify(productRepository).findAll();
    }

    @Test
    public void givenSingleProductInInventory_whenGetInventoryReport_thenReturnCorrectMetrics() {
        // given
        Category electronics = Category.builder().categoryName("Electronics").build();
        Product laptop = Product.builder()
                .name("Laptop")
                .category(electronics)
                .unitPrice(new BigDecimal("1200.00"))
                .inStock(5)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(laptop));

        // when
        InventoryMetricsReport report = productService.getInventoryReport();

        // then
        assertNotNull(report);

        OverallMetrics overall = report.getOverallMetrics();
        assertEquals(5, overall.getTotalProductsInStock());
        assertEquals(new BigDecimal("6000.00"), overall.getTotalValueInStock());
        assertEquals(new BigDecimal("1200.00"), overall.getAveragePriceInStock());

        List<CategoryMetrics> categoryMetrics = report.getCategoryMetrics();
        assertEquals(1, categoryMetrics.size());

        CategoryMetrics electronicsMetrics = categoryMetrics.getFirst();
        assertEquals("Electronics", electronicsMetrics.getCategoryName());
        assertEquals(5, electronicsMetrics.getTotalProductsInStock());
        assertEquals(new BigDecimal("6000.00"), electronicsMetrics.getTotalValueInStock());
        assertEquals(new BigDecimal("1200.00"), electronicsMetrics.getAveragePriceInStock());

        verify(productRepository).findAll();
    }

    @Test
    public void givenMultipleProductsInMultipleCategories_whenGetInventoryReport_thenReturnCorrectMetrics() {
        // given
        Category electronics = Category.builder().categoryName("Electronics").build();
        Category food = Category.builder().categoryName("Food").build();

        List<Product> products = Arrays.asList(
                Product.builder().name("Laptop").category(electronics)
                        .unitPrice(new BigDecimal("1200.00")).inStock(2).build(),
                Product.builder().name("Phone").category(electronics)
                        .unitPrice(new BigDecimal("800.00")).inStock(3).build(),
                Product.builder().name("Apple").category(food)
                        .unitPrice(new BigDecimal("1.50")).inStock(10).build(),
                Product.builder().name("Bread").category(food)
                        .unitPrice(new BigDecimal("2.00")).inStock(0).build()
        );

        when(productRepository.findAll()).thenReturn(products);

        InventoryMetricsReport report = productService.getInventoryReport();

        assertNotNull(report);

        OverallMetrics overall = report.getOverallMetrics();
        assertEquals(15, overall.getTotalProductsInStock());
        assertEquals(new BigDecimal("4815.00"), overall.getTotalValueInStock());
        assertTrue(overall.getAveragePriceInStock().compareTo(new BigDecimal("667.16")) >= 0.01);

        List<CategoryMetrics> categoryMetrics = report.getCategoryMetrics();
        assertEquals(2, categoryMetrics.size());

        CategoryMetrics electronicsMetrics = categoryMetrics.getFirst();
        assertEquals("Electronics", electronicsMetrics.getCategoryName());
        assertEquals(5, electronicsMetrics.getTotalProductsInStock());
        assertEquals(new BigDecimal("4800.00"), electronicsMetrics.getTotalValueInStock());
        assertEquals(new BigDecimal("1000.00"), electronicsMetrics.getAveragePriceInStock());

        CategoryMetrics foodMetrics = categoryMetrics.get(1);
        assertEquals("Food", foodMetrics.getCategoryName());
        assertEquals(10, foodMetrics.getTotalProductsInStock());
        assertEquals(new BigDecimal("15.00"), foodMetrics.getTotalValueInStock());
        assertEquals(new BigDecimal("1.50"), foodMetrics.getAveragePriceInStock());

        verify(productRepository).findAll();
    }

    @Test
    public void givenProductsOutOfStock_whenGetInventoryReport_thenExcludeFromMetrics() {
        // given
        Category electronics = Category.builder().categoryName("Electronics").build();

        List<Product> products = Arrays.asList(
                Product.builder().name("Laptop").category(electronics)
                        .unitPrice(new BigDecimal("1200.00")).inStock(0).build(),
                Product.builder().name("Phone").category(electronics)
                        .unitPrice(new BigDecimal("800.00")).inStock(0).build()
        );

        when(productRepository.findAll()).thenReturn(products);

        // when
        InventoryMetricsReport report = productService.getInventoryReport();

        // then
        assertNotNull(report);

        OverallMetrics overall = report.getOverallMetrics();
        assertEquals(0, overall.getTotalProductsInStock());
        assertEquals(BigDecimal.ZERO, overall.getTotalValueInStock());
        assertEquals(BigDecimal.ZERO, overall.getAveragePriceInStock());
        assertTrue(report.getCategoryMetrics().isEmpty());

        verify(productRepository).findAll();
    }
}