package com.inventory.products.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategoryMetrics {
    private String categoryName;
    private int totalProductsInStock;
    private BigDecimal totalValueInStock;
    private BigDecimal averagePriceInStock;
}