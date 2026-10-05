package com.bank;

import com.bank.dao.AccountDAO;
import com.bank.dao.CustomerDAO;
import com.bank.dao.TransactionDAO;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.service.AccountService;
import com.bank.service.CustomerService;
import com.bank.service.TransactionService;

import java.io.Console;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Console console = System.console();
    private final CustomerService customerService = new CustomerService(new CustomerDAO());
    private final AccountService accountService = new AccountService(new AccountDAO());
    private final TransactionService transactionService =
            new TransactionService(new AccountDAO(), new TransactionDAO());

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        if (console == null) {
            System.out.println("Secure console input is unavailable. Run this application in a terminal.");
            return;
        }
        System.out.println("====================================");
        System.out.println("BANK MANAGEMENT SYSTEM");
        while (true) {
            System.out.println("\n1. Customer Registration\n2. Customer Login\n3. Exit");
            switch (readChoice("Choose an option: ")) {
                case 1 -> registerCustomer();
                case 2 -> loginCustomer();
                case 3 -> {
                    System.out.println("Goodbye.");
                    return;
                }
                default -> System.out.println("Invalid menu option. Choose 1, 2, or 3.");
            }
        }
    }

    private void registerCustomer() {
        try {
            String name = readLine("Name: ");
            String email = readLine("Email: ");
            String phone = readLine("Phone number: ");
            String password = readPassword("Password (minimum 8 characters): ");
            int customerId = customerService.register(name, email, phone, password);
            System.out.println("Registration successful. Your customer ID is " + customerId + ".");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void loginCustomer() {
        try {
            String email = readLine("Email: ");
            String password = readPassword("Password: ");
            Customer customer = customerService.login(email, password);
            System.out.println("Welcome, " + customer.getName() + ".");
            customerMenu(customer);
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void customerMenu(Customer customer) {
        while (true) {
            System.out.println("\n====================================");
            System.out.println("CUSTOMER MENU");
            System.out.println("1. Create Bank Account\n2. View Account Details\n3. Deposit Money"
                    + "\n4. Withdraw Money\n5. Transfer Money\n6. Check Balance"
                    + "\n7. Transaction History\n8. Close Account\n9. Logout");
            switch (readChoice("Choose an option: ")) {
                case 1 -> createAccount(customer);
                case 2 -> viewAccount(customer, false);
                case 3 -> deposit(customer);
                case 4 -> withdraw(customer);
                case 5 -> transfer(customer);
                case 6 -> viewAccount(customer, true);
                case 7 -> showHistory(customer);
                case 8 -> closeAccount(customer);
                case 9 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid menu option. Choose a number from 1 to 9.");
            }
        }
    }

    private void createAccount(Customer customer) {
        try {
            System.out.println("Account type: 1. Savings  2. Current");
            int typeChoice = readChoice("Choose account type: ");
            String type = switch (typeChoice) {
                case 1 -> "SAVINGS";
                case 2 -> "CURRENT";
                default -> throw new BankException("Invalid account type.");
            };
            Account account = accountService.createAccount(customer.getCustomerId(), type);
            System.out.println("Account created: " + account.getAccountNumber() + " (" + account.getAccountType() + ").");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void viewAccount(Customer customer, boolean balanceOnly) {
        try {
            String accountNumber = readLine("Account number: ");
            Account account = accountService.getAccount(customer.getCustomerId(), accountNumber);
            if (balanceOnly) {
                System.out.printf("Account: %s | Status: %s | Balance: %s%n", account.getAccountNumber(),
                        account.getStatus(), account.getBalance().toPlainString());
            } else {
                System.out.printf("Account: %s | Type: %s | Status: %s | Balance: %s | Created: %s%n",
                        account.getAccountNumber(), account.getAccountType(), account.getStatus(),
                        account.getBalance().toPlainString(), formatDate(account.getCreatedAt()));
            }
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void deposit(Customer customer) {
        try {
            String accountNumber = readLine("Account number: ");
            BigDecimal amount = readAmount("Deposit amount: ");
            transactionService.deposit(customer.getCustomerId(), accountNumber, amount);
            System.out.println("Deposit completed.");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void withdraw(Customer customer) {
        try {
            String accountNumber = readLine("Account number: ");
            BigDecimal amount = readAmount("Withdrawal amount: ");
            transactionService.withdraw(customer.getCustomerId(), accountNumber, amount);
            System.out.println("Withdrawal completed.");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void transfer(Customer customer) {
        try {
            String fromAccount = readLine("From account number: ");
            String toAccount = readLine("To account number: ");
            BigDecimal amount = readAmount("Transfer amount: ");
            transactionService.transfer(customer.getCustomerId(), fromAccount, toAccount, amount);
            System.out.println("Transfer completed.");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void showHistory(Customer customer) {
        try {
            String accountNumber = readLine("Account number: ");
            System.out.println("1. Oldest first  2. Newest first");
            int order = readChoice("Sort order: ");
            if (order != 1 && order != 2) {
                System.out.println("Invalid sort option.");
                return;
            }
            List<Transaction> transactions = transactionService.getHistory(customer.getCustomerId(), accountNumber,
                    order == 2);
            if (transactions.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }
            System.out.printf("%-12s %-15s %-12s %-22s %s%n", "ID", "TYPE", "AMOUNT", "DATE/TIME", "REFERENCE");
            for (Transaction transaction : transactions) {
                System.out.printf("%-12d %-15s %-12s %-22s %s%n", transaction.getTransactionId(),
                        transaction.getTransactionType(), transaction.getAmount().toPlainString(),
                        formatDate(transaction.getTransactionDate()), transaction.getReferenceAccount());
            }
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void closeAccount(Customer customer) {
        try {
            String accountNumber = readLine("Account number to close: ");
            accountService.closeAccount(customer.getCustomerId(), accountNumber);
            System.out.println("Account closed. The account record has been retained.");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private int readChoice(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt));
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private BigDecimal readAmount(String prompt) throws BankException {
        try {
            return new BigDecimal(readLine(prompt));
        } catch (NumberFormatException exception) {
            throw new BankException("Enter a valid amount.");
        }
    }

    private String readLine(String prompt) {
        String value = console.readLine("%s", prompt);
        return value == null ? "" : value.trim();
    }

    private String readPassword(String prompt) throws BankException {
        char[] password = console.readPassword("%s", prompt);
        if (password == null) {
            throw new BankException("Password input was cancelled.");
        }
        String value = new String(password);
        java.util.Arrays.fill(password, '\0');
        return value;
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_FORMAT);
    }

    private void showError(BankException exception) {
        System.out.println("Error: " + exception.getMessage());
    }
}