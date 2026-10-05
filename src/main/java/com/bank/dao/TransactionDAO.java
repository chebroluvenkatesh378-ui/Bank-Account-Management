package com.bank.dao;

import com.bank.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    public void create(Connection connection, int accountId, String type, BigDecimal amount,
                       String referenceAccount) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, reference_account) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            statement.setString(2, type);
            statement.setBigDecimal(3, amount);
            statement.setString(4, referenceAccount);
            statement.executeUpdate();
        }
    }

    public List<Transaction> findByAccount(Connection connection, int accountId, boolean newestFirst)
            throws SQLException {
        String direction = newestFirst ? "DESC" : "ASC";
        String sql = "SELECT transaction_id, transaction_type, amount, reference_account, transaction_date "
                + "FROM transactions WHERE account_id = ? ORDER BY transaction_date " + direction
                + ", transaction_id " + direction;
        List<Transaction> transactions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Timestamp timestamp = result.getTimestamp("transaction_date");
                    LocalDateTime date = timestamp == null ? null : timestamp.toLocalDateTime();
                    transactions.add(new Transaction(result.getLong("transaction_id"),
                            result.getString("transaction_type"), result.getBigDecimal("amount"),
                            result.getString("reference_account"), date));
                }
            }
        }
        return transactions;
    }
}