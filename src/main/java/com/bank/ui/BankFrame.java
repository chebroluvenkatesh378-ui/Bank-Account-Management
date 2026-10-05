package com.bank.ui;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class BankFrame extends JFrame {
    private static final Color INK = new Color(27, 44, 41);
    private static final Color GREEN = new Color(37, 111, 83);
    private static final Color MINT = new Color(220, 238, 225);
    private static final Color LIME = new Color(218, 235, 111);
    private static final Color CORAL = new Color(218, 101, 82);
    private static final Color PAPER = new Color(245, 247, 243);
    private static final Color MUTED = new Color(104, 119, 112);
    private static final Color LINE = new Color(222, 229, 221);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font DISPLAY = new Font("Georgia", Font.BOLD, 28);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CustomerService customerService = new CustomerService(new CustomerDAO());
    private final AccountService accountService = new AccountService(new AccountDAO());
    private final TransactionService transactionService =
            new TransactionService(new AccountDAO(), new TransactionDAO());
    private final CardLayout rootLayout = new CardLayout();
    private final JPanel root = new JPanel(rootLayout);
    private final JPanel pageContent = new JPanel(new BorderLayout());
    private JPanel sidebarPanel;
    private JPanel dashboardPanel;
    private Customer customer;
    private List<Account> accounts = List.of();
    private String activePage = "Overview";
    private JTable accountTable;
    private TableRowSorter<DefaultTableModel> accountSorter;

    public BankFrame() {
        super("Northbank | Personal Banking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1180, 790);
        setLocationRelativeTo(null);
        root.add(buildAuthScreen(), "auth");
        setContentPane(root);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                rootLayout.show(root, "auth");
            }
        });
    }

    private JPanel buildAuthScreen() {
        JPanel screen = new JPanel(new GridLayout(1, 2));
        screen.setBackground(PAPER);

        JPanel brand = new JPanel();
        brand.setBackground(INK);
        brand.setBorder(BorderFactory.createEmptyBorder(54, 54, 54, 46));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel brandName = label("NORTHBANK", 15, LIME, Font.BOLD);
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(90));
        JLabel headline = new JLabel("Your money,\nin good hands.");
        headline.setText("<html>Your money,<br>in good hands.</html>");
        headline.setFont(new Font("Georgia", Font.BOLD, 38));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(headline);
        brand.add(Box.createVerticalStrut(22));
        JLabel detail = label("A clearer view of your accounts and everyday banking.", 15,
                new Color(194, 211, 200), Font.PLAIN);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(detail);
        brand.add(Box.createVerticalGlue());
        JLabel footnote = label("PERSONAL BANKING  /  EST. 2025", 11, new Color(149, 172, 158), Font.BOLD);
        footnote.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(footnote);

        JPanel formArea = new JPanel(new java.awt.GridBagLayout());
        formArea.setBackground(PAPER);
        JPanel forms = new JPanel(new CardLayout());
        forms.setOpaque(false);
        forms.add(buildLoginForm(forms), "login");
        forms.add(buildRegistrationForm(forms), "register");
        java.awt.GridBagConstraints formConstraints = new java.awt.GridBagConstraints();
        formConstraints.gridx = 0;
        formConstraints.gridy = 0;
        formConstraints.weightx = 1;
        formConstraints.weighty = 1;
        formConstraints.fill = java.awt.GridBagConstraints.BOTH;
        formArea.add(forms, formConstraints);
        screen.add(brand);
        screen.add(formArea);
        return screen;
    }

    private JPanel buildLoginForm(JPanel forms) {
        JPanel panel = formPanel();
        panel.add(label("WELCOME BACK", 12, GREEN, Font.BOLD));
        panel.add(Box.createVerticalStrut(10));
        panel.add(label("Sign in", 30, INK, Font.BOLD));
        panel.add(Box.createVerticalStrut(6));
        panel.add(label("Access your personal banking dashboard.", 14, MUTED, Font.PLAIN));
        panel.add(Box.createVerticalStrut(28));
        JTextField email = textField();
        JPasswordField password = passwordField();
        panel.add(fieldBlock("Email address", email));
        panel.add(Box.createVerticalStrut(16));
        panel.add(fieldBlock("Password", password));
        panel.add(Box.createVerticalStrut(22));
        JButton login = button("Sign in", GREEN, Color.WHITE);
        login.addActionListener(event -> {
            char[] secret = password.getPassword();
            try {
                customer = customerService.login(email.getText().trim(), new String(secret));
                password.setText("");
                showDashboard();
            } catch (BankException exception) {
                showError(exception);
            } finally {
                Arrays.fill(secret, '\0');
            }
        });
        panel.add(login);
        panel.add(Box.createVerticalStrut(18));
        JButton register = textButton("New to Northbank?  Create an account", GREEN);
        register.addActionListener(event -> ((CardLayout) forms.getLayout()).show(forms, "register"));
        panel.add(register);
        return panel;
    }

    private JPanel buildRegistrationForm(JPanel forms) {
        JPanel panel = formPanel();
        panel.add(label("GET STARTED", 12, GREEN, Font.BOLD));
        panel.add(Box.createVerticalStrut(10));
        panel.add(label("Create your profile", 28, INK, Font.BOLD));
        panel.add(Box.createVerticalStrut(20));
        JTextField name = textField();
        JTextField email = textField();
        JTextField phone = textField();
        JPasswordField password = passwordField();
        panel.add(fieldBlock("Full name", name));
        panel.add(Box.createVerticalStrut(11));
        panel.add(fieldBlock("Email address", email));
        panel.add(Box.createVerticalStrut(11));
        panel.add(fieldBlock("Phone number", phone));
        panel.add(Box.createVerticalStrut(11));
        panel.add(fieldBlock("Password (8 characters minimum)", password));
        panel.add(Box.createVerticalStrut(18));
        JButton create = button("Create profile", GREEN, Color.WHITE);
        create.addActionListener(event -> {
            char[] secret = password.getPassword();
            try {
                int id = customerService.register(name.getText(), email.getText(), phone.getText(), new String(secret));
                JOptionPane.showMessageDialog(this, "Profile created. Customer ID: " + id,
                        "Registration complete", JOptionPane.INFORMATION_MESSAGE);
                email.setText("");
                password.setText("");
                ((CardLayout) forms.getLayout()).show(forms, "login");
            } catch (BankException exception) {
                showError(exception);
            } finally {
                Arrays.fill(secret, '\0');
            }
        });
        panel.add(create);
        panel.add(Box.createVerticalStrut(12));
        JButton back = textButton("Already registered?  Sign in", GREEN);
        back.addActionListener(event -> ((CardLayout) forms.getLayout()).show(forms, "login"));
        panel.add(back);
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 45, 24, 45));
        return panel;
    }

    private JPanel fieldBlock(String title, Component field) {
        JPanel block = new JPanel(new BorderLayout(0, 7));
        block.setOpaque(false);
        JLabel label = label(title, 13, INK, Font.BOLD);
        block.add(label, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        return block;
    }

    private void showDashboard() {
        activePage = "Overview";
        refreshAccounts(false);
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(PAPER);
        sidebarPanel = buildSidebar();
        dashboard.add(sidebarPanel, BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(PAPER);
        main.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setOpaque(false);
        JLabel greeting = label("Good day, " + customer.getName(), 19, INK, Font.BOLD);
        JLabel identity = label(customer.getEmail(), 13, MUTED, Font.PLAIN);
        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        who.add(greeting);
        who.add(Box.createVerticalStrut(4));
        who.add(identity);
        topbar.add(who, BorderLayout.WEST);
        main.add(topbar, BorderLayout.NORTH);
        pageContent.setOpaque(false);
        main.add(pageContent, BorderLayout.CENTER);
        dashboard.add(main, BorderLayout.CENTER);

        if (dashboardPanel != null) root.remove(dashboardPanel);
        dashboardPanel = dashboard;
        root.add(dashboardPanel, "dashboard");
        rootLayout.show(root, "dashboard");
        renderPage();
        root.revalidate();
        root.repaint();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(224, 0));
        sidebar.setBackground(INK);
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 18, 22, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel logo = label("NORTHBANK", 15, LIME, Font.BOLD);
        logo.setBorder(BorderFactory.createEmptyBorder(4, 12, 32, 0));
        top.add(logo);
        addNavButton(top, "Overview");
        addNavButton(top, "Accounts");
        addNavButton(top, "Activity");
        sidebar.add(top, BorderLayout.NORTH);

        JButton logout = button("Log out", new Color(47, 69, 62), Color.WHITE);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.addActionListener(event -> {
            customer = null;
            accounts = List.of();
            rootLayout.show(root, "auth");
        });
        sidebar.add(logout, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavButton(JPanel nav, String name) {
        JButton item = button(name, name.equals(activePage) ? GREEN : INK,
                name.equals(activePage) ? Color.WHITE : new Color(196, 211, 200));
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setBorder(BorderFactory.createEmptyBorder(12, 13, 12, 10));
        item.addActionListener(event -> {
            activePage = name;
            renderPage();
            updateNavigation();
        });
        nav.add(item);
        nav.add(Box.createVerticalStrut(7));
    }

    private void updateNavigation() {
        if (sidebarPanel == null || sidebarPanel.getComponentCount() == 0) return;
        JPanel nav = (JPanel) sidebarPanel.getComponent(0);
        for (Component component : nav.getComponents()) {
            if (component instanceof JButton item) {
                boolean selected = item.getText().equals(activePage);
                item.setBackground(selected ? GREEN : INK);
                item.setForeground(selected ? Color.WHITE : new Color(196, 211, 200));
            }
        }
    }

    private void renderPage() {
        pageContent.removeAll();
        switch (activePage) {
            case "Accounts" -> pageContent.add(buildAccountsPage(), BorderLayout.CENTER);
            case "Activity" -> pageContent.add(buildActivityPage(), BorderLayout.CENTER);
            default -> pageContent.add(buildOverviewPage(), BorderLayout.CENTER);
        }
        pageContent.revalidate();
        pageContent.repaint();
    }

    private JPanel pageShell(String title, String subtitle) {
        JPanel shell = new JPanel(new BorderLayout(0, 20));
        shell.setOpaque(false);
        shell.setBorder(BorderFactory.createEmptyBorder(34, 0, 0, 0));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel titleLabel = label(title, 29, INK, Font.BOLD);
        titleLabel.setFont(DISPLAY);
        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(5));
        heading.add(label(subtitle, 14, MUTED, Font.PLAIN));
        shell.add(heading, BorderLayout.NORTH);
        return shell;
    }

    private JPanel buildOverviewPage() {
        JPanel shell = pageShell("Overview", "Your accounts at a glance.");
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        if (accounts.isEmpty()) {
            body.add(emptyState("Start with your first account", "Create a savings or current account to get going.",
                    this::createAccount));
            shell.add(body, BorderLayout.CENTER);
            return shell;
        }

        Account account = preferredAccount();
        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        summary.add(metricCard("AVAILABLE BALANCE", currency(account.getBalance()), account.getAccountNumber(),
                MINT, GREEN));
        summary.add(metricCard("ACCOUNT TYPE", titleCase(account.getAccountType()), "Personal account",
                Color.WHITE, INK));
        summary.add(metricCard("ACCOUNT STATUS", titleCase(account.getStatus()), "Opened "
                + formatDate(account.getCreatedAt()), new Color(250, 235, 214), CORAL));
        body.add(summary);
        body.add(Box.createVerticalStrut(17));

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel actionsLabel = label("QUICK ACTIONS", 12, MUTED, Font.BOLD);
        actions.add(actionsLabel, BorderLayout.WEST);
        JPanel actionButtons = new JPanel(new GridLayout(1, 4, 9, 0));
        actionButtons.setOpaque(false);
        JButton deposit = button("Deposit", GREEN, Color.WHITE);
        deposit.addActionListener(event -> amountAction(true));
        JButton withdraw = button("Withdraw", Color.WHITE, INK);
        withdraw.addActionListener(event -> amountAction(false));
        JButton transfer = button("Transfer", Color.WHITE, INK);
        transfer.addActionListener(event -> transferAction());
        JButton newAccount = button("New account", LIME, INK);
        newAccount.addActionListener(event -> createAccount());
        actionButtons.add(deposit);
        actionButtons.add(withdraw);
        actionButtons.add(transfer);
        actionButtons.add(newAccount);
        actions.add(actionButtons, BorderLayout.EAST);
        body.add(actions);
        body.add(Box.createVerticalStrut(20));

        JPanel recent = sectionPanel("Recent activity", "Latest transactions for " + account.getAccountNumber());
        try {
            List<Transaction> history = transactionService.getHistory(customer.getCustomerId(),
                    account.getAccountNumber(), true);
            recent.add(transactionTable(history.stream().limit(5).toList()), BorderLayout.CENTER);
        } catch (BankException exception) {
            recent.add(label(exception.getMessage(), 13, CORAL, Font.PLAIN), BorderLayout.CENTER);
        }
        body.add(recent);
        shell.add(body, BorderLayout.CENTER);
        return shell;
    }

    private JPanel buildAccountsPage() {
        JPanel shell = pageShell("Accounts", "Manage and search your bank accounts.");
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);
        JTextField search = textField();
        search.setToolTipText("Filter by account number");
        controls.add(search, BorderLayout.CENTER);
        JPanel controlButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        controlButtons.setOpaque(false);
        JButton close = button("Close account", Color.WHITE, CORAL);
        close.addActionListener(event -> closeAccount());
        JButton addAccount = button("+  New account", GREEN, Color.WHITE);
        addAccount.addActionListener(event -> createAccount());
        controlButtons.add(close);
        controlButtons.add(addAccount);
        controls.add(controlButtons, BorderLayout.EAST);
        body.add(controls, BorderLayout.NORTH);

        DefaultTableModel model = accountTableModel();
        accountTable = styledTable(model);
        accountSorter = new TableRowSorter<>(model);
        accountTable.setRowSorter(accountSorter);
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String value = search.getText().trim();
                accountSorter.setRowFilter(value.isEmpty() ? null
                        : RowFilter.regexFilter("(?i)" + Pattern.quote(value), 1));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { filter(); }
        });
        JPanel tablePanel = sectionPanel("Your accounts", accounts.size() + " account(s)");
        tablePanel.add(new JScrollPane(accountTable), BorderLayout.CENTER);
        body.add(tablePanel, BorderLayout.CENTER);
        shell.add(body, BorderLayout.CENTER);
        return shell;
    }

    private JPanel buildActivityPage() {
        JPanel shell = pageShell("Activity", "Review transactions and choose a date order.");
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        JPanel filters = new JPanel(new BorderLayout(12, 0));
        filters.setOpaque(false);
        JComboBox<String> accountSelect = accountSelector();
        JComboBox<String> sortSelect = new JComboBox<>(new String[]{"Newest first", "Oldest first"});
        styleCombo(accountSelect);
        styleCombo(sortSelect);
        filters.add(labeledControl("ACCOUNT", accountSelect), BorderLayout.CENTER);
        filters.add(labeledControl("SORT BY DATE", sortSelect), BorderLayout.EAST);
        body.add(filters, BorderLayout.NORTH);

        JPanel historySection = sectionPanel("Transaction history", "Deposits, withdrawals, and transfers");
        JPanel tableHolder = new JPanel(new BorderLayout());
        tableHolder.setOpaque(false);
        historySection.add(tableHolder, BorderLayout.CENTER);
        Runnable loadHistory = () -> {
            tableHolder.removeAll();
            String accountNumber = (String) accountSelect.getSelectedItem();
            if (accountNumber == null) {
                tableHolder.add(emptyState("No accounts yet", "Create an account to see activity.",
                        this::createAccount));
            } else {
                try {
                    boolean newestFirst = sortSelect.getSelectedIndex() == 0;
                    List<Transaction> transactions = transactionService.getHistory(customer.getCustomerId(),
                            accountNumber, newestFirst);
                    tableHolder.add(new JScrollPane(transactionTable(transactions)), BorderLayout.CENTER);
                } catch (BankException exception) {
                    tableHolder.add(label(exception.getMessage(), 13, CORAL, Font.PLAIN), BorderLayout.NORTH);
                }
            }
            tableHolder.revalidate();
            tableHolder.repaint();
        };
        accountSelect.addActionListener(event -> loadHistory.run());
        sortSelect.addActionListener(event -> loadHistory.run());
        loadHistory.run();
        body.add(historySection, BorderLayout.CENTER);
        shell.add(body, BorderLayout.CENTER);
        return shell;
    }

    private JPanel metricCard(String title, String value, String caption, Color background, Color accent) {
        JPanel card = new JPanel();
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(17, 18, 15, 18)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(label(title, 11, accent, Font.BOLD));
        card.add(Box.createVerticalStrut(13));
        JLabel main = label(value, 23, INK, Font.BOLD);
        main.setFont(new Font("Georgia", Font.BOLD, 23));
        card.add(main);
        card.add(Box.createVerticalStrut(5));
        card.add(label(caption, 12, MUTED, Font.PLAIN));
        return card;
    }

    private JPanel sectionPanel(String title, String subtitle) {
        JPanel section = new JPanel(new BorderLayout(0, 13));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(16, 17, 17, 17)));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(label(title, 16, INK, Font.BOLD));
        heading.add(Box.createVerticalStrut(4));
        heading.add(label(subtitle, 12, MUTED, Font.PLAIN));
        section.add(heading, BorderLayout.NORTH);
        return section;
    }

    private JPanel emptyState(String title, String description, Runnable action) {
        JPanel empty = new JPanel();
        empty.setBackground(Color.WHITE);
        empty.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(32, 28, 32, 28)));
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        JLabel titleLabel = label(title, 21, INK, Font.BOLD);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        empty.add(titleLabel);
        empty.add(Box.createVerticalStrut(8));
        JLabel descriptionLabel = label(description, 14, MUTED, Font.PLAIN);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        empty.add(descriptionLabel);
        empty.add(Box.createVerticalStrut(18));
        JButton create = button("Create an account", GREEN, Color.WHITE);
        create.setAlignmentX(Component.LEFT_ALIGNMENT);
        create.addActionListener(event -> action.run());
        empty.add(create);
        return empty;
    }

    private JComboBox<String> accountSelector() {
        String[] numbers = accounts.stream().map(Account::getAccountNumber).toArray(String[]::new);
        JComboBox<String> selector = new JComboBox<>(numbers);
        styleCombo(selector);
        return selector;
    }

    private JPanel labeledControl(String title, Component control) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(label(title, 11, MUTED, Font.BOLD), BorderLayout.NORTH);
        panel.add(control, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(220, 66));
        return panel;
    }

    private DefaultTableModel accountTableModel() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "ACCOUNT NUMBER", "TYPE", "BALANCE", "STATUS", "OPENED"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Account account : accounts) {
            model.addRow(new Object[]{account.getAccountId(), account.getAccountNumber(),
                    titleCase(account.getAccountType()), currency(account.getBalance()),
                    titleCase(account.getStatus()), formatDate(account.getCreatedAt())});
        }
        return model;
    }

    private JTable transactionTable(List<Transaction> transactions) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "TYPE", "AMOUNT", "DATE / TIME", "REFERENCE"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Transaction transaction : transactions) {
            model.addRow(new Object[]{transaction.getTransactionId(), titleCase(transaction.getTransactionType()),
                    currency(transaction.getAmount()), formatDate(transaction.getTransactionDate()),
                    transaction.getReferenceAccount()});
        }
        return styledTable(model);
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(BODY);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setSelectionBackground(MINT);
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(239, 243, 238));
        table.getTableHeader().setForeground(MUTED);
        table.setFillsViewportHeight(true);
        return table;
    }

    private void createAccount() {
        JComboBox<String> type = new JComboBox<>(new String[]{"Savings", "Current"});
        styleCombo(type);
        int result = JOptionPane.showConfirmDialog(this, type, "Choose account type",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try {
            Account account = accountService.createAccount(customer.getCustomerId(),
                    type.getSelectedIndex() == 0 ? "SAVINGS" : "CURRENT");
            refreshAccounts(true);
            JOptionPane.showMessageDialog(this, "Account created: " + account.getAccountNumber(),
                    "Account ready", JOptionPane.INFORMATION_MESSAGE);
            renderPage();
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void amountAction(boolean deposit) {
        JComboBox<String> account = accountSelector();
        JTextField amount = textField();
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel("Account number"));
        form.add(account);
        form.add(new JLabel(deposit ? "Deposit amount" : "Withdrawal amount"));
        form.add(amount);
        int result = JOptionPane.showConfirmDialog(this, form, deposit ? "Deposit" : "Withdraw",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try {
            BigDecimal value = new BigDecimal(amount.getText().trim());
            String number = (String) account.getSelectedItem();
            if (deposit) transactionService.deposit(customer.getCustomerId(), number, value);
            else transactionService.withdraw(customer.getCustomerId(), number, value);
            refreshAccounts(true);
            JOptionPane.showMessageDialog(this, deposit ? "Deposit completed." : "Withdrawal completed.",
                    "Transaction complete", JOptionPane.INFORMATION_MESSAGE);
            renderPage();
        } catch (NumberFormatException exception) {
            showMessage("Enter a valid amount.");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void transferAction() {
        JComboBox<String> source = accountSelector();
        JTextField destination = textField();
        JTextField amount = textField();
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel("From account"));
        form.add(source);
        form.add(new JLabel("To account number"));
        form.add(destination);
        form.add(new JLabel("Amount"));
        form.add(amount);
        int result = JOptionPane.showConfirmDialog(this, form, "Transfer funds",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try {
            transactionService.transfer(customer.getCustomerId(), (String) source.getSelectedItem(),
                    destination.getText().trim(), new BigDecimal(amount.getText().trim()));
            refreshAccounts(true);
            JOptionPane.showMessageDialog(this, "Transfer completed.", "Transfer complete",
                    JOptionPane.INFORMATION_MESSAGE);
            renderPage();
        } catch (NumberFormatException exception) {
            showMessage("Enter a valid amount.");
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private void closeAccount() {
        JComboBox<String> account = accountSelector();
        int result = JOptionPane.showConfirmDialog(this, account, "Choose account to close",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        String number = (String) account.getSelectedItem();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Close account " + number + "? Its balance must be zero.", "Confirm closure",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            accountService.closeAccount(customer.getCustomerId(), number);
            refreshAccounts(true);
            showMessage("Account closed. Its record has been retained.");
            renderPage();
        } catch (BankException exception) {
            showError(exception);
        }
    }

    private Account preferredAccount() {
        return accounts.get(0);
    }

    private void refreshAccounts(boolean showErrors) {
        try {
            accounts = accountService.getCustomerAccounts(customer.getCustomerId());
        } catch (BankException exception) {
            accounts = List.of();
            if (showErrors) showError(exception);
        }
    }

    private void showError(BankException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Banking error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Northbank", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel label(String text, int size, Color color, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", style, size));
        label.setForeground(color);
        return label;
    }

    private JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(BODY);
        field.setPreferredSize(new Dimension(220, 40));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return field;
    }

    private JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(BODY);
        field.setPreferredSize(new Dimension(220, 40));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return field;
    }

    private JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JButton textButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(color);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(BODY);
        combo.setPreferredSize(new Dimension(190, 40));
        combo.setBackground(Color.WHITE);
    }

    private String currency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "-" : date.format(DATE_FORMAT);
    }

    private String titleCase(String value) {
        if (value == null || value.isEmpty()) return "-";
        String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

}