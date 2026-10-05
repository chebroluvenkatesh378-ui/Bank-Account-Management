CREATE DATABASE IF NOT EXISTS bank_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE bank_management;

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(24) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    UNIQUE KEY uq_customers_email (email),
    KEY idx_customers_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS accounts (
    account_id INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    account_number CHAR(12) NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'BLOCKED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id),
    UNIQUE KEY uq_accounts_account_number (account_number),
    KEY idx_accounts_customer_id (customer_id),
    KEY idx_accounts_status (status),
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id)
        REFERENCES customers (customer_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_accounts_balance CHECK (balance >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT NOT NULL AUTO_INCREMENT,
    account_id INT NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_OUT', 'TRANSFER_IN') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    reference_account VARCHAR(32) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (transaction_id),
    KEY idx_transactions_account_date (account_id, transaction_date, transaction_id),
    KEY idx_transactions_date (transaction_date),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id)
        REFERENCES accounts (account_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_transactions_amount CHECK (amount > 0)
) ENGINE=InnoDB;

INSERT INTO customers (customer_id, name, email, phone, password, status)
VALUES
    (1, 'Alex Morgan', 'alex@example.com', '+1-555-0101',
     '120000:00112233445566778899aabbccddeeff:8a53873d97b19a0152a202c49e3fdfcf5fd7aafd3617d489c5ecef0b6067d6cf', 'ACTIVE'),
    (2, 'Jordan Lee', 'jordan@example.com', '+1-555-0102',
     '120000:00112233445566778899aabbccddeeff:8a53873d97b19a0152a202c49e3fdfcf5fd7aafd3617d489c5ecef0b6067d6cf', 'ACTIVE');

INSERT INTO accounts (account_id, customer_id, account_number, account_type, balance, status)
VALUES
    (1, 1, '100000000001', 'SAVINGS', 1500.00, 'ACTIVE'),
    (2, 2, '100000000002', 'CURRENT', 750.00, 'ACTIVE');

INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, reference_account)
VALUES
    (1, 1, 'DEPOSIT', 1500.00, 'CASH'),
    (2, 2, 'DEPOSIT', 750.00, 'CASH');