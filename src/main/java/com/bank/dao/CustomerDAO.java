package com.bank.dao;

import com.bank.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class CustomerDAO {
    public int create(Connection connection, String name, String email, String phone, String passwordHash)
            throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone, password, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Customer ID was not generated.");
    }

    public Optional<Customer> findByEmail(Connection connection, String email) throws SQLException {
        String sql = "SELECT customer_id, name, email, phone, password, status, created_at "
                + "FROM customers WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapCustomer(result)) : Optional.empty();
            }
        }
    }

    private Customer mapCustomer(ResultSet result) throws SQLException {
        Timestamp createdAt = result.getTimestamp("created_at");
        LocalDateTime created = createdAt == null ? null : createdAt.toLocalDateTime();
        return new Customer(result.getInt("customer_id"), result.getString("name"),
                result.getString("email"), result.getString("phone"), result.getString("password"),
                result.getString("status"), created);
    }
}