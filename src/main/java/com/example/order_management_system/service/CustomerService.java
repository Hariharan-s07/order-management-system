package com.example.order_management_system.service;

import com.example.order_management_system.dto.CustomerRequest;
import com.example.order_management_system.dto.CustomerResponse;
import com.example.order_management_system.entity.Customer;
import com.example.order_management_system.exception.ResourceNotFoundException;
import com.example.order_management_system.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import com.example.order_management_system.exception.DuplicateResourceException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Customer already exists with email: " + request.getEmail());
        }

        Customer customer = new Customer();

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCreatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);

        return convertToResponse(savedCustomer);
    }

    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id));

        return convertToResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {

        List<Customer> customers = customerRepository.findAll();

        return customers.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + id));

        if (!customer.getEmail().equals(request.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {

            throw new RuntimeException("Customer already exists with email: "
                    + request.getEmail());
        }

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer updatedCustomer = customerRepository.save(customer);

        return convertToResponse(updatedCustomer);
    }

    private CustomerResponse convertToResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt()
        );
    }
}