package com.example.order_management_system.controller;

import com.example.order_management_system.dto.CustomerReportResponse;
import com.example.order_management_system.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.order_management_system.dto.ProductSalesReportResponse;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final OrderService orderService;

    public ReportController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerReportResponse> getCustomerReport(
            @PathVariable Long customerId) {

        CustomerReportResponse response =
                orderService.getCustomerReport(customerId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/products/sales")
    public ResponseEntity<List<ProductSalesReportResponse>> getProductSalesReport() {

        List<ProductSalesReportResponse> response =
                orderService.getProductSalesReport();

        return ResponseEntity.ok(response);
    }
    @GetMapping("/products/top")
    public ResponseEntity<List<ProductSalesReportResponse>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {

        List<ProductSalesReportResponse> response =
                orderService.getTopProducts(limit);

        return ResponseEntity.ok(response);
    }
}