package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private final int accountId;
    private final int customerId;
    private final String accountNumber;
    private final String accountType;
    private final BigDecimal balance;
    private final String status;
    private final LocalDateTime createdAt;

    public Account(int accountId, int customerId, String accountNumber, String accountType,
                   BigDecimal balance, String status, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getAccountId() { return accountId; }
    public int getCustomerId() { return customerId; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public BigDecimal getBalance() { return balance; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}