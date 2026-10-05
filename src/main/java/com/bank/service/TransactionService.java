package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.BankException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.util.DBConnection;
import com.bank.util.InputValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransactionService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public TransactionService(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    public void deposit(int customerId, String accountNumber, BigDecimal amount) throws BankException {
        validateAmount(amount);
        inTransaction(connection -> {
            Account account = findLocked(accountNumber, customerId, connection);
            accountDAO.updateBalance(connection, account.getAccountId(), account.getBalance().add(amount));
            transactionDAO.create(connection, account.getAccountId(), "DEPOSIT", amount, "CASH");
        });
    }

    public void withdraw(int customerId, String accountNumber, BigDecimal amount) throws BankException {
        validateAmount(amount);
        inTransaction(connection -> {
            Account account = findLocked(accountNumber, customerId, connection);
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException();
            }
            accountDAO.updateBalance(connection, account.getAccountId(), account.getBalance().subtract(amount));
            transactionDAO.create(connection, account.getAccountId(), "WITHDRAWAL", amount, "CASH");
        });
    }

    public void transfer(int customerId, String fromNumber, String toNumber, BigDecimal amount) throws BankException {
        validateAmount(amount);
        String from = fromNumber == null ? "" : fromNumber.trim();
        String to = toNumber == null ? "" : toNumber.trim();
        if (from.equals(to)) {
            throw new BankException("Source and destination accounts must be different.");
        }
        inTransaction(connection -> {
            List<String> lockOrder = new ArrayList<>(List.of(from, to));
            lockOrder.sort(Comparator.naturalOrder());
            Account first = accountDAO.findByNumber(connection, lockOrder.get(0), true)
                    .orElseThrow(AccountNotFoundException::new);
            Account second = accountDAO.findByNumber(connection, lockOrder.get(1), true)
                    .orElseThrow(AccountNotFoundException::new);
            Account source = from.equals(first.getAccountNumber()) ? first : second;
            Account destination = to.equals(first.getAccountNumber()) ? first : second;
            AccountService.verifyOwnership(source, customerId);
            AccountService.requireActive(source);
            AccountService.requireActive(destination);
            if (source.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException();
            }
            if (!accountDAO.updateBalance(connection, source.getAccountId(), source.getBalance().subtract(amount))
                    || !accountDAO.updateBalance(connection, destination.getAccountId(), destination.getBalance().add(amount))) {
                throw new BankException("Transfer could not be completed because an account is not active.");
            }
            transactionDAO.create(connection, source.getAccountId(), "TRANSFER_OUT", amount, to);
            transactionDAO.create(connection, destination.getAccountId(), "TRANSFER_IN", amount, from);
        });
    }

    public List<Transaction> getHistory(int customerId, String accountNumber, boolean newestFirst)
            throws BankException {
        try (Connection connection = DBConnection.getConnection()) {
            Account account = accountDAO.findByNumber(connection, accountNumber.trim(), false)
                    .orElseThrow(AccountNotFoundException::new);
            AccountService.verifyOwnership(account, customerId);
            return transactionDAO.findByAccount(connection, account.getAccountId(), newestFirst);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    private Account findLocked(String accountNumber, int customerId, Connection connection)
            throws SQLException, BankException {
        Account account = accountDAO.findByNumber(connection, accountNumber == null ? "" : accountNumber.trim(), true)
                .orElseThrow(AccountNotFoundException::new);
        AccountService.verifyOwnership(account, customerId);
        AccountService.requireActive(account);
        return account;
    }

    private void validateAmount(BigDecimal amount) throws BankException {
        if (!InputValidator.isPositiveAmount(amount)) {
            throw new BankException("Amount must be greater than zero and have no more than two decimal places.");
        }
    }

    private void inTransaction(SqlWork work) throws BankException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                work.execute(connection);
                connection.commit();
            } catch (BankException | SQLException exception) {
                rollback(connection);
                if (exception instanceof BankException bankException) {
                    throw bankException;
                }
                throw (SQLException) exception;
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the transaction failure for the caller.
        }
    }

    private BankException databaseError(SQLException exception) {
        return new BankException("Could not complete the transaction. Check the database connection and try again.", exception);
    }

    @FunctionalInterface
    private interface SqlWork {
        void execute(Connection connection) throws SQLException, BankException;
    }
}