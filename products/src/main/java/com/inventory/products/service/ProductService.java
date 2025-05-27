package com.inventory.products.service;

import com.inventory.products.dto.InventoryMetricsReport;
import com.inventory.products.model.Product;
import com.inventory.products.dto.ProductInfo;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductInfo productInfo);

    Product updateProduct(ProductInfo productInfo);

    Product getProductById(String productId);

    List<Product> getAllProducts();

    List<Product> getProductsByCriteria(String name, List<String> categoryFilter, Boolean availability);

    void setProductInStock(String productId);

    void setProductOutOfStock(String productId);

    InventoryMetricsReport getInventoryReport();
}
