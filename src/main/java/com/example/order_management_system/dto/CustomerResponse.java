package com.example.order_management_system.dto;

import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String name, String email,
                            String phone, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}