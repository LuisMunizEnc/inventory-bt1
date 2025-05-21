package com.inventory.products.service.impl;

import com.inventory.products.dto.CategoryMetrics;
import com.inventory.products.dto.InventoryMetricsReport;
import com.inventory.products.dto.ProductInfo;
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
        productInfo.setUnitPrice(1200.0f);
        productInfo.setInStock(10);
        productInfo.setExpirationDate(null);

        Category existingCategory = Category.builder().categoryName("Electronics").build();
        Product expectedProduct = Product.builder()
                .id(UUID.randomUUID().toString())
                .name("Laptop")
                .category(existingCategory)
                .unitPrice(1200.0f)
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
        productInfo.setUnitPrice(100.0f);
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
        productInfo.setUnitPrice(100.0f);
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
        productInfo.setUnitPrice(200.0f);
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
        productInfo.setUnitPrice(200.0f);
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
        productInfo.setUnitPrice(0.0f);
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
        productInfo.setUnitPrice(50.0f);
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
        productInfo.setUnitPrice(300.0f);
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
        productInfo.setUnitPrice(100.0f);
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
        productInfo.setUnitPrice(1300.0f);
        productInfo.setInStock(12);
        productInfo.setExpirationDate(LocalDate.now().plusDays(30));

        Product existingProduct = Product.builder()
                .id(productId)
                .name("Laptop")
                .category(Category.builder().categoryName("Electronics").build())
                .unitPrice(1200.0f)
                .inStock(10)
                .createdAt(LocalDate.now().minusDays(10))
                .updatedAt(LocalDate.now())
                .build();

        Category updatedCategory = Category.builder().categoryName("Electronics").build();

        when(productRepository.findById(productId)).thenReturn(existingProduct);
        when(categoryService.getCategoryByName("Electronics")).thenReturn(updatedCategory);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // when
        Product updatedProduct = productService.updateProduct(productInfo);

        // then
        assertNotNull(updatedProduct);
        assertEquals(productId, updatedProduct.getId());
        assertEquals("Updated Laptop", updatedProduct.getName());
        assertEquals(1300.0f, updatedProduct.getUnitPrice());
        assertEquals(12, updatedProduct.getInStock());
        assertEquals("Electronics", updatedProduct.getCategory().getCategoryName());
        assertNotNull(updatedProduct.getUpdatedAt());

        verify(productRepository).findById(productId);
        verify(categoryService).getCategoryByName("Electronics");
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
        productInfo.setUnitPrice(1.0f);
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
        productInfo.setUnitPrice(100.0f);
        productInfo.setInStock(5);

        when(productRepository.findById(nonExistentId)).thenReturn(null);

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
        productInfo.setUnitPrice(100.0f);
        productInfo.setInStock(5);

        Product existingProduct = Product.builder()
                .id(productId)
                .name("Product")
                .category(Category.builder().categoryName("OldCategory").build())
                .unitPrice(100.0f)
                .inStock(5)
                .build();

        when(productRepository.findById(productId)).thenReturn(existingProduct);
        when(categoryService.getCategoryByName("NonExistentCategory")).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> productService.updateProduct(productInfo))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category does not exist: NonExistentCategory");
        verify(productRepository).findById(productId);
        verify(categoryService).getCategoryByName("NonExistentCategory");
        verify(productRepository, never()).save(any(Product.class));
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
        when(productRepository.findById(productId)).thenReturn(expectedProduct);

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
        when(productRepository.findById(nonExistentId)).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> productService.getProductById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with ID: " + nonExistentId);
        verify(productRepository).findById(nonExistentId);
    }

    // --- Tests for updateProductAvailability ---

    @Test
    public void givenProductIds_whenUpdateProductAvailability_thenStockIsToggled() {
        // given
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        List<String> productIds = Arrays.asList(id1, id2);

        Product product1 = Product.builder().id(id1).name("Product 1").inStock(10).build(); // In stock
        Product product2 = Product.builder().id(id2).name("Product 2").inStock(0).build();  // Out of stock

        when(productRepository.findById(id1)).thenReturn(product1);
        when(productRepository.findById(id2)).thenReturn(product2);

        // when
        productService.updateProductAvailability(productIds);

        // then
        verify(productRepository).updateAvailability(product1, true); // true because (10 > 0) is true
        verify(productRepository).updateAvailability(product2, false); // false because (0 > 0) is false
    }

    @Test
    public void givenNullProductIds_whenUpdateProductAvailability_thenThrowIllegalArgumentException() {
        // given
        List<String> productIds = null;

        // when
        // then
        assertThatThrownBy(() -> productService.updateProductAvailability(productIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product IDs list cannot be null or empty");
        verifyNoInteractions(productRepository);
    }

    @Test
    public void givenEmptyProductIds_whenUpdateProductAvailability_thenThrowIllegalArgumentException() {
        // given
        List<String> productIds = new ArrayList<>();

        // when
        // then
        assertThatThrownBy(() -> productService.updateProductAvailability(productIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product IDs list cannot be null or empty");
        verifyNoInteractions(productRepository);
    }

    @Test
    public void givenProductIdsWithNonExistingProduct_whenUpdateProductAvailability_thenNonExistingAreSkipped() {
        // given
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        List<String> productIds = Arrays.asList(id1, id2);

        Product product1 = Product.builder().id(id1).name("Product 1").inStock(10).build();

        when(productRepository.findById(id1)).thenReturn(product1);
        when(productRepository.findById(id2)).thenReturn(null);

        // when
        productService.updateProductAvailability(productIds);

        // then
        verify(productRepository).findById(id1);
        verify(productRepository).findById(id2);
        verify(productRepository).updateAvailability(product1, true);
        verifyNoMoreInteractions(productRepository);
    }

    // --- Tests for getInventoryReport ---

    @Test
    public void givenNoProducts_whenGetInventoryReport_thenReturnEmptyReport() {
        // given
        when(productRepository.getTotalProductsInStockByCategory()).thenReturn(Collections.emptyMap());
        when(productRepository.getTotalValueOfInventoryByCategory()).thenReturn(Collections.emptyMap());
        when(productRepository.getAveragePriceOfInStockProductsByCategory()).thenReturn(Collections.emptyMap());
        when(productRepository.getTotalProductsInStock()).thenReturn(0);
        when(productRepository.getTotalValueOfInventory()).thenReturn(0.0);
        when(productRepository.getAveragePriceOfInStockProducts()).thenReturn(0.0);

        // when
        InventoryMetricsReport report = productService.getInventoryReport();

        // then
        assertNotNull(report);
        assertTrue(report.getCategoryMetrics().isEmpty());
        assertNotNull(report.getOverallMetrics());
        assertEquals(0, report.getOverallMetrics().getTotalProductsInStock());
        assertEquals(0.0, report.getOverallMetrics().getTotalValueInStock());
        assertEquals(0.0, report.getOverallMetrics().getAveragePriceInStock());

        verify(productRepository).getTotalProductsInStockByCategory();
        verify(productRepository).getTotalValueOfInventoryByCategory();
        verify(productRepository).getAveragePriceOfInStockProductsByCategory();
        verify(productRepository).getTotalProductsInStock();
        verify(productRepository).getTotalValueOfInventory();
        verify(productRepository).getAveragePriceOfInStockProducts();
    }

    @Test
    public void givenSomeProducts_whenGetInventoryReport_thenReturnCorrectReport() {
        // given
        Map<String, Integer> productsInStockByCategory = new HashMap<>();
        productsInStockByCategory.put("Electronics", 15);
        productsInStockByCategory.put("Food", 20);

        Map<String, Double> totalValueOfInventoryByCategory = new HashMap<>();
        totalValueOfInventoryByCategory.put("Electronics", 1500.0);
        totalValueOfInventoryByCategory.put("Food", 100.0);

        Map<String, Double> averagePriceOfInStockProductsByCategory = new HashMap<>();
        averagePriceOfInStockProductsByCategory.put("Electronics", 100.0);
        averagePriceOfInStockProductsByCategory.put("Food", 5.0);

        when(productRepository.getTotalProductsInStockByCategory()).thenReturn(productsInStockByCategory);
        when(productRepository.getTotalValueOfInventoryByCategory()).thenReturn(totalValueOfInventoryByCategory);
        when(productRepository.getAveragePriceOfInStockProductsByCategory()).thenReturn(averagePriceOfInStockProductsByCategory);
        when(productRepository.getTotalProductsInStock()).thenReturn(35);
        when(productRepository.getTotalValueOfInventory()).thenReturn(1600.0);
        when(productRepository.getAveragePriceOfInStockProducts()).thenReturn(45.71); // Example average

        // when
        InventoryMetricsReport report = productService.getInventoryReport();

        // then
        assertNotNull(report);
        assertFalse(report.getCategoryMetrics().isEmpty());
        assertEquals(2, report.getCategoryMetrics().size());

        CategoryMetrics electronicsMetrics = report.getCategoryMetrics().get(0);
        assertEquals("Electronics", electronicsMetrics.getCategoryName());
        assertEquals(15, electronicsMetrics.getTotalProductsInStock());
        assertEquals(1500.0, electronicsMetrics.getTotalValueInStock());
        assertEquals(100.0, electronicsMetrics.getAveragePriceInStock());

        CategoryMetrics foodMetrics = report.getCategoryMetrics().get(1);
        assertEquals("Food", foodMetrics.getCategoryName());
        assertEquals(20, foodMetrics.getTotalProductsInStock());
        assertEquals(100.0, foodMetrics.getTotalValueInStock());
        assertEquals(5.0, foodMetrics.getAveragePriceInStock());

        assertNotNull(report.getOverallMetrics());
        assertEquals(35, report.getOverallMetrics().getTotalProductsInStock());
        assertEquals(1600.0, report.getOverallMetrics().getTotalValueInStock());
        assertEquals(45.71, report.getOverallMetrics().getAveragePriceInStock());

        verify(productRepository).getTotalProductsInStockByCategory();
        verify(productRepository).getTotalValueOfInventoryByCategory();
        verify(productRepository).getAveragePriceOfInStockProductsByCategory();
        verify(productRepository).getTotalProductsInStock();
        verify(productRepository).getTotalValueOfInventory();
        verify(productRepository).getAveragePriceOfInStockProducts();
    }
}