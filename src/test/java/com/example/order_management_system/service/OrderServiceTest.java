package com.example.order_management_system.service;

import com.example.order_management_system.dto.OrderItemRequest;
import com.example.order_management_system.dto.OrderRequest;
import com.example.order_management_system.dto.OrderResponse;
import com.example.order_management_system.entity.*;
import com.example.order_management_system.exception.BusinessRuleException;
import com.example.order_management_system.exception.ResourceNotFoundException;
import com.example.order_management_system.repository.CustomerRepository;
import com.example.order_management_system.repository.OrderItemRepository;
import com.example.order_management_system.repository.OrderRepository;
import com.example.order_management_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;


    @Test
    void shouldCreateOrderSuccessfully() {

        // Customer
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setEmail("john@gmail.com");

        // Product
        Product product = new Product();
        product.setId(2L);
        product.setName("Gaming Mouse");
        product.setPrice(new BigDecimal("2500"));
        product.setAvailableQuantity(10);
        product.setActive(true);

        // Request
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(2L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerId(1L);
        request.setItems(List.of(itemRequest));

        // Mock database calls
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {

                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        // Call service
        OrderResponse response =
                orderService.createOrder(request);

        // Check result
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getCustomerId());

        assertEquals(
                new BigDecimal("5000"),
                response.getTotalAmount()
        );

        // Check inventory
        assertEquals(8, product.getAvailableQuantity());

        // Verify database calls
        verify(customerRepository).findById(1L);
        verify(productRepository).findById(2L);
        verify(orderRepository).save(any(Order.class));
    }


    @Test
    void shouldFailWhenInventoryIsInsufficient() {

        // Customer
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setEmail("john@gmail.com");

        // Product has only 2 items
        Product product = new Product();
        product.setId(2L);
        product.setName("Gaming Mouse");
        product.setPrice(new BigDecimal("2500"));
        product.setAvailableQuantity(2);
        product.setActive(true);

        // Request 5 items
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(2L);
        itemRequest.setQuantity(5);

        OrderRequest request = new OrderRequest();
        request.setCustomerId(1L);
        request.setItems(List.of(itemRequest));

        // Mock database
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product));

        // Expect business exception
        assertThrows(
                BusinessRuleException.class,
                () -> orderService.createOrder(request)
        );

        // Stock must remain unchanged
        assertEquals(2, product.getAvailableQuantity());

        // Order should never be saved
        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldFailWhenCustomerDoesNotExist() {

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(2L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setCustomerId(999L);
        request.setItems(List.of(itemRequest));

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(request)
        );

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(productRepository, never())
                .findById(anyLong());
    }


    @Test
    void shouldCancelOrderAndRestoreStock() {

        // Customer
        Customer customer = new Customer();
        customer.setId(1L);

        // Product
        Product product = new Product();
        product.setId(2L);
        product.setName("Gaming Mouse");
        product.setPrice(new BigDecimal("2500"));
        product.setAvailableQuantity(8);
        product.setActive(true);

        // Order
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("5000"));

        // Order item
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("2500"));
        item.setTotalPrice(new BigDecimal("5000"));

        order.setItems(List.of(item));

        // Mock database
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Important:
        // Mockito should return the same order after save
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        // Call service
        OrderResponse response =
                orderService.cancelOrder(1L);

        // Check status
        assertEquals(
                OrderStatus.CANCELLED,
                response.getStatus()
        );

        // Stock should be restored
        assertEquals(10, product.getAvailableQuantity());

        // Verify database calls
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldNotCancelCompletedOrder() {

        Customer customer = new Customer();
        customer.setId(1L);

        Product product = new Product();
        product.setId(2L);
        product.setName("Gaming Mouse");
        product.setPrice(new BigDecimal("2500"));
        product.setAvailableQuantity(8);
        product.setActive(true);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalAmount(new BigDecimal("5000"));

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("2500"));
        item.setTotalPrice(new BigDecimal("5000"));

        order.setItems(List.of(item));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                BusinessRuleException.class,
                () -> orderService.cancelOrder(1L)
        );

        // Stock should not change
        assertEquals(8, product.getAvailableQuantity());

        // Order should not be saved
        verify(orderRepository, never())
                .save(any(Order.class));

        // Product should not be saved
        verify(productRepository, never())
                .save(any(Product.class));
    }
}