package com.example.order_management_system.controller;

import com.example.order_management_system.dto.OrderResponse;
import com.example.order_management_system.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerOrderController {

    private final OrderService orderService;

    public CustomerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{customerId}/orders")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(
            @PathVariable Long customerId) {

        List<OrderResponse> response =
                orderService.getCustomerOrders(customerId);

        return ResponseEntity.ok(response);
    }
}