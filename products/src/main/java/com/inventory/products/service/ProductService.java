package com.inventory.products.service;

import com.inventory.products.dto.InventoryMetricsReport;
import com.inventory.products.model.Product;
import com.inventory.products.dto.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductInfo productInfo);

    Product updateProduct(ProductInfo productInfo);

    Product getProductById(String productId);

    void deleteProductById(String productId);

    Page<Product> getProductsByCriteria(String name, List<String> categoryFilter, Boolean availability, Pageable pageable);

    void setProductInStock(String productId);

    void setProductOutOfStock(String productId);

    InventoryMetricsReport getInventoryReport();
}
