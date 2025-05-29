package com.inventory.products.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductInfo {
    private String id;
    private String name;
    private String categoryName;
    private BigDecimal unitPrice;
    private LocalDate expirationDate;
    private int inStock;
}
