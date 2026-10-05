# Bank Account Management System

A beginner-friendly desktop banking application built with Java 17 Swing, Maven, MySQL, and JDBC. The application uses a layered structure so the graphical interface, business rules, SQL access, and database configuration stay separate. A console entry point is also available through `com.bank.Main`.

## Features

- Customer registration with generated IDs, email uniqueness, email/phone validation, and salted PBKDF2 password hashes.
- Customer login for active customers using a desktop sign-in screen. Passwords are masked while being entered.
- Savings and current accounts with generated unique account numbers and `ACTIVE`, `BLOCKED`, or `CLOSED` status.
- Account detail and balance views, deposits, withdrawals, account closure, and date-sortable transaction history.
- Desktop dashboard with searchable accounts, transaction activity, and dialogs for deposits, withdrawals, transfers, and account closure.
- Fund transfers update both accounts and add both transaction records in one database transaction. Account rows are locked in a stable order while transferring.
- Prepared statements, foreign keys, unique constraints, indexes, and user-friendly error messages.

## Technologies

- Java 17
- Apache Maven
- MySQL 8.0+
- JDBC with MySQL Connector/J
- Visual Studio Code with Extension Pack for Java

## Project Architecture

```text
src/main/java/com/bank/
	BankApp.java  (Swing desktop launcher)
	Main.java     (optional console launcher)
	ui/           BankFrame.java
	model/       Customer.java, Account.java, Transaction.java
	dao/         CustomerDAO.java, AccountDAO.java, TransactionDAO.java
	service/     CustomerService.java, AccountService.java, TransactionService.java
	util/        DBConnection.java, InputValidator.java, PasswordUtil.java
	exception/   BankException.java, InsufficientBalanceException.java,
							 AccountNotFoundException.java
database.sql
pom.xml
```

`BankApp` launches the Swing interface and `BankFrame` handles the desktop interaction. `Main` remains available as the optional console interface. Services enforce business rules, DAOs execute prepared SQL statements, models represent database records, and `DBConnection` centralizes connection configuration.

## Database Setup

1. Install MySQL Server 8.0 or newer and start the MySQL service.
2. Open a MySQL client or MySQL Workbench and execute `database.sql`:

```sql
SOURCE /absolute/path/to/Bank-Account-Management/database.sql;
```

The script creates and selects `bank_management`, creates all tables and indexes, then inserts two demo customers, two accounts, and two transactions. Run it against a fresh database; the sample inserts are intended for initial setup.

## Configure Database Credentials

`DBConnection` reads these environment variables and does not embed a password in application source:

- `BANK_DB_URL` (default: `jdbc:mysql://localhost:3306/bank_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
- `BANK_DB_USER` (default: `root`)
- `BANK_DB_PASSWORD` (default: empty)

For the current PowerShell session, set your local MySQL values before launching:

```powershell
$env:BANK_DB_URL = "jdbc:mysql://localhost:3306/bank_management?serverTimezone=UTC"
$env:BANK_DB_USER = "root"
$env:BANK_DB_PASSWORD = "your-local-mysql-password"
```

Do not commit credentials or place real passwords in source control. The default URL is suitable for local development; adjust it for your MySQL host, port, TLS, or authentication policy.

## VS Code Setup

1. Install JDK 17 and Apache Maven, and make sure `java` and `mvn` are available in the VS Code integrated terminal.
2. Install the recommended Java Extension Pack. Reopen this repository as a folder.
3. Execute `database.sql` and set the database environment variables in the VS Code terminal.
4. Use the **Bank Management System** launch configuration or run the Maven command below. This starts the desktop banking window.

## Maven Commands

Build and compile:

```powershell
mvn clean package
```

Run the desktop app:

```powershell
mvn exec:java
```

To run the optional console version instead:

```powershell
mvn exec:java -Dexec.mainClass=com.bank.Main
```

Alternatively, in VS Code use **Run > Start Debugging** with the included launch configuration, or run the **Maven: package** / **Maven: run banking app** tasks.

## Sample Login

After loading the SQL script, use either account:

- Email: `alex@example.com`
- Password: `BankDemo123!`
- Account number: `100000000001`

Or:

- Email: `jordan@example.com`
- Password: `BankDemo123!`
- Account number: `100000000002`

The demo password is for local testing only. Change it or register a new customer for any non-demo deployment.

## Desktop App

The first screen is a sign-in/register window. After login, the dashboard provides Overview, Accounts, and Activity views. From the dashboard you can create accounts, deposit, withdraw, transfer between accounts, search account numbers, review history in either date order, and close zero-balance accounts.

## Possible Future Enhancements

- Add administrator workflows for blocking or reactivating customers and accounts.
- Add audit logging, configurable daily transaction limits, and stronger session controls.
- Add automated integration tests using a disposable MySQL instance or Testcontainers.
- Add account statements as CSV/PDF exports and configurable currency/locale display.
- Add a REST API after the core JDBC behavior is established.