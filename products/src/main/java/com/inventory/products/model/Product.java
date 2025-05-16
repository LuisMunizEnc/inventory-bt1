package com.inventory.products.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private int id;
    private String name;
    private Category category;
    private float unitPrice;
    private LocalDate expirationDate;
    private int inStock;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}

