import java.sql.*;
import java.util.Scanner;

public class Online_Banking_System 
{

	
    // Account Class
    public abstract static class Account 
    {
        protected int accountId;
        protected double balance;
        protected User user;

        public Account(int accountId, double balance, User user)
        {
            this.accountId = accountId;
            this.balance = balance;
            this.user = user;
        }

        public int getAccountId() 
        {
            return accountId;
        }

        public abstract void createAccount(String accountType);

        public abstract double getBalance();

        public abstract void transferFunds(Account toAccount, double amount);

        public abstract void getTransactionHistory();
    }
    

    // Savings Account
    public static class SavingsAccount extends Account 
    {
        public SavingsAccount(int accountId, double balance, User user) 
        {
            super(accountId, balance, user);
        }

        @Override
        public void createAccount(String accountType) 
        {
            try (Connection connection = DatabaseConnection.getConnection()) 
            {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO accounts(user_id, balance, account_type) VALUES (?, ?, ?)");
                statement.setInt(1, this.user.getUserId());
                statement.setDouble(2, this.balance);
                statement.setString(3, accountType);
                statement.executeUpdate();
                System.out.println("Savings Account created successfully!");
            } 
            catch (SQLException e) 
            {
                e.printStackTrace();
            }
        }

        @Override
        public double getBalance() 
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("SELECT balance FROM accounts WHERE account_id = ?");
                statement.setInt(1, this.accountId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next())
                {
                    return resultSet.getDouble("balance");
                }
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            return 0; // Return 0 if balance retrieval fails
        }

        @Override
        public void transferFunds(Account toAccount, double amount)
        {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your password to proceed: ");
            String enteredPassword = scanner.nextLine();

            if (user.authenticate(enteredPassword)) {
                try (Connection connection = DatabaseConnection.getConnection())
                {
                    connection.setAutoCommit(false);

                    PreparedStatement deductStatement = connection.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
                    deductStatement.setDouble(1, amount);
                    deductStatement.setInt(2, this.accountId);
                    deductStatement.executeUpdate();

                    PreparedStatement addStatement = connection.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
                    addStatement.setDouble(1, amount);
                    addStatement.setInt(2, toAccount.getAccountId());
                    addStatement.executeUpdate();

                    PreparedStatement transactionStatement = connection.prepareStatement("INSERT INTO transactions (from_account_id, to_account_id, amount) VALUES (?, ?, ?)");
                    transactionStatement.setInt(1, this.accountId);
                    transactionStatement.setInt(2, toAccount.getAccountId());
                    transactionStatement.setDouble(3, amount);
                    transactionStatement.executeUpdate();

                    connection.commit();
                    System.out.println("Funds transferred successfully!");
                } 
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            } 
            else 
            {
            	System.out.println("Incorrect password. Transfer aborted.");
            }
        }

        @Override
        public void getTransactionHistory() 
        {
            try (Connection connection = DatabaseConnection.getConnection()) 
            {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY transaction_date DESC LIMIT 7");
                statement.setInt(1, this.accountId);
                statement.setInt(2, this.accountId);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) 
                {
                    System.out.println("Transaction ID: " + resultSet.getInt("transaction_id"));
                    System.out.println("From Account: " + resultSet.getInt("from_account_id"));
                    System.out.println("To Account: " + resultSet.getInt("to_account_id"));
                    System.out.println("Amount: " + resultSet.getDouble("amount"));
                    System.out.println("Date: " + resultSet.getTimestamp("transaction_date"));
                    System.out.println("-----------------------------------");
                }
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    // Checking Account
    public static class CheckingAccount extends Account
    {
        public CheckingAccount(int accountId, double balance, User user) 
        {
            super(accountId, balance, user);
        }

        @Override
        public void createAccount(String accountType)
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO accounts(user_id, balance, account_type) VALUES (?, ?, ?)");
                statement.setInt(1, this.user.getUserId());
                statement.setDouble(2, this.balance);
                statement.setString(3, accountType);
                statement.executeUpdate();
                System.out.println("Checking Account created successfully!");
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public double getBalance()
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("SELECT balance FROM accounts WHERE account_id = ?");
                statement.setInt(1, this.accountId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next())
                {
                    return resultSet.getDouble("balance");
                }
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            return 0;
        }
        
        @Override
        public void transferFunds(Account toAccount, double amount) 
        {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your password to proceed: ");
            String enteredPassword = scanner.nextLine();

            if (user.authenticate(enteredPassword))
            {
                try (Connection connection = DatabaseConnection.getConnection())
                {
                    connection.setAutoCommit(false);

                    PreparedStatement deductStatement = connection.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
                    deductStatement.setDouble(1, amount);
                    deductStatement.setInt(2, this.accountId);
                    deductStatement.executeUpdate();

                    PreparedStatement addStatement = connection.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
                    addStatement.setDouble(1, amount);
                    addStatement.setInt(2, toAccount.getAccountId());
                    addStatement.executeUpdate();

                    PreparedStatement transactionStatement = connection.prepareStatement("INSERT INTO transactions (from_account_id, to_account_id, amount) VALUES (?, ?, ?)");
                    transactionStatement.setInt(1, this.accountId);
                    transactionStatement.setInt(2, toAccount.getAccountId());
                    transactionStatement.setDouble(3, amount);
                    transactionStatement.executeUpdate();

                    connection.commit();
                    System.out.println("Funds transferred successfully!");
                } 
                catch (SQLException e) 
                {
                    e.printStackTrace();
                }
            } 
            else
            {
                System.out.println("Incorrect password. Transfer aborted.");
            }
        }

        @Override
        public void getTransactionHistory()
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY transaction_date DESC LIMIT 7");
                statement.setInt(1, this.accountId);
                statement.setInt(2, this.accountId);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next())
                {
                    System.out.println("Transaction ID: " + resultSet.getInt("transaction_id"));
                    System.out.println("From Account: " + resultSet.getInt("from_account_id"));
                    System.out.println("To Account: " + resultSet.getInt("to_account_id"));
                    System.out.println("Amount: " + resultSet.getDouble("amount"));
                    System.out.println("Date: " + resultSet.getTimestamp("transaction_date"));
                    System.out.println("-----------------------------------");
                }
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    
    // User Class
    public static class User 
    {
        private int userId;
        private String username;
        private String password;

        public User(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        public int getUserId()
        {
            return userId;
        }

        public void register(int accountTypeChoice)
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO users(username, password) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, this.username);
                statement.setString(2, this.password);
                statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next())
                {
                    this.userId = rs.getInt(1);
                }
                if (accountTypeChoice == 1)
                {
                    SavingsAccount savingsAccount = new SavingsAccount(0, 0, this);
                    savingsAccount.createAccount("Savings");
                } 
                else if (accountTypeChoice == 2)
                {
                    CheckingAccount checkingAccount = new CheckingAccount(0, 0, this);
                    checkingAccount.createAccount("Checking");
                }
                System.out.println("User registered successfully!");
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        public boolean authenticate() 
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
                statement.setString(1, this.username);
                statement.setString(2, this.password);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next())
                {
                    this.userId = resultSet.getInt("user_id");
                    return true;
                }
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            return false;
        }

        public boolean authenticate(String enteredPassword)
        {
            return this.password.equals(enteredPassword);
        }
    }

    
    // Transaction Class
    public static class Transaction
    {
        private Account fromAccount;
        private Account toAccount;
        private double amount;

        public Transaction(Account fromAccount, Account toAccount, double amount)
        {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
        }

        public void transferFunds() {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                connection.setAutoCommit(false);

                // Deduct amount from sender's account
                PreparedStatement deductStatement = connection.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
                deductStatement.setDouble(1, amount);
                deductStatement.setInt(2, this.fromAccount.getAccountId());
                deductStatement.executeUpdate();

                // Add amount to receiver's account
                PreparedStatement addStatement = connection.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
                addStatement.setDouble(1, amount);
                addStatement.setInt(2, this.toAccount.getAccountId());
                addStatement.executeUpdate();

                // Record transaction
                PreparedStatement transactionStatement = connection.prepareStatement("INSERT INTO transactions (from_account_id, to_account_id, amount) VALUES (?, ?, ?)");
                transactionStatement.setInt(1, this.fromAccount.getAccountId());
                transactionStatement.setInt(2, this.toAccount.getAccountId());
                transactionStatement.setDouble(3, amount);
                transactionStatement.executeUpdate();

                connection.commit();
                System.out.println("Funds transferred successfully!");
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    // Loan Class
    public static class Loan
    {
        private User user;
        private double loanAmount;
        private double remainingAmount;

        public Loan(User user, double loanAmount)
        {
            this.user = user;
            this.loanAmount = loanAmount;
            this.remainingAmount = loanAmount;
        }

        public void applyLoan()
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO loans(user_id, loan_amount, remaining_amount) VALUES (?, ?, ?)");
                statement.setInt(1, this.user.getUserId());
                statement.setDouble(2, this.loanAmount);
                statement.setDouble(3, this.remainingAmount);
                statement.executeUpdate();
                System.out.println("Loan applied successfully!");
            } 
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        public void managePayments(double paymentAmount)
        {
            try (Connection connection = DatabaseConnection.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement("UPDATE loans SET remaining_amount = GREATEST(remaining_amount - ?, 0) WHERE user_id = ?");
                statement.setDouble(1, paymentAmount);
                statement.setInt(2, this.user.getUserId());
                statement.executeUpdate();
                System.out.println("Payment processed successfully!");
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
    

    // Database Connection Class
    public static class DatabaseConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/banking_system";
        private static final String USERNAME = "root";
        private static final String PASSWORD = "" // your password;

        static 
        {
            try 
            {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
            } 
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                throw new RuntimeException("Failed to load MySQL JDBC driver");
            }
        }

        public static Connection getConnection() throws SQLException
        {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
    }

    // Main Method
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Online Banking System");
        System.out.println("Press 1 to Login");
        System.out.println("Press 2 to Register");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) 
        {
            case 1:
                System.out.print("Username: ");
                String loginUsername = scanner.nextLine();

                System.out.print("Password: ");
                String loginPassword = scanner.nextLine();

                User loginUser = new User(loginUsername, loginPassword);

                if (loginUser.authenticate())
                {
                    System.out.println("Login successful!");
                    System.out.println("Press 1 to check balance");
                    System.out.println("Press 2 to transfer funds");
                    System.out.println("Press 3 to view transaction history");
                    System.out.println("Press 4 to apply for a loan");
                    System.out.print("Enter your choice: ");
                    int bankingChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (bankingChoice)
                    {
                        case 1:
                            try (Connection connection = DatabaseConnection.getConnection()) 
                            {
                                PreparedStatement statement = connection.prepareStatement("SELECT * FROM accounts WHERE user_id = ?");
                                statement.setInt(1, loginUser.getUserId());
                                ResultSet resultSet = statement.executeQuery();

                                while (resultSet.next())
                                {
                                    int accountId = resultSet.getInt("account_id");
                                    String accountType = resultSet.getString("account_type");
                                    double balance = resultSet.getDouble("balance");

                                    System.out.println("Account ID: " + accountId);
                                    System.out.println("Account Type: " + accountType);
                                    System.out.println("Balance: " + balance);
                                    System.out.println("-----------------------------------");
                                }
                            } 
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                            }
                            break;
                        case 2:
                            System.out.println("Transfer Funds");
                            System.out.print("Enter your account ID: ");
                            int fromAccountId = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            System.out.print("Enter recipient's account ID: ");
                            int toAccountId = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            System.out.print("Enter amount to transfer: ");
                            double transferAmount = scanner.nextDouble();
                            scanner.nextLine(); // Consume newline

                            // Get accounts based on account IDs
                            Account fromAccount = null;
                            Account toAccount = null;
                            try (Connection connection = DatabaseConnection.getConnection())
                            {
                                PreparedStatement statement = connection.prepareStatement("SELECT * FROM accounts WHERE account_id = ?");
                                statement.setInt(1, fromAccountId);
                                ResultSet resultSet = statement.executeQuery();
                                if (resultSet.next())
                                {
                                    String accountType = resultSet.getString("account_type");
                                    double balance = resultSet.getDouble("balance");
                                    if (accountType.equals("Savings"))
                                    {
                                        fromAccount = new SavingsAccount(fromAccountId, balance, loginUser);
                                    }
                                    else if (accountType.equals("Checking"))
                                    {
                                        fromAccount = new CheckingAccount(fromAccountId, balance, loginUser);
                                    }
                                }

                                statement.setInt(1, toAccountId);
                                resultSet = statement.executeQuery();
                                if (resultSet.next())
                                {
                                    String accountType = resultSet.getString("account_type");
                                    double balance = resultSet.getDouble("balance");
                                    if (accountType.equals("Savings"))
                                    {
                                        toAccount = new SavingsAccount(toAccountId, balance, loginUser);
                                    } 
                                    else if (accountType.equals("Checking"))
                                    {
                                        toAccount = new CheckingAccount(toAccountId, balance, loginUser);
                                    }
                                }
                            } 
                            catch (SQLException e) 
                            {
                                e.printStackTrace();
                            }

                            if (fromAccount != null && toAccount != null)
                            {
                                fromAccount.transferFunds(toAccount, transferAmount);
                            } 
                            else 
                            {
                                System.out.println("Invalid account IDs. Transfer aborted.");
                            }
                            break;
                        case 3:
                            System.out.println("Transaction History");
                            System.out.print("Enter your account ID: ");
                            int accountId = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            // Get account based on account ID
                            Account account = null;
                            try (Connection connection = DatabaseConnection.getConnection()) 
                            {
                                PreparedStatement statement = connection.prepareStatement("SELECT * FROM accounts WHERE account_id = ?");
                                statement.setInt(1, accountId);
                                ResultSet resultSet = statement.executeQuery();
                                if (resultSet.next())
                                {
                                    String accountType = resultSet.getString("account_type");
                                    double balance = resultSet.getDouble("balance");
                                    if (accountType.equals("Savings")) 
                                    {
                                        account = new SavingsAccount(accountId, balance, loginUser);
                                    } 
                                    else if (accountType.equals("Checking")) 
                                    {
                                        account = new CheckingAccount(accountId, balance, loginUser);
                                    }
                                }
                            } 
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                            }

                            if (account != null) 
                            {
                                account.getTransactionHistory();
                            } 
                            else
                            {
                                System.out.println("Invalid account ID.");
                            }
                            break;
                        case 4:
                            System.out.println("Loan Application");
                            System.out.print("Enter loan amount: ");
                            double loanAmount = scanner.nextDouble();
                            scanner.nextLine(); // Consume newline

                            Loan loan = new Loan(loginUser, loanAmount);
                            loan.applyLoan();
                            break;
                        default:
                            System.out.println("Invalid choice.");
                    }
                } 
                else 
                {
                    System.out.println("Login failed. Incorrect username or password.");
                }
                break;
            case 2:
                System.out.print("Enter username: ");
                String username = scanner.nextLine();

                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                System.out.println("Press 1 for Savings Account");
                System.out.println("Press 2 for Checking Account");
                System.out.print("Enter your choice: ");
                int accountTypeChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                User newUser = new User(username, password);
                newUser.register(accountTypeChoice);
                break;
            default:
                System.out.println("Invalid choice.");
        }

        scanner.close();
    }
}
