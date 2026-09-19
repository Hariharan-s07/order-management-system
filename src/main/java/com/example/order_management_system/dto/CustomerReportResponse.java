package com.example.order_management_system.dto;

import java.math.BigDecimal;

public class CustomerReportResponse {

    private Long customerId;
    private String customerName;
    private Long totalOrders;
    private BigDecimal totalAmount;

    public CustomerReportResponse() {
    }

    public CustomerReportResponse(Long customerId,
                                  String customerName,
                                  Long totalOrders,
                                  BigDecimal totalAmount) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalOrders = totalOrders;
        this.totalAmount = totalAmount;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}