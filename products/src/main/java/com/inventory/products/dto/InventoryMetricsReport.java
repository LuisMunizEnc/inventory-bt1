package com.inventory.products.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class InventoryMetricsReport {
    private List<CategoryMetrics> categoryMetrics;
    private OverallMetrics overallMetrics;
}