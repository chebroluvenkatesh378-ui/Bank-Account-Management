package com.bank.dao;

import com.bank.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDAO {
    public int create(Connection connection, int customerId, String accountNumber, String accountType)
            throws SQLException {
        String sql = "INSERT INTO accounts (customer_id, account_number, account_type, balance, status) "
                + "VALUES (?, ?, ?, 0.00, 'ACTIVE')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, customerId);
            statement.setString(2, accountNumber);
            statement.setString(3, accountType);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Account ID was not generated.");
    }

    public Optional<Account> findByNumber(Connection connection, String accountNumber, boolean lock)
            throws SQLException {
        String sql = "SELECT account_id, customer_id, account_number, account_type, balance, status, created_at "
                + "FROM accounts WHERE account_number = ?" + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapAccount(result)) : Optional.empty();
            }
        }
    }

    public List<Account> findByCustomer(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT account_id, customer_id, account_number, account_type, balance, status, created_at "
                + "FROM accounts WHERE customer_id = ? ORDER BY created_at, account_id";
        List<Account> accounts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    accounts.add(mapAccount(result));
                }
            }
        }
        return accounts;
    }

    public boolean updateBalance(Connection connection, int accountId, BigDecimal balance) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = ? WHERE account_id = ? AND status = 'ACTIVE'")) {
            statement.setBigDecimal(1, balance);
            statement.setInt(2, accountId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean closeIfZeroBalance(Connection connection, int accountId) throws SQLException {
        String sql = "UPDATE accounts SET status = 'CLOSED' WHERE account_id = ? AND status = 'ACTIVE' AND balance = 0";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            return statement.executeUpdate() == 1;
        }
    }

    private Account mapAccount(ResultSet result) throws SQLException {
        Timestamp createdAt = result.getTimestamp("created_at");
        LocalDateTime created = createdAt == null ? null : createdAt.toLocalDateTime();
        return new Account(result.getInt("account_id"), result.getInt("customer_id"),
                result.getString("account_number"), result.getString("account_type"),
                result.getBigDecimal("balance"), result.getString("status"), created);
    }
}