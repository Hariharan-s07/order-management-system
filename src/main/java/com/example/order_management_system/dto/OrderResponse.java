package com.example.order_management_system.dto;

import com.example.order_management_system.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long customerId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemResponse> items;

    public OrderResponse() {
    }

    public OrderResponse(Long id,
                         Long customerId,
                         LocalDateTime orderDate,
                         BigDecimal totalAmount,
                         OrderStatus status,
                         List<OrderItemResponse> items) {
        this.id = id;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}