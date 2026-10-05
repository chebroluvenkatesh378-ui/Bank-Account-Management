package com.bank.exception;

public class AccountNotFoundException extends BankException {
    public AccountNotFoundException() { super("Account number was not found."); }
}