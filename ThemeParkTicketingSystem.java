import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class CustomerInformation {
    private static int nextCustomerId = 1;
    private int custId;             // Unique customer ID
    private String custName;        // Customer's name
    private int ticketsPurchased;  // Number of tickets purchased
    private int counter;           // Counter to which the customer is assigned
    private boolean paid;          // Flag to track payment status

    public CustomerInformation(String custName, int ticketsPurchased) {
        this.custId = nextCustomerId++;
        this.custName = custName;
        this.ticketsPurchased = ticketsPurchased;
        this.counter = -1;  // Initially not assigned to any counter
        this.paid = false;
    }

    public int getCustId() {
        return custId;
    }

    public String getCustName() {
        return custName;
    }

    public int getTicketsPurchased() {
        return ticketsPurchased;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public boolean hasPaid() {
        return paid;
    }

    public void markAsPaid() {
        paid = true;
    }

    public void markAsUnpaid() {
        paid = false;
    }
}

public class  ThemeParkTicketingSystem extends JFrame {
    private LinkedList<CustomerInformation> customerList;  // List to store customer information
    private Queue<CustomerInformation> counter1Queue;      // Queue for customers at counter 1
    private Queue<CustomerInformation> counter2Queue;      // Queue for customers at counter 2
    private Queue<CustomerInformation> counter3Queue;      // Queue for customers at counter 3
    private Stack<CustomerInformation> completeStack;     // Stack to store paid customers
    private DefaultTableModel tableModel;                 // Table model for displaying customer data
    private JTable customerTable;                         // Table for displaying customer information
    private int ticketPrice = 15;                         // Price per ticket
    private int counter1Count = 0;                        // Count of customers at counter 1
    private int counter2Count = 0;                        // Count of customers at counter 2
    private int currentCounter = 1;                      // Current counter being processed
    private int paymentLimit = 5;                         // Payment limit per processing cycle
    private int receiptCounter = 1;                      // Counter for generating receipts


    public ThemeParkTicketingSystem() {
        customerList = new LinkedList<>();
        counter1Queue = new LinkedList<>();
        counter2Queue = new LinkedList<>();
        counter3Queue = new LinkedList<>();
        completeStack = new Stack<>();

        setTitle("Theme Park Ticketing System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new BorderLayout());

        // Create labels and text fields for customer input
        JLabel custNameLabel = new JLabel("Customer Name:");
        JTextField custNameField = new JTextField(10);
        JLabel counterPaidLabel = new JLabel("Tickets Purchased:");
        JTextField counterPaidField = new JTextField(10);

        // Create "Add Customer" button and its action
        JButton addButton = new JButton("Add Customer");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Retrieve customer name and purchased tickets from input fields
                    String custName = custNameField.getText();
                    int counterPaid = Integer.parseInt(counterPaidField.getText());
                    int counter = assignCounter(counterPaid);
                    // Create a new CustomerInformation object and add it to the system
                    CustomerInformation customer = new CustomerInformation(custName, counterPaid);
                    customer.setCounter(counter);
                    addCustomerToList(customer);
                    addCustomerToTable(customer);
                } catch (NumberFormatException ex) {
                    // Display an error message for invalid input
                    JOptionPane.showMessageDialog(ThemeParkTicketingSystem.this, "Invalid input. Please check your entries.");
                }
            }
        });

        // Create a panel for customer input components
        JPanel inputPanel = new JPanel();
        inputPanel.add(custNameLabel);
        inputPanel.add(custNameField);
        inputPanel.add(counterPaidLabel);
        inputPanel.add(counterPaidField);
        inputPanel.add(addButton);

        // Add the input panel to the main panel in the NORTH region
        panel.add(inputPanel, BorderLayout.NORTH);

        // Create the table for displaying customer information
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Tickets", "Counter", "Paid"}, 0);
        customerTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scrollPane = new JScrollPane(customerTable);

        // Add the table to the main panel in the CENTER region
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create a panel for buttons using GridLayout
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2)); // Use GridLayout for buttons with 1 row and 2 columns

        // Create "Process Payments" button and its action
        JButton processButton = new JButton("Process Payments");
        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processPayments();
            }
        });

        // Add the "Process Payments" button to the button panel
        buttonPanel.add(processButton);

        // Create "Show Receipt" button and its action
        JButton showReceiptButton = new JButton("Show Receipt");
        showReceiptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showReceipt();
            }
        });

        // Add the "Show Receipt" button to the button panel
        buttonPanel.add(showReceiptButton);

        // Create a bottom panel to contain the button panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Add the button panel to the bottom panel
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add the bottom panel to the main panel in the SOUTH region
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Load customer data from a text file
        loadCustomersFromTextFile("customer.txt");

        // Make the main window visible
        setVisible(true);
    }

    public void loadCustomersFromTextFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 2) {
                    // Extract customer name and purchased tickets from the line
                    String custName = parts[0].trim();
                    int counterPaid = Integer.parseInt(parts[1].trim());
                    // Determine the counter for the customer based on the number of purchased tickets
                    int counter = assignCounter(counterPaid);
                    // Create a CustomerInformation object and add it to the system
                    CustomerInformation customer = new CustomerInformation(custName, counterPaid);
                    customer.setCounter(counter);
                    addCustomerToList(customer);
                    addCustomerToTable(customer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle any IO errors by printing the exception
        }
    }

    private int calculateTotalPrice(int ticketsPurchased) {
        int ticketPrice = 15; // Define the price per ticket
        return ticketsPurchased * ticketPrice; // Calculate the total price based on the number of purchased tickets
    }

    public void addCustomerToList(CustomerInformation customer) {
        customerList.add(customer);  // Add the customer to the general customer list
        if (customer.getCounter() == 1) {
            counter1Queue.add(customer);  // Add the customer to the counter 1 queue
            counter1Count++;  // Increment the counter 1 count
        } else if (customer.getCounter() == 2) {
            counter2Queue.add(customer);  // Add the customer to the counter 2 queue
            counter2Count++;  // Increment the counter 2 count
        } else {
            counter3Queue.add(customer);  // Add the customer to the counter 3 queue
        }
    }

    public void addCustomerToTable(CustomerInformation customer) {
        tableModel.addRow(new Object[]{customer.getCustId(), customer.getCustName(), customer.getTicketsPurchased(), "Counter " + customer.getCounter(), customer.hasPaid() ? "Yes" : "No"});
        // Add a row to the customer table with the customer's information
    }

    public int assignCounter(int ticketsPurchased) {
        if (ticketsPurchased <= 5) {
            if (currentCounter == 1) {
                currentCounter = 2;
                return 1;  // Assign counter 1 if the customer purchased 5 or fewer tickets
            } else {
                currentCounter = 1;
                return 2;  // Assign counter 2 if the customer purchased 5 or fewer tickets
            }
        } else {
            return 3;  // Assign counter 3 if the customer purchased more than 5 tickets
        }
    }


    public void processPayments() {
        Queue<CustomerInformation> currentQueue = new LinkedList<>();
        int counter = currentCounter;  // Initialize the counter based on the current counter

        int count = 0;  // Initialize a count variable to keep track of processed payments

        while (count < paymentLimit) {
            if (counter == 1) {
                currentQueue = counter1Queue;  // Use the queue for counter 1 if current counter is 1
            } else if (counter == 2) {
                currentQueue = counter2Queue;  // Use the queue for counter 2 if current counter is 2
            } else {
                currentQueue = counter3Queue;  // Use the queue for counter 3 if current counter is 3
            }

            for (int i = 0; i < 5 && !currentQueue.isEmpty(); i++) {
                CustomerInformation customer = currentQueue.poll();  // Retrieve a customer from the queue

                if (!customer.hasPaid()) {  // Check if the customer has not paid yet
                    int totalPayment = customer.getTicketsPurchased() * ticketPrice;  // Calculate the total payment
                    String paymentStr = JOptionPane.showInputDialog(this, "Customer ID: " + customer.getCustId() + "\nTotal Payment: " + totalPayment, "Payment");

                    if (paymentStr != null) {  // Check if payment input is not canceled
                        try {
                            int paidAmount = Integer.parseInt(paymentStr);  // Parse the entered payment amount

                            if (paidAmount < totalPayment) {
                                JOptionPane.showMessageDialog(this, "Insufficient payment for Customer ID: " + customer.getCustId());
                                currentQueue.add(customer);  // Add the customer back to the queue
                            } else {
                                customer.markAsPaid();  // Mark the customer as paid
                                addCustomerToTable(customer);  // Add the customer's information to the table
                                completeStack.push(customer);  // Push the customer onto the completeStack
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Invalid payment amount for Customer ID: " + customer.getCustId());
                        }
                    } else {
                        currentQueue.add(customer);  // Add the customer back to the queue if payment input is canceled
                    }
                    count++;  // Increment the count of processed payments
                }
            }

            counter = (counter % 3) + 1;  // Rotate the counter (1 -> 2 -> 3 -> 1 -> 2 -> 3) to process payments in sequence
        }
        currentCounter = counter;  // Update the current counter for the next payment cycle
    }

    public void showReceipt() {
        int customersToShow = 5;  // Define the maximum number of customers to show on the receipt
        int counterToShow = receiptCounter;  // Get the counter number for which the receipt is being displayed
        int currentQueueSize = 0;  // Initialize the size of the current queue

        Queue<CustomerInformation> currentQueue;

        // Determine the current queue based on the counter to show
        if (counterToShow == 1) {
            currentQueue = counter1Queue;
            currentQueueSize = counter1Queue.size();
        } else if (counterToShow == 2) {
            currentQueue = counter2Queue;
            currentQueueSize = counter2Queue.size();
        } else {
            currentQueue = counter3Queue;
            currentQueueSize = counter3Queue.size();
        }

        if (currentQueueSize > 0) {  // Check if there are customers in the current queue
            JFrame receiptFrame = new JFrame("Receipts for Counter " + counterToShow);  // Create a new JFrame for the receipt
            JPanel receiptPanel = new JPanel();
            receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));  // Create a panel with a vertical layout

            JLabel title = new JLabel("Receipts for Counter " + counterToShow);  // Create a title label
            title.setFont(new Font("Arial", Font.BOLD, 24));  // Set the font for the title
            title.setAlignmentX(Component.CENTER_ALIGNMENT);  // Center-align the title label
            receiptPanel.add(title);  // Add the title label to the receipt panel

            receiptPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Add some spacing

            // Iterate through a limited number of customers and display their information on the receipt
            for (int i = 0; i < Math.min(customersToShow, currentQueueSize); i++) {
                CustomerInformation customer = currentQueue.poll();

                JLabel customerIdLabel = new JLabel("Customer ID: " + customer.getCustId());
                customerIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                receiptPanel.add(customerIdLabel);  // Add customer ID to the receipt

                JLabel customerNameLabel = new JLabel("Name: " + customer.getCustName());
                customerNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                receiptPanel.add(customerNameLabel);  // Add customer name to the receipt

                JLabel ticketsLabel = new JLabel("Tickets: " + customer.getTicketsPurchased());
                ticketsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                receiptPanel.add(ticketsLabel);  // Add ticket information to the receipt

                JLabel totalPriceLabel = new JLabel("Total Price (RM): " + calculateTotalPrice(customer.getTicketsPurchased()));
                totalPriceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                receiptPanel.add(totalPriceLabel);  // Add the total price to the receipt

                receiptPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Add spacing between customers
            }

            receiptFrame.add(receiptPanel);  // Add the receipt panel to the receipt frame
            receiptFrame.setSize(400, 500);  // Set the size of the receipt frame
            receiptFrame.setLocationRelativeTo(null);  // Center the receipt frame on the screen
            receiptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Set the default close operation for the frame
            receiptFrame.setVisible(true);  // Make the receipt frame visible

            // Update the receipt counter to cycle through counters (1 -> 2 -> 3 -> 1 -> 2 -> 3)
            receiptCounter = (receiptCounter % 3) + 1;
        } else {
            JOptionPane.showMessageDialog(null, "No customers to show receipts for Counter " + counterToShow + ".", "Information", JOptionPane.INFORMATION_MESSAGE);  // Display an information dialog if there are no customers in the queue
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ThemeParkTicketingSystem();
        });
    }
}