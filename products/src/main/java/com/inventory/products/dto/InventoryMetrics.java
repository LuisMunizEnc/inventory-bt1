package com.inventory.products.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryMetrics {
    private int totalProductsInStock;
    private BigDecimal totalValueOfInventory;
    private BigDecimal averagePriceOfInStockProducts;
    private Map<String, Integer> productsInStockByCategory;
    private Map<String, BigDecimal> totalValueOfInventoryByCategory;
    private Map<String, BigDecimal> averagePriceOfInStockProductsByCategory;
}
