package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.util.DBConnection;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public Account createAccount(int customerId, String accountType) throws BankException {
        String normalizedType = accountType == null ? "" : accountType.trim().toUpperCase();
        if (!normalizedType.equals("SAVINGS") && !normalizedType.equals("CURRENT")) {
            throw new BankException("Choose SAVINGS or CURRENT as the account type.");
        }
        try (Connection connection = DBConnection.getConnection()) {
            for (int attempt = 0; attempt < 5; attempt++) {
                try {
                    String accountNumber = generateAccountNumber();
                    int accountId = accountDAO.create(connection, customerId, accountNumber, normalizedType);
                    return accountDAO.findByNumber(connection, accountNumber, false)
                            .orElseThrow(() -> new SQLException("Created account could not be retrieved."));
                } catch (SQLException exception) {
                    if ("23000".equals(exception.getSQLState()) && attempt < 4) {
                        continue;
                    }
                    throw exception;
                }
            }
            throw new SQLException("Could not generate a unique account number.");
        } catch (SQLException exception) {
            throw new BankException("Could not create account. Check the database connection and try again.", exception);
        }
    }

    public List<Account> getCustomerAccounts(int customerId) throws BankException {
        try (Connection connection = DBConnection.getConnection()) {
            return accountDAO.findByCustomer(connection, customerId);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    public Account getAccount(int customerId, String accountNumber) throws BankException {
        try (Connection connection = DBConnection.getConnection()) {
            Account account = accountDAO.findByNumber(connection, accountNumber == null ? "" : accountNumber.trim(), false)
                    .orElseThrow(AccountNotFoundException::new);
            verifyOwnership(account, customerId);
            return account;
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    public void closeAccount(int customerId, String accountNumber) throws BankException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Account account = accountDAO.findByNumber(connection, accountNumber.trim(), true)
                        .orElseThrow(AccountNotFoundException::new);
                verifyOwnership(account, customerId);
                requireActive(account);
                if (account.getBalance().signum() > 0) {
                    throw new BankException("Account cannot be closed while its balance is greater than zero.");
                }
                if (!accountDAO.closeIfZeroBalance(connection, account.getAccountId())) {
                    throw new BankException("Account could not be closed. Check its status and balance.");
                }
                connection.commit();
            } catch (BankException | SQLException exception) {
                rollback(connection);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    public static void verifyOwnership(Account account, int customerId) throws AccountNotFoundException {
        if (account.getCustomerId() != customerId) {
            throw new AccountNotFoundException();
        }
    }

    public static void requireActive(Account account) throws BankException {
        if ("CLOSED".equals(account.getStatus())) {
            throw new BankException("This account is closed.");
        }
        if ("BLOCKED".equals(account.getStatus())) {
            throw new BankException("This account is blocked. Contact the bank.");
        }
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new BankException("This account is not active.");
        }
    }

    private String generateAccountNumber() {
        return "10" + String.format("%010d", RANDOM.nextLong(1_000_000_000L));
    }

    private BankException databaseError(SQLException exception) {
        return new BankException("Could not access account data. Check the database connection and try again.", exception);
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // The original operation failure is more useful to the caller.
        }
    }
}