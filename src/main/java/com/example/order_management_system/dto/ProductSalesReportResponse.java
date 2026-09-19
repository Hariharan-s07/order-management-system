package com.example.order_management_system.dto;

import java.math.BigDecimal;

public class ProductSalesReportResponse {

    private Long productId;
    private String productName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;

    public ProductSalesReportResponse() {
    }

    public ProductSalesReportResponse(Long productId,
                                      String productName,
                                      Long totalQuantitySold,
                                      BigDecimal totalRevenue) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
        this.totalRevenue = totalRevenue;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}