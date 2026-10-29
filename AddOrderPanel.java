package GUI;

import Control.systemDataBase;
import Model.Manager;
import Model.Order;
import Model.Subscription;
import Model.Taxi;
import Model.ExpressTaxi;
import Model.IntercityTaxi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AddOrderPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JTextField orderIdField;
    private JComboBox<String> subscriberComboBox;
    private JComboBox<String> taxiComboBox;
    private JTextField dayField;
    private JTextField monthField;
    private JTextField hourField;
    private JButton addButton;
    private JButton backButton;

    private systemDataBase ourSystem;
    private Manager currentManager;
    private JPanel parentPanel; 

    public AddOrderPanel(systemDataBase system, Manager manager, JPanel parentPanel) {
        this.ourSystem = system;
        this.currentManager = manager;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Add New Order", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10)); // 6 rows for fields

        formPanel.add(new JLabel("Order ID:"));
        orderIdField = new JTextField();
        formPanel.add(orderIdField);

        formPanel.add(new JLabel("Select Subscriber:"));
        subscriberComboBox = new JComboBox<>();
        formPanel.add(subscriberComboBox);

        formPanel.add(new JLabel("Select Taxi (Available Only):"));
        taxiComboBox = new JComboBox<>();
        formPanel.add(taxiComboBox);

        formPanel.add(new JLabel("Day (1-31):"));
        dayField = new JTextField();
        formPanel.add(dayField);

        formPanel.add(new JLabel("Month (1-12):"));
        monthField = new JTextField();
        formPanel.add(monthField);

        formPanel.add(new JLabel("Hour (0-23):"));
        hourField = new JTextField();
        formPanel.add(hourField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Add Order");
        addButton.addActionListener(this);
        buttonPanel.add(addButton);

        backButton = new JButton("Back");
        backButton.addActionListener(this);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        populateSubscribers();
        populateTaxis();
        updateAddButtonState(); 
    }

    private void populateSubscribers() {
        subscriberComboBox.removeAllItems();
        if (ourSystem.getSubscriptions().isEmpty()) {
            subscriberComboBox.addItem("No subscribers available");
            updateAddButtonState();
            return;
        }
        for (Subscription sub : ourSystem.getSubscriptions()) {
            subscriberComboBox.addItem(sub.getSubCode() + " - " + sub.getFirstName() + " " + sub.getLastName());
        }
        updateAddButtonState();
    }

    private void populateTaxis() {
        taxiComboBox.removeAllItems();
        ArrayList<Taxi> availableTaxis = new ArrayList<>();
        for (Taxi taxi : ourSystem.getTaxis()) {
            if (taxi.isAvailable()) {
                availableTaxis.add(taxi);
            }
        }

        if (availableTaxis.isEmpty()) {
            taxiComboBox.addItem("No available taxis");
            updateAddButtonState();
            return;
        }

        for (Taxi taxi : availableTaxis) {
            taxiComboBox.addItem(taxi.getTaxiCode() + " - " + taxi.getMinPrice() + " (" + (taxi instanceof ExpressTaxi ? "Express" : (taxi instanceof IntercityTaxi ? "Intercity" : "Regular")) + ")");
        }
        updateAddButtonState();
    }

    private void updateAddButtonState() {
        boolean enable = (subscriberComboBox.getItemCount() > 0 && !subscriberComboBox.getSelectedItem().equals("No subscribers available")) &&
                         (taxiComboBox.getItemCount() > 0 && !taxiComboBox.getSelectedItem().equals("No available taxis"));
        addButton.setEnabled(enable);
    }

    // Helper method to check if a manager is responsible for a taxi
    private boolean isManagerResponsibleForTaxi(Manager manager, Taxi taxi) {
        if (manager == null || taxi == null) {
            return false;
        }
        for (Taxi responsibleTaxi : manager.getTaxis()) {
            if (responsibleTaxi.getTaxiCode().equals(taxi.getTaxiCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            String orderId = orderIdField.getText();
            String selectedSubInfo = (String) subscriberComboBox.getSelectedItem();
            String selectedTaxiInfo = (String) taxiComboBox.getSelectedItem();

            if (orderId.isEmpty() || selectedSubInfo == null || selectedTaxiInfo == null || selectedSubInfo.equals("No subscribers available") || selectedTaxiInfo.equals("No available taxis")) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields and select available options.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int day, month, hour;
            try {
                day = Integer.parseInt(dayField.getText());
                month = Integer.parseInt(monthField.getText());
                hour = Integer.parseInt(hourField.getText());

                if (day < 1 || day > 31 || month < 1 || month > 12 || hour < 0 || hour > 23) {
                    throw new NumberFormatException("Invalid date/time range.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for Day (1-31), Month (1-12), and Hour (0-23).", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String subCode = selectedSubInfo.split(" - ")[0];
            String taxiCode = selectedTaxiInfo.split(" - ")[0];

            Subscription selectedSub = null;
            for (Subscription sub : ourSystem.getSubscriptions()) {
                if (sub.getSubCode().equals(subCode)) {
                    selectedSub = sub;
                    break;
                }
            }

            Taxi selectedTaxi = null;
            for (Taxi taxi : ourSystem.getTaxis()) {
                if (taxi.getTaxiCode().equals(taxiCode)) {
                    selectedTaxi = taxi;
                    break;
                }
            }

            if (selectedSub == null || selectedTaxi == null) {
                JOptionPane.showMessageDialog(this, "Subscriber or Taxi not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!selectedTaxi.isAvailable()) {
                JOptionPane.showMessageDialog(this, "Selected taxi is not available. Please refresh and select another.", "Error", JOptionPane.ERROR_MESSAGE);
                populateTaxis();
                return;
            }

            // --- MANAGER RESPONSIBILITY CHECK ---
            if (!isManagerResponsibleForTaxi(currentManager, selectedTaxi)) {
                JOptionPane.showMessageDialog(this, "You are not responsible for this taxi. Order cannot be added.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // --- END RESPONSIBILITY CHECK ---

            Order newOrder = new Order(orderId, currentManager.getId(), day, month, hour, subCode, selectedTaxi, selectedTaxi.getMinPrice());

            if (ourSystem.addOrder(newOrder)) {
                selectedTaxi.setAvailable(false);
                JOptionPane.showMessageDialog(this, "Order " + orderId + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Navigate back based on parent type
                if (parentPanel instanceof MainManagerPanel) {
                    ((MainManagerPanel) parentPanel).showSubPanel(MainManagerPanel.WELCOME_PANEL);
                } else if (parentPanel instanceof ManagerPanel) {
                    // For a regular manager, the "dashboard" is the ManagerPanel itself.
                    // We get the top-level frame and tell it to show the ManagerPanel.
                    MainApplicationFrame frame = (MainApplicationFrame) SwingUtilities.getWindowAncestor(this);
                    if (frame != null) {
                        frame.showPanel(MainApplicationFrame.MANAGER_PANEL, currentManager);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add order. Order ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == backButton) {
            // Navigate back based on parent type
            if (parentPanel instanceof MainManagerPanel) {
                ((MainManagerPanel) parentPanel).showSubPanel(MainManagerPanel.WELCOME_PANEL);
            } else if (parentPanel instanceof ManagerPanel) {
                 MainApplicationFrame frame = (MainApplicationFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) {
                    frame.showPanel(MainApplicationFrame.MANAGER_PANEL, currentManager);
                }
            }
        }
    }
}