package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final long transactionId;
    private final String transactionType;
    private final BigDecimal amount;
    private final String referenceAccount;
    private final LocalDateTime transactionDate;

    public Transaction(long transactionId, String transactionType, BigDecimal amount,
                       String referenceAccount, LocalDateTime transactionDate) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.referenceAccount = referenceAccount;
        this.transactionDate = transactionDate;
    }

    public long getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public BigDecimal getAmount() { return amount; }
    public String getReferenceAccount() { return referenceAccount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
}