package com.bank.model;

import java.time.LocalDateTime;

public class Customer {
    private final int customerId;
    private final String name;
    private final String email;
    private final String phone;
    private final String passwordHash;
    private final String status;
    private final LocalDateTime createdAt;

    public Customer(int customerId, String name, String email, String phone, String passwordHash,
                    String status, LocalDateTime createdAt) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}