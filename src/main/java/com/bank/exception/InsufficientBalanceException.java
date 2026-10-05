package com.bank.exception;

public class InsufficientBalanceException extends BankException {
    public InsufficientBalanceException() { super("Insufficient balance for this transaction."); }
}