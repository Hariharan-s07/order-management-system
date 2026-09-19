package com.example.order_management_system.service;

import com.example.order_management_system.repository.CustomerRepository;
import com.example.order_management_system.repository.OrderItemRepository;
import com.example.order_management_system.repository.OrderRepository;
import com.example.order_management_system.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.example.order_management_system.dto.OrderItemRequest;
import com.example.order_management_system.dto.OrderItemResponse;
import com.example.order_management_system.dto.OrderRequest;
import com.example.order_management_system.dto.OrderResponse;
import com.example.order_management_system.entity.Customer;
import com.example.order_management_system.entity.Order;
import com.example.order_management_system.entity.OrderItem;
import com.example.order_management_system.entity.OrderStatus;
import com.example.order_management_system.entity.Product;
import com.example.order_management_system.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import com.example.order_management_system.exception.BusinessRuleException;
import com.example.order_management_system.dto.CustomerReportResponse;
import com.example.order_management_system.dto.ProductSalesReportResponse;
import java.util.Set;
import java.util.HashSet;
import java.util.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + id));

        return convertToResponse(order);
    }
    public List<OrderResponse> getAllOrders() {

        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + request.getCustomerId()
                        ));

        // Store validated products and calculate totals later
        List<Product> products = new ArrayList<>();
        Set<Long> productIds = new HashSet<>();

        // 2. Validate ALL products and stock first
        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Product not found with id: "
                                            + itemRequest.getProductId()
                            ));

            // Check active
            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BusinessRuleException(
                        "Product is inactive: " + product.getName()
                );
            }

            // Check stock
            if (product.getAvailableQuantity() < itemRequest.getQuantity()) {
                throw new BusinessRuleException(
                        "Insufficient inventory for product: "
                                + product.getName()
                );
            }

            products.add(product);
        }

        // 3. Create order
        Order order = new Order();

        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 4. Now reduce inventory and create order items
        for (int i = 0; i < request.getItems().size(); i++) {

            OrderItemRequest itemRequest = request.getItems().get(i);
            Product product = products.get(i);

            // Reduce stock
            product.setAvailableQuantity(
                    product.getAvailableQuantity()
                            - itemRequest.getQuantity()
            );

            product.setUpdatedAt(LocalDateTime.now());

            productRepository.save(product);

            // Price snapshot
            BigDecimal unitPrice = product.getPrice();

            BigDecimal itemTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(itemRequest.getQuantity())
                    );

            // Create order item
            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(itemTotal);

            orderItems.add(orderItem);

            totalAmount = totalAmount.add(itemTotal);
        }

        // 5. Set order details
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 6. Save order
        Order savedOrder = orderRepository.save(order);

        return convertToResponse(savedOrder);
    }
    @Transactional
    public OrderResponse cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + id));

        // Cannot cancel an already cancelled order
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException(
                    "Order is already cancelled");
        }

        // Cannot cancel a completed order
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Completed order cannot be cancelled");
        }

        // Restore product quantities
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setAvailableQuantity(
                    product.getAvailableQuantity()
                            + item.getQuantity());

            productRepository.save(product);
        }

        // Change order status
        order.setStatus(OrderStatus.CANCELLED);

        Order cancelledOrder = orderRepository.save(order);

        return convertToResponse(cancelledOrder);
    }
    public List<OrderResponse> getCustomerOrders(Long customerId) {

        // First check whether customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId);
        }

        List<Order> orders =
                orderRepository.findByCustomer_Id(customerId);

        return orders.stream()
                .map(this::convertToResponse)
                .toList();
    }
    public CustomerReportResponse getCustomerReport(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId));

        List<Order> orders =
                orderRepository.findByCustomer_Id(customerId);

        BigDecimal totalAmount = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .count();

        return new CustomerReportResponse(
                customer.getId(),
                customer.getName(),
                totalOrders,
                totalAmount
        );
    }
    public List<ProductSalesReportResponse> getProductSalesReport() {

        List<Order> orders = orderRepository.findAll();

        Map<Long, ProductSalesReportResponse> salesMap = new HashMap<>();

        for (Order order : orders) {

            // Do not count cancelled orders
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }

            for (OrderItem item : order.getItems()) {

                Long productId = item.getProduct().getId();

                ProductSalesReportResponse existing =
                        salesMap.get(productId);

                if (existing == null) {

                    salesMap.put(
                            productId,
                            new ProductSalesReportResponse(
                                    productId,
                                    item.getProduct().getName(),
                                    item.getQuantity().longValue(),
                                    item.getTotalPrice()
                            )
                    );

                } else {

                    Long newQuantity =
                            existing.getTotalQuantitySold()
                                    + item.getQuantity();

                    BigDecimal newRevenue =
                            existing.getTotalRevenue()
                                    .add(item.getTotalPrice());

                    salesMap.put(
                            productId,
                            new ProductSalesReportResponse(
                                    productId,
                                    existing.getProductName(),
                                    newQuantity,
                                    newRevenue
                            )
                    );
                }
            }
        }

        return salesMap.values()
                .stream()
                .sorted((a, b) ->
                        Long.compare(
                                b.getTotalQuantitySold(),
                                a.getTotalQuantitySold()
                        ))
                .toList();
    }
    public List<ProductSalesReportResponse> getTopProducts(int limit) {

        if (limit <= 0) {
            throw new BusinessRuleException(
                    "Limit must be greater than 0");
        }

        return getProductSalesReport()
                .stream()
                .limit(limit)
                .toList();
    }
    private OrderResponse convertToResponse(Order order) {

        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                itemResponses
        );
    }
}