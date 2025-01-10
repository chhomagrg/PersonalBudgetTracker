import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PersonalBudgetTracker extends JFrame {
    // Maps to store categories and their respective values
    private static Map<String, Double> incomeCategories = new HashMap<>();
    private static Map<String, Double> expenseCategories = new HashMap<>();
    private static Map<String, Double> categoryLimits = new HashMap<>();
    private static double budget; // Total available budget

    // UI components for input and display
    private JTextField incomeAmountField, expenseAmountField, newIncomeCategoryField, newExpenseCategoryField;
    private JComboBox<String> incomeCategoryBox, expenseCategoryBox;
    private JLabel budgetLabel;
    private JTable financialDataTable;
    private DefaultTableModel tableModel;

    public PersonalBudgetTracker() {
        super("Personal Budget Tracker");
        loadData(); // Load saved data
        setupUI(); // Set up the user interface
        updateFinancialDataTable(); // Display the loaded data in the table
        updateBudgetLabel(); // Update the budget label after loading data
    }


    private void setupUI() {
        // Initialize UI components
        budgetLabel = new JLabel("Current Budget: $" + budget);
        incomeAmountField = new JTextField(10);
        expenseAmountField = new JTextField(10);
        newIncomeCategoryField = new JTextField(10);
        newExpenseCategoryField = new JTextField(10);

        // Dropdowns for selecting categories
        incomeCategoryBox = new JComboBox<>(incomeCategories.keySet().toArray(new String[0]));
        expenseCategoryBox = new JComboBox<>(expenseCategories.keySet().toArray(new String[0]));

        // Buttons for various actions
        JButton addIncomeButton = createButton("Add Income", e -> addIncome());
        JButton addExpenseButton = createButton("Add Expense", e -> addExpense());
        JButton addIncomeCategoryButton = createButton("Add Income Category", e -> addIncomeCategory());
        JButton addExpenseCategoryButton = createButton("Add Expense Category", e -> addExpenseCategory());
        JButton viewSummaryButton = createButton("View Summary", e -> showSummary());
        JButton exportDataButton = createButton("Export Data", e -> exportData());
        JButton exitButton = createButton("Exit", e -> exitApp());

        // Table to display financial data
        tableModel = new DefaultTableModel(new String[]{"Type", "Category", "Amount"}, 0);
        financialDataTable = new JTable(tableModel);

        // Set up panels for organized layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Add components to the main panel
        mainPanel.add(createPanel(budgetLabel));
        mainPanel.add(createPanel(new JLabel("Income: $"), incomeAmountField, new JLabel("Category:"), incomeCategoryBox, addIncomeButton));
        mainPanel.add(createPanel(new JLabel("Expense: $"), expenseAmountField, new JLabel("Category:"), expenseCategoryBox, addExpenseButton));
        mainPanel.add(createPanel(new JLabel("New Income Category:"), newIncomeCategoryField, addIncomeCategoryButton));
        mainPanel.add(createPanel(new JLabel("New Expense Category:"), newExpenseCategoryField, addExpenseCategoryButton));
        mainPanel.add(new JScrollPane(financialDataTable));
        mainPanel.add(createPanel(viewSummaryButton, exportDataButton, exitButton));

        add(mainPanel); // Add the main panel to the frame

        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Helper method to create a button
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        return button;
    }

    // Helper method to create a panel with components
    private JPanel createPanel(Component... components) {
        JPanel panel = new JPanel(new FlowLayout());
        for (Component component : components) {
            panel.add(component);
        }
        return panel;
    }

    // Add income to the selected category
    private void addIncome() {
        handleTransaction(incomeAmountField, incomeCategoryBox, "Income", true);
    }

    // Add expense to the selected category, checking limits
    private void addExpense() {
        String category = (String) expenseCategoryBox.getSelectedItem();
        double amount = parseAmount(expenseAmountField);
        if (amount < 0) return; // Invalid amount

        // Check if the expense exceeds the limit for the category
        if (categoryLimits.containsKey(category) && expenseCategories.getOrDefault(category, 0.0) + amount > categoryLimits.get(category)) {
            JOptionPane.showMessageDialog(this, "Expense exceeds the limit for category: " + category);
            return;
        }
        handleTransaction(expenseAmountField, expenseCategoryBox, "Expense", false);
    }

    // Handle income or expense transaction
    private void handleTransaction(JTextField amountField, JComboBox<String> categoryBox, String type, boolean isIncome) {
        String category = (String) categoryBox.getSelectedItem();
        double amount = parseAmount(amountField);
        if (amount < 0) return; // Invalid amount

        // Update budget and the respective category
        budget += isIncome ? amount : -amount;
        Map<String, Double> targetMap = isIncome ? incomeCategories : expenseCategories;
        targetMap.put(category, targetMap.getOrDefault(category, 0.0) + amount);
        tableModel.addRow(new Object[]{type, category, "$" + amount});
        updateBudgetLabel();
    }

    // Parse the entered amount, showing an error if invalid
    private double parseAmount(JTextField field) {
        try {
            return Double.parseDouble(field.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.");
            return -1;
        }
    }

    // Add a new income category
    private void addIncomeCategory() {
        addCategory(newIncomeCategoryField, incomeCategories, incomeCategoryBox);
    }

    // Add a new expense category, optionally setting a limit
    private void addExpenseCategory() {
        addCategory(newExpenseCategoryField, expenseCategories, expenseCategoryBox);
        String category = newExpenseCategoryField.getText().trim();
        if (!category.isEmpty() && !categoryLimits.containsKey(category)) {
            String limitStr = JOptionPane.showInputDialog(this, "Set spending limit for this category:");
            try {
                categoryLimits.put(category, Double.parseDouble(limitStr));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid limit. Please enter a valid number.");
            }
        }
    }

    // General method for adding categories
    private void addCategory(JTextField field, Map<String, Double> categories, JComboBox<String> categoryBox) {
        String newCategory = field.getText().trim();
        if (!newCategory.isEmpty() && !categories.containsKey(newCategory)) {
            categories.put(newCategory, 0.0);
            categoryBox.addItem(newCategory);
            field.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Category already exists or name is empty.");
        }
    }

    // Update financial data table with all transactions
    private void updateFinancialDataTable() {
        tableModel.setRowCount(0); // Clear the table
        incomeCategories.forEach((category, amount) -> tableModel.addRow(new Object[]{"Income", category, "$" + amount}));
        expenseCategories.forEach((category, amount) -> tableModel.addRow(new Object[]{"Expense", category, "$" + amount}));
    }

    // Update the budget label to show the current budget
    private void updateBudgetLabel() {
        budgetLabel.setText("Current Budget: $" + budget);
    }

    // Load saved data from a file
    private void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("data.ser"))) {
            incomeCategories = (Map<String, Double>) in.readObject();
            expenseCategories = (Map<String, Double>) in.readObject();
            budget = calculateBudgetFromSavedData(); // Calculate budget from loaded data
        } catch (IOException | ClassNotFoundException e) {
            // Initialize with default values if no data found
            incomeCategories.put("General", 0.0);
            expenseCategories.put("General", 0.0);
            budget = 0.0;
        }
    }


    // Save data to a file
    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data.ser"))) {
            out.writeObject(incomeCategories);
            out.writeObject(expenseCategories);
            out.writeObject(categoryLimits);
            out.writeDouble(budget);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data.");
        }
    }

    // Export financial data to a text file
    private void exportData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("financial_data.txt"))) {
            writer.println("Financial Data:");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.println(tableModel.getValueAt(i, 0) + ", " + tableModel.getValueAt(i, 1) + ", " + tableModel.getValueAt(i, 2));
            }
            writer.println("\nCurrent Budget: $" + budget);
            JOptionPane.showMessageDialog(this, "Data exported successfully to financial_data.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting data.");
        }
    }

    // Show a summary of all income and expense categories
    private void showSummary() {
        StringBuilder summary = new StringBuilder("Category Summary:\n\nIncome:\n");
        incomeCategories.forEach((k, v) -> summary.append(k).append(": $").append(v).append("\n"));
        summary.append("\nExpenses:\n");
        expenseCategories.forEach((k, v) -> summary.append(k).append(": $").append(v).append("\n"));
        JOptionPane.showMessageDialog(this, summary.toString());
    }

    // Calculate budget from saved income and expense data
    private double calculateBudgetFromSavedData() {
        double totalIncome = incomeCategories.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = expenseCategories.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalIncome - totalExpense;
    }

    // Exit the application, saving data first
    private void exitApp() {
        saveData();
        System.exit(0);
    }

    public static void main(String[] args) {
        new PersonalBudgetTracker();
    }
}
