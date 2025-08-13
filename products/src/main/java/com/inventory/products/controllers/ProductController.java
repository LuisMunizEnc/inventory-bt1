package com.inventory.products.controllers;

import com.inventory.products.dto.InventoryMetricsReport;
import com.inventory.products.dto.ProductInfo;
import com.inventory.products.model.Product;
import com.inventory.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductInfo productInfo) {
        log.info("Received request to create product: {}", productInfo);
        Product createdProduct = productService.createProduct(productInfo);
        log.info("Product created successfully: {}", createdProduct);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody ProductInfo productInfo) {
        log.info("Received request to update product with ID: {} and info: {}", id, productInfo);
        productInfo.setId(id);
        Product updatedProduct = productService.updateProduct(productInfo);
        log.info("Product updated successfully: {}", updatedProduct);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        log.info("Received request to delete product with ID: {}", id);
        productService.deleteProductById(id);
        log.info("Product with ID: {} deleted successfully", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        log.info("Received request to get product by ID: {}", id);
        Product product = productService.getProductById(id);
        log.info("Returning product: {}", product);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        log.info("Received request to get products with filters - name: {}, categories: {}, inStock: {}, page: {}, sort: {}",
                name, categories, inStock, pageable.getPageNumber(), pageable.getSort());

        Page<Product> products = productService.getProductsByCriteria(name, categories, inStock, pageable);
        log.info("Returning {} products", products.getTotalElements());
        return new ResponseEntity<>(products, HttpStatus.OK);

    }

    @PutMapping("/{id}/outofstock")
    public ResponseEntity<Void> markProductOutOfStock(@PathVariable String id) {
        log.info("Received request to mark product with ID: {} as out of stock", id);
        productService.setProductOutOfStock(id);
        log.info("Product with ID: {} marked as out of stock successfully", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/instock")
    public ResponseEntity<Void> markProductInStock(@PathVariable String id) {
        log.info("Received request to mark product with ID: {} as in stock", id);
        productService.setProductInStock(id);
        log.info("Product with ID: {} marked as in stock successfully", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/metrics")
    public ResponseEntity<InventoryMetricsReport> getInventoryMetricsReport() {
        log.info("Received request to get inventory metrics report");
        InventoryMetricsReport report = productService.getInventoryReport();
        log.info("Returning inventory metrics report");
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}
