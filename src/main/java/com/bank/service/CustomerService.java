package com.bank.service;

import com.bank.dao.CustomerDAO;
import com.bank.exception.BankException;
import com.bank.model.Customer;
import com.bank.util.DBConnection;
import com.bank.util.InputValidator;
import com.bank.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public int register(String name, String email, String phone, String password) throws BankException {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (name == null || name.isBlank()) {
            throw new BankException("Name is required.");
        }
        if (!InputValidator.isValidEmail(normalizedEmail)) {
            throw new BankException("Enter a valid email address.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new BankException("Enter a valid phone number (8 to 15 digits).");
        }
        if (!InputValidator.isValidPassword(password)) {
            throw new BankException("Password must contain at least 8 characters.");
        }
        try (Connection connection = DBConnection.getConnection()) {
            return customerDAO.create(connection, name.trim(), normalizedEmail, phone.trim(), PasswordUtil.hash(password));
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new BankException("An account with that email already exists.");
            }
            throw new BankException("Could not register customer. Check the database connection and try again.", exception);
        }
    }

    public Customer login(String email, String password) throws BankException {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (password == null || password.isEmpty()) {
            throw new BankException("Invalid email or password.");
        }
        try (Connection connection = DBConnection.getConnection()) {
            Customer customer = customerDAO.findByEmail(connection, normalizedEmail).orElse(null);
            if (customer == null || !"ACTIVE".equals(customer.getStatus())
                    || !PasswordUtil.verify(password, customer.getPasswordHash())) {
                throw new BankException("Invalid email or password, or the customer is not active.");
            }
            return customer;
        } catch (SQLException exception) {
            throw new BankException("Could not log in. Check the database connection and try again.", exception);
        }
    }
}