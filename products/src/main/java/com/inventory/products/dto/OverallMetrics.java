package com.inventory.products.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallMetrics {
    private int totalProductsInStock;
    private BigDecimal totalValueInStock;
    private BigDecimal averagePriceInStock;
}