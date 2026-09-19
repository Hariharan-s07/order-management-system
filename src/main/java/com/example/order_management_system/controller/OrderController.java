package com.example.order_management_system.controller;

import com.example.order_management_system.dto.OrderRequest;
import com.example.order_management_system.dto.OrderResponse;
import com.example.order_management_system.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request) {

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id) {

        OrderResponse response = orderService.getOrderById(id);

        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        List<OrderResponse> response = orderService.getAllOrders();

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id) {

        OrderResponse response = orderService.cancelOrder(id);

        return ResponseEntity.ok(response);
    }

}