package com.inventory.products.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    private String id;
    private String name;

    @ManyToOne()
    @JoinColumn(name = "category_name")
    private Category category;

    private BigDecimal unitPrice;
    private LocalDate expirationDate;
    private int inStock;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
