package GUI; 

import Control.systemDataBase;
import Model.Subscription;
import Model.Order;
import Model.Taxi;
import Model.ExpressTaxi;
import Model.IntercityTaxi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;

public class SubscriberPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private Subscription currentSubscriber;
    private MainApplicationFrame parentFrame; 

    private CardLayout cardLayout;
    private JPanel cardsPanel; // The panel that holds the different views

    // --- Components for "My Orders" View ---
    private JTextArea orderDetailsArea;

    // --- Components for "Update Details" View ---
    private JLabel currentPhoneLabel;
    private JLabel currentAddressLabel;
    private JTextField newPhoneField;
    private JTextField newAddressField;
    private JButton updateDetailsButton;

    // --- Components for "Show Taxi Details" View ---
    private JComboBox<String> taxiComboBox;
    private JTextArea taxiDetailsArea;
    
    // --- Navigation Buttons ---
    private JButton myOrdersButton;
    private JButton updateDetailsNavButton;
    private JButton viewTaxisButton;
    private JButton logoutButton;

    public SubscriberPanel(systemDataBase system, MainApplicationFrame parentFrame, Subscription subscriber) {
        this.ourSystem = system;
        this.parentFrame = parentFrame;
        this.currentSubscriber = subscriber;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- New, Beautiful Welcome Header ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titleContent = new JPanel();
        titleContent.setLayout(new BoxLayout(titleContent, BoxLayout.Y_AXIS));
        titleContent.setOpaque(false);

        JLabel welcomeTitle = new JLabel("Subscriber Dashboard"); // User Icon
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel welcomeSubtitle = new JLabel("Welcome, " + currentSubscriber.getFirstName() + " " + currentSubscriber.getLastName() + "!");
        welcomeSubtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        welcomeSubtitle.setForeground(Color.DARK_GRAY);
        welcomeSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titleContent.add(welcomeTitle);
        titleContent.add(Box.createRigidArea(new Dimension(0, 5)));
        titleContent.add(welcomeSubtitle);
        
        headerPanel.add(titleContent);
        add(headerPanel, BorderLayout.NORTH);

        // --- Navigation Panel (Left) ---
        JPanel navPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        myOrdersButton = new JButton("My Orders");
        updateDetailsNavButton = new JButton("Update My Details");
        viewTaxisButton = new JButton("View Taxi Details");
        logoutButton = new JButton("Logout");

        JButton[] buttons = {myOrdersButton, updateDetailsNavButton, viewTaxisButton};
        for(JButton btn : buttons) {
            btn.addActionListener(this);
            navPanel.add(btn, gbc);
        }
        gbc.weighty = 1; // Push buttons to the top
        navPanel.add(Box.createVerticalGlue(), gbc);

        gbc.weighty = 0;
        logoutButton.addActionListener(this);
        navPanel.add(logoutButton, gbc);

        add(navPanel, BorderLayout.WEST);

        // --- Content Panel with CardLayout (Center) ---
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        
        cardsPanel.add(createMyOrdersPanel(), "MyOrders");
        cardsPanel.add(createUpdateDetailsPanel(), "UpdateDetails");
        cardsPanel.add(createShowTaxiDetailsPanel(), "ShowTaxis");
        
        add(cardsPanel, BorderLayout.CENTER);
        
        cardLayout.show(cardsPanel, "MyOrders"); // Show "My Orders" by default
    }
    
    // --- Panel Creation Methods ---

    private JPanel createMyOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Your Order History"));
        orderDetailsArea = new JTextArea();
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(orderDetailsArea);
        displayOrders();
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUpdateDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel currentInfoPanel = new JPanel(new GridBagLayout());
        currentInfoPanel.setBorder(BorderFactory.createTitledBorder("Current Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; currentInfoPanel.add(new JLabel("Subscriber Code:"), gbc);
        gbc.gridx = 1; currentInfoPanel.add(new JLabel(currentSubscriber.getSubCode()), gbc);
        gbc.gridx = 0; gbc.gridy = 1; currentInfoPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; currentInfoPanel.add(new JLabel(currentSubscriber.getFirstName() + " " + currentSubscriber.getLastName()), gbc);
        gbc.gridx = 0; gbc.gridy = 2; currentInfoPanel.add(new JLabel("Current Phone:"), gbc);
        gbc.gridx = 1; currentPhoneLabel = new JLabel(currentSubscriber.getPhone()); currentInfoPanel.add(currentPhoneLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 3; currentInfoPanel.add(new JLabel("Current Address:"), gbc);
        gbc.gridx = 1; currentAddressLabel = new JLabel(currentSubscriber.getAddress()); currentInfoPanel.add(currentAddressLabel, gbc);

        JPanel editableFormPanel = new JPanel(new GridBagLayout());
        editableFormPanel.setBorder(BorderFactory.createTitledBorder("Edit Details"));

        gbc.gridx = 0; gbc.gridy = 0; editableFormPanel.add(new JLabel("New Phone:"), gbc);
        gbc.gridx = 1; newPhoneField = new JTextField(currentSubscriber.getPhone(), 20); editableFormPanel.add(newPhoneField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; editableFormPanel.add(new JLabel("New Address:"), gbc);
        gbc.gridx = 1; newAddressField = new JTextField(currentSubscriber.getAddress(), 20); editableFormPanel.add(newAddressField, gbc);

        ((AbstractDocument) newPhoneField.getDocument()).setDocumentFilter(new InputFilter(10, "DIGITS_ONLY"));
        ((AbstractDocument) newAddressField.getDocument()).setDocumentFilter(new InputFilter(30, "ANY"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        updateDetailsButton = new JButton("Update Details");
        updateDetailsButton.addActionListener(this);
        buttonPanel.add(updateDetailsButton);
        
        panel.add(currentInfoPanel, BorderLayout.NORTH);
        panel.add(editableFormPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createShowTaxiDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Taxi:"));
        taxiComboBox = new JComboBox<>();
        taxiComboBox.addActionListener(this);
        selectionPanel.add(taxiComboBox);
        panel.add(selectionPanel, BorderLayout.NORTH);

        taxiDetailsArea = new JTextArea();
        taxiDetailsArea.setEditable(false);
        taxiDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(taxiDetailsArea), BorderLayout.CENTER);

        populateTaxisComboBox();
        displaySelectedTaxiDetails();

        return panel;
    }
    
    // --- Data Population & Display Logic ---

    private void displayOrders() {
        ArrayList<Order> ordersOfSub = ourSystem.getOrdersPerSub().get(currentSubscriber.getSubCode());
        if (ordersOfSub != null && !ordersOfSub.isEmpty()) {
            StringBuilder sb = new StringBuilder("Your Order History:\n\n");
            for (Order order : ordersOfSub) {
                sb.append("Order Number: ").append(order.getOrderNum()).append("\n");
                sb.append("  Manager: ").append(order.getManagerCode()).append("\n");
                sb.append("  Date: ").append(order.getDay()).append("/").append(order.getMonth()).append(" at ").append(order.getHour()).append(":00\n");
                sb.append("  Taxi: ").append(order.getTaxi().getTaxiCode()).append("\n");
                sb.append("  Order Price: ").append(String.format("%.2f", order.getOrderPrice())).append("\n");
                sb.append("--------------------------------------------------\n");
            }
            orderDetailsArea.setText(sb.toString());
        } else {
            orderDetailsArea.setText("No orders found.");
        }
    }

    private void populateTaxisComboBox() {
        taxiComboBox.removeAllItems();
        if (ourSystem.getTaxis().isEmpty()) {
            taxiComboBox.addItem("No taxis available");
            return;
        }
        ourSystem.getTaxis().stream()
            .sorted(Comparator.comparing(t -> Integer.parseInt(t.getTaxiCode().substring(1))))
            .forEach(t -> taxiComboBox.addItem(t.getTaxiCode()));
    }

    private void displaySelectedTaxiDetails() {
        String selectedTaxiCode = (String) taxiComboBox.getSelectedItem();
        if (selectedTaxiCode == null || selectedTaxiCode.contains("No taxis")) {
            taxiDetailsArea.setText("Please select a taxi to view details.");
            return;
        }

        Taxi foundTaxi = ourSystem.getTaxis().stream()
            .filter(t -> t.getTaxiCode().equals(selectedTaxiCode))
            .findFirst().orElse(null);

        if (foundTaxi != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("--- Taxi Details ---\n");
            sb.append(String.format("  Taxi Code:    %s\n", foundTaxi.getTaxiCode()));
            sb.append(String.format("  Available:    %s\n", foundTaxi.isAvailable() ? "Yes" : "No"));
            sb.append(String.format("  Min Price:    %.2f\n", foundTaxi.getMinPrice()));

            if (foundTaxi instanceof ExpressTaxi) {
                ExpressTaxi et = (ExpressTaxi) foundTaxi;
                sb.append("  Type:         Express Taxi\n");
                sb.append(String.format("  City Taxi:    %s\n", et.isCityTaxi() ? "Yes" : "No"));
                sb.append(String.format("  Extra Price:  %.2f\n", et.getExtraPrice()));
            } else if (foundTaxi instanceof IntercityTaxi) {
                IntercityTaxi it = (IntercityTaxi) foundTaxi;
                sb.append("  Type:         Intercity Taxi\n");
                sb.append(String.format("  Cities:       %s\n", String.join(", ", it.getCities())));
                sb.append(String.format("  Extra Price:  %.2f\n", it.getExtraPrice()));
                sb.append(String.format("  Max Hours:    %d\n", it.getMaxHours()));
            } else {
                sb.append("  Type:         Regular Taxi\n");
            }
            sb.append("--------------------");
            taxiDetailsArea.setText(sb.toString());
        } else {
            taxiDetailsArea.setText("Error: Taxi not found.");
        }
    }
    
    private boolean validateDetails(String phone, String address) {
        if (phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!phone.matches("0\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Phone must be 10 digits, starting with 0.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // --- Navigation Button Actions ---
        if (source == myOrdersButton) {
            cardLayout.show(cardsPanel, "MyOrders");
        } else if (source == updateDetailsNavButton) {
            cardLayout.show(cardsPanel, "UpdateDetails");
        } else if (source == viewTaxisButton) {
            cardLayout.show(cardsPanel, "ShowTaxis");
        } else if (source == logoutButton) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                parentFrame.showPanel(MainApplicationFrame.LOGIN_PANEL, null);
            }
        }
        // --- Form Action Events ---
        else if (source == updateDetailsButton) {
            String newPhone = newPhoneField.getText().trim();
            String newAddress = newAddressField.getText().trim();

            if (!validateDetails(newPhone, newAddress)) {
                return;
            }
            
            if (!newPhone.equals(currentSubscriber.getPhone()) || !newAddress.equals(currentSubscriber.getAddress())) {
                currentSubscriber.setPhone(newPhone);
                currentSubscriber.setAddress(newAddress);
                
                currentPhoneLabel.setText(newPhone);
                currentAddressLabel.setText(newAddress);

                JOptionPane.showMessageDialog(this, "Details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No changes were detected.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (source == taxiComboBox) {
            displaySelectedTaxiDetails();
        }
    }
}