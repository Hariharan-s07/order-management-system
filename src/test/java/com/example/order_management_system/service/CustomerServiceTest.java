package com.example.order_management_system.service;

import com.example.order_management_system.dto.CustomerRequest;
import com.example.order_management_system.exception.DuplicateResourceException;
import com.example.order_management_system.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldFailWhenEmailAlreadyExists() {

        CustomerRequest request = new CustomerRequest();

        request.setName("John");
        request.setEmail("john@gmail.com");
        request.setPhone("9876543210");

        when(customerRepository.existsByEmail("john@gmail.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.createCustomer(request)
        );
    }
}