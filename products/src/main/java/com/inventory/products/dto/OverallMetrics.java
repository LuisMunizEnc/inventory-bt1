package com.inventory.products.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverallMetrics {
    private int totalProductsInStock;
    private double totalValueInStock;
    private double averagePriceInStock;
}