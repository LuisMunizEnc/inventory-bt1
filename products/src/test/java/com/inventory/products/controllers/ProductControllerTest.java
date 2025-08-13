package com.inventory.products.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.inventory.products.dto.*;
import com.inventory.products.exception.EntityAlreadyExistsException;
import com.inventory.products.exception.EntityNotFoundException;
import com.inventory.products.model.Category;
import com.inventory.products.model.Product;
import com.inventory.products.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private ObjectMapper objectMapper;

    private Product product1;
    private Product product2;
    private ProductInfo productInfo1;
    private Category categoryFood;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        categoryFood = Category.builder().categoryName("Food").build();
        Category categoryDrink = Category.builder().categoryName("Drink").build();

        product1 = Product.builder()
                .id("prod1")
                .name("Apple")
                .category(categoryFood)
                .unitPrice(new BigDecimal("1.20"))
                .expirationDate(LocalDate.of(2025, 12, 31))
                .inStock(100)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        product2 = Product.builder()
                .id("prod2")
                .name("Orange Juice")
                .category(categoryDrink)
                .unitPrice(new BigDecimal("2.50"))
                .expirationDate(LocalDate.of(2025, 10, 15))
                .inStock(50)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        productInfo1 = ProductInfo.builder()
                .id("prod1")
                .name("Apple")
                .categoryName("Food")
                .unitPrice(new BigDecimal("1.20"))
                .expirationDate(LocalDate.of(2025, 12, 31))
                .inStock(100)
                .build();
    }

    @Test
    void createProduct_Success() throws Exception {
        // given
        ProductInfo productInfoToCreate = productInfo1;
        when(productService.createProduct(any(ProductInfo.class))).thenReturn(product1);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productInfoToCreate)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Product responseProduct = objectMapper.readValue(response.getContentAsString(), Product.class);
        assertNotNull(responseProduct);
        assertEquals(product1.getId(), responseProduct.getId());
        assertEquals(product1.getName(), responseProduct.getName());
        assertEquals(product1.getCategory().getCategoryName(), responseProduct.getCategory().getCategoryName());
        assertEquals(product1.getUnitPrice(), responseProduct.getUnitPrice());
        assertEquals(product1.getExpirationDate(), responseProduct.getExpirationDate());
        assertEquals(product1.getInStock(), responseProduct.getInStock());

        verify(productService, times(1)).createProduct(any(ProductInfo.class));
    }

    @Test
    void createProduct_IllegalArgumentException() throws Exception {
        // given
        ProductInfo invalidProductInfo = ProductInfo.builder()
                .id("prodInvalid")
                .name("")
                .categoryName("Food")
                .unitPrice(new BigDecimal("1.00"))
                .expirationDate(LocalDate.of(2025, 1, 1))
                .inStock(10)
                .build();
        String errorMessage = "Product name cannot be null or empty";
        when(productService.createProduct(any(ProductInfo.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductInfo)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(productService, times(1)).createProduct(any(ProductInfo.class));
    }

    @Test
    void createProduct_EntityAlreadyExistsException() throws Exception {
        // given
        ProductInfo productInfoExisting = productInfo1;
        String errorMessage = "Product with name Apple already exists";
        when(productService.createProduct(any(ProductInfo.class)))
                .thenThrow(new EntityAlreadyExistsException(errorMessage));

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productInfoExisting)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(productService, times(1)).createProduct(any(ProductInfo.class));
    }

    @Test
    void updateProduct_Success() throws Exception {
        // given
        ProductInfo updatedProductInfo = ProductInfo.builder()
                .id("prod1")
                .name("Red Apple")
                .categoryName("Food")
                .unitPrice(new BigDecimal("1.50"))
                .expirationDate(LocalDate.of(2026, 1, 1))
                .inStock(120)
                .build();

        Product updatedProduct = Product.builder()
                .id("prod1")
                .name("Red Apple")
                .category(categoryFood)
                .unitPrice(new BigDecimal("1.50"))
                .expirationDate(LocalDate.of(2026, 1, 1))
                .inStock(120)
                .createdAt(product1.getCreatedAt())
                .updatedAt(LocalDate.now())
                .build();

        when(productService.updateProduct(any(ProductInfo.class))).thenReturn(updatedProduct);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}", "prod1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProductInfo)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Product responseProduct = objectMapper.readValue(response.getContentAsString(), Product.class);
        assertNotNull(responseProduct);
        assertEquals(updatedProduct.getId(), responseProduct.getId());
        assertEquals(updatedProduct.getName(), responseProduct.getName());
        assertEquals(updatedProduct.getUnitPrice(), responseProduct.getUnitPrice());

        verify(productService, times(1)).updateProduct(any(ProductInfo.class));
    }

    @Test
    void updateProduct_IllegalArgumentException() throws Exception {
        // given
        ProductInfo productInfoWithoutId = ProductInfo.builder()
                .name("Apple")
                .categoryName("Food")
                .unitPrice(new BigDecimal("1.20"))
                .expirationDate(LocalDate.of(2025, 12, 31))
                .inStock(100)
                .build();
        String errorMessage = "Product ID is required for updating";
        doThrow(new IllegalArgumentException(errorMessage))
                .when(productService).updateProduct(any(ProductInfo.class));

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}", "prod1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productInfoWithoutId)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(productService, times(1)).updateProduct(any(ProductInfo.class));
    }

    @Test
    void updateProduct_EntityNotFoundException() throws Exception {
        // given
        ProductInfo productInfoToUpdate = ProductInfo.builder()
                .id("nonExistentId")
                .name("Non Existent Product")
                .categoryName("Food")
                .unitPrice(new BigDecimal("1.00"))
                .inStock(10)
                .build();
        String errorMessage = "Product not found with ID: nonExistentId";
        doThrow(new EntityNotFoundException(errorMessage))
                .when(productService).updateProduct(any(ProductInfo.class));

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}", "nonExistentId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productInfoToUpdate)))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(productService, times(1)).updateProduct(any(ProductInfo.class));
    }

    @Test
    public void givenExistingProductId_whenDeleteProductById_thenReturnsNoContent() throws Exception {
        // given
        String productId = UUID.randomUUID().toString();
        doNothing().when(productService).deleteProductById(productId);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.delete("/products/" + productId))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(productService).deleteProductById(productId);
    }

    @Test
    public void givenNonExistingProductId_whenDeleteProductById_thenReturnsNotFound() throws Exception {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        String errorMessage = "Product not found with ID: " + nonExistentId + " for deletion";
        doThrow(new EntityNotFoundException(errorMessage)).when(productService).deleteProductById(nonExistentId);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.delete("/products/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());
        verify(productService).deleteProductById(nonExistentId);
    }

    @Test
    public void givenNullProductIdForDelete_whenDeleteProductById_thenReturnsBadRequest() throws Exception {
        // given
        String productId = " ";
        String errorMessage = "Product ID cannot be null or empty for deletion";
        doThrow(new IllegalArgumentException(errorMessage)).when(productService).deleteProductById(productId);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.delete("/products/" + productId))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());
        verify(productService).deleteProductById(productId);
    }

    @Test
    void getProductById_Success() throws Exception {
        // given
        String productId = "prod1";
        when(productService.getProductById(productId)).thenReturn(product1);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Product responseProduct = objectMapper.readValue(response.getContentAsString(), Product.class);
        assertNotNull(responseProduct);
        assertEquals(product1.getId(), responseProduct.getId());
        assertEquals(product1.getName(), responseProduct.getName());

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void getProductById_EntityNotFoundException() throws Exception {
        // given
        String productId = "nonExistentId";
        String errorMessage = "Product not found with ID: " + productId;
        when(productService.getProductById(productId))
                .thenThrow(new EntityNotFoundException(errorMessage));

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void getAllProducts_WithFilters_Success() throws Exception {
        // given
        List<Product> filteredProducts = Collections.singletonList(product1);
        String nameFilter = "Apple";
        List<String> categoryFilter = Collections.singletonList("Food");
        Boolean inStockFilter = true;

        Page<Product> productPage = new PageImpl<>(filteredProducts);
        when(productService.getProductsByCriteria(eq(nameFilter), eq(categoryFilter), eq(inStockFilter), any(Pageable.class)))
                .thenReturn(productPage);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .param("name", nameFilter)
                        .param("categories", categoryFilter.getFirst())
                        .param("inStock", inStockFilter.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        String json = response.getContentAsString();
        Map<String, Object> pagedResponse = objectMapper.readValue(json, new TypeReference<>() {});
        List<Product> content = (List<Product>) pagedResponse.get("content");
        assertNotNull(content);
        assertEquals(1, content.size());

        Map<?, ?> productMap = (Map<?, ?>) content.get(0);
        assertEquals(product1.getId(), productMap.get("id"));
        assertEquals(product1.getName(), productMap.get("name"));

        verify(productService, times(1)).getProductsByCriteria(eq(nameFilter), eq(categoryFilter), eq(inStockFilter), any(Pageable.class));
    }

    @Test
    void markProductOutOfStock_Success() throws Exception {
        // given
        String productId = "prod1";
        doNothing().when(productService).setProductOutOfStock(productId);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}/outofstock", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(productService, times(1)).setProductOutOfStock(productId);
    }

    @Test
    void markProductOutOfStock_EntityNotFoundException() throws Exception {
        // given
        String productId = "nonExistentId";
        String errorMessage = "Product not found with ID: " + productId;
        doThrow(new EntityNotFoundException(errorMessage))
                .when(productService).setProductOutOfStock(productId);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}/outofstock", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(productService, times(1)).setProductOutOfStock(productId);
    }

    @Test
    void markProductInStock_Success() throws Exception {
        // given
        String productId = "prod1";
        doNothing().when(productService).setProductInStock(productId);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}/instock", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
        verify(productService, times(1)).setProductInStock(productId);
    }

    @Test
    void markProductInStock_EntityNotFoundException() throws Exception {
        // given
        String productId = "nonExistentId";
        String errorMessage = "Product not found with ID: " + productId;
        doThrow(new EntityNotFoundException(errorMessage))
                .when(productService).setProductInStock(productId);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}/instock", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage, errorResponse.getMessage());

        verify(productService, times(1)).setProductInStock(productId);
    }

    @Test
    void getInventoryMetricsReport_Success() throws Exception {
        // given
        OverallMetrics overallMetrics = OverallMetrics.builder()
                .totalProductsInStock(150)
                .totalValueInStock(new BigDecimal("270.00"))
                .averagePriceInStock(new BigDecimal("1.80"))
                .build();

        CategoryMetrics foodMetrics = CategoryMetrics.builder()
                .categoryName("Food")
                .totalProductsInStock(100)
                .totalValueInStock(new BigDecimal("120.00"))
                .averagePriceInStock(new BigDecimal("1.20"))
                .build();

        CategoryMetrics drinkMetrics = CategoryMetrics.builder()
                .categoryName("Drink")
                .totalProductsInStock(50)
                .totalValueInStock(new BigDecimal("150.00"))
                .averagePriceInStock(new BigDecimal("3.00"))
                .build();

        InventoryMetricsReport report = InventoryMetricsReport.builder()
                .overallMetrics(overallMetrics)
                .categoryMetrics(Arrays.asList(drinkMetrics, foodMetrics))
                .build();

        when(productService.getInventoryReport()).thenReturn(report);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/products/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        InventoryMetricsReport responseReport = objectMapper.readValue(response.getContentAsString(), InventoryMetricsReport.class);
        assertNotNull(responseReport);
        assertNotNull(responseReport.getOverallMetrics());
        assertEquals(overallMetrics.getTotalProductsInStock(), responseReport.getOverallMetrics().getTotalProductsInStock());
        assertEquals(overallMetrics.getTotalValueInStock(), responseReport.getOverallMetrics().getTotalValueInStock());
        assertEquals(overallMetrics.getAveragePriceInStock(), responseReport.getOverallMetrics().getAveragePriceInStock());

        assertNotNull(responseReport.getCategoryMetrics());
        assertEquals(2, responseReport.getCategoryMetrics().size());
        assertEquals("Drink", responseReport.getCategoryMetrics().get(0).getCategoryName());
        assertEquals("Food", responseReport.getCategoryMetrics().get(1).getCategoryName());

        verify(productService, times(1)).getInventoryReport();
    }
}