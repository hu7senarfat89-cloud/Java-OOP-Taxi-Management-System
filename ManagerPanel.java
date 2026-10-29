package GUI;

import Control.systemDataBase;
import Model.Manager;
import Model.Order;
import Model.Taxi;
import Model.Subscription;
import Model.ExpressTaxi;
import Model.IntercityTaxi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private Manager currentManager;
    private MainApplicationFrame parentFrame; 

    private JTable table;
    private DefaultTableModel model;
    
    // Details/Form fields
    private JTextField detailsOrderIdField;
    private JComboBox<String> detailsSubscriberComboBox;
    private JComboBox<String> detailsTaxiComboBox;
    private JTextField detailsDayField;
    private JTextField detailsMonthField;
    private JTextField detailsHourField;
    private JTextField detailsOrderPriceField;
    private JLabel currentTaxiLabel;

    // Action buttons
    private JButton addNewOrderButton;
    private JButton editOrderButton;
    private JButton changeTaxiButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton removeOrderButton;
    private JButton logoutButton; // NEW LOGOUT BUTTON

    private int selectedRow = -1;
    private boolean isAddingMode = false;
    private boolean isChangingTaxiMode = false;
    private boolean isEditingMode = false;

    public ManagerPanel(systemDataBase system, MainApplicationFrame parentFrame, Manager manager) {
        this.ourSystem = system;
        this.parentFrame = parentFrame;
        this.currentManager = manager;
        setLayout(new BorderLayout(10, 10)); 
        setBorder(new EmptyBorder(10,10,10,10));

        // --- New, Beautiful Welcome Header ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titleContent = new JPanel();
        titleContent.setLayout(new BoxLayout(titleContent, BoxLayout.Y_AXIS));
        titleContent.setOpaque(false);

        JLabel welcomeTitle = new JLabel("Manager Dashboard");
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel welcomeSubtitle = new JLabel("Welcome, " + currentManager.getFirstName() + "! Here are the orders you are managing.");
        welcomeSubtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        welcomeSubtitle.setForeground(Color.DARK_GRAY);
        welcomeSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titleContent.add(welcomeTitle);
        titleContent.add(Box.createRigidArea(new Dimension(0, 5)));
        titleContent.add(welcomeSubtitle);
        
        headerPanel.add(titleContent);
        add(headerPanel, BorderLayout.NORTH);

        // --- Table View ---
        String[] columnNames = {"Order ID", "Subscriber", "Taxi Code", "Date", "Time", "Price"};
        model = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
			@Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // --- Details Section & Form ---
        JPanel detailsPanel = createFormPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsPanel);
        splitPane.setResizeWeight(0.5); 
        add(splitPane, BorderLayout.CENTER); 
        
        // --- Logout Button Panel ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this);
        southPanel.add(logoutButton);
        add(southPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isAddingMode && !isChangingTaxiMode && !isEditingMode) {
                selectedRow = table.getSelectedRow();
                resetToViewState();
                if (selectedRow != -1) {
                    populateDetailsFromTable();
                    editOrderButton.setEnabled(true);
                    changeTaxiButton.setEnabled(canChangeTaxi());
                    removeOrderButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                }
            }
        });

        resetToInitialState();
        populateTable();
    }
    
    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        formContainer.setBorder(BorderFactory.createTitledBorder("Order Details / Input"));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; fieldsPanel.add(new JLabel("Order ID:"), gbc);
        gbc.gridx = 1; detailsOrderIdField = new JTextField(20); fieldsPanel.add(detailsOrderIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; fieldsPanel.add(new JLabel("Subscriber:"), gbc);
        gbc.gridx = 1; detailsSubscriberComboBox = new JComboBox<>(); fieldsPanel.add(detailsSubscriberComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; fieldsPanel.add(new JLabel("Taxi:"), gbc);
        gbc.gridx = 1; detailsTaxiComboBox = new JComboBox<>(); fieldsPanel.add(detailsTaxiComboBox, gbc);
        gbc.gridx = 2; currentTaxiLabel = new JLabel(); fieldsPanel.add(currentTaxiLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; fieldsPanel.add(new JLabel("Date (DD/MM):"), gbc);
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        detailsDayField = new JTextField(3);
        detailsMonthField = new JTextField(3);
        datePanel.add(detailsDayField);
        datePanel.add(new JLabel("/"));
        datePanel.add(detailsMonthField);
        gbc.gridx = 1; fieldsPanel.add(datePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 4; fieldsPanel.add(new JLabel("Hour (0-23):"), gbc);
        gbc.gridx = 1; detailsHourField = new JTextField(5); fieldsPanel.add(detailsHourField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; fieldsPanel.add(new JLabel("Order Price:"), gbc);
        gbc.gridx = 1; detailsOrderPriceField = new JTextField(10); fieldsPanel.add(detailsOrderPriceField, gbc);
        
        ((AbstractDocument) detailsDayField.getDocument()).setDocumentFilter(new RangeInputFilter(31));
        ((AbstractDocument) detailsMonthField.getDocument()).setDocumentFilter(new RangeInputFilter(12));
        ((AbstractDocument) detailsHourField.getDocument()).setDocumentFilter(new RangeInputFilter(23));
        ((AbstractDocument) detailsOrderPriceField.getDocument()).setDocumentFilter(new DecimalInputFilter());

        formContainer.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addNewOrderButton = new JButton("Add New Order");
        editOrderButton = new JButton("Edit Order");
        changeTaxiButton = new JButton("Change Taxi");
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        removeOrderButton = new JButton("Remove Order");
        
        JButton[] buttons = {addNewOrderButton, editOrderButton, changeTaxiButton, saveButton, cancelButton, removeOrderButton};
        for(JButton btn : buttons) {
            btn.addActionListener(this);
            buttonPanel.add(btn);
        }
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        return formContainer;
    }
    
    private void resetToInitialState() {
        isAddingMode = false;
        isChangingTaxiMode = false;
        isEditingMode = false;
        table.clearSelection();
        clearFormFields();
        setFormEnabled(false);
        
        addNewOrderButton.setEnabled(true);
        editOrderButton.setEnabled(false);
        changeTaxiButton.setEnabled(false);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        removeOrderButton.setEnabled(false);
    }
    
    private void resetToViewState() {
        isAddingMode = false;
        isChangingTaxiMode = false;
        isEditingMode = false;
        clearFormFields();
        setFormEnabled(false);
        
        addNewOrderButton.setEnabled(true);
        editOrderButton.setEnabled(false);
        changeTaxiButton.setEnabled(false);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        removeOrderButton.setEnabled(false);
    }
    
    private void setFormEnabled(boolean enabled) {
        detailsOrderIdField.setEnabled(false);
        detailsSubscriberComboBox.setEnabled(isAddingMode && enabled);
        detailsTaxiComboBox.setEnabled(isAddingMode || isChangingTaxiMode);
        detailsDayField.setEnabled(isAddingMode || isEditingMode);
        detailsMonthField.setEnabled(isAddingMode || isEditingMode);
        detailsHourField.setEnabled(isAddingMode || isEditingMode);
        detailsOrderPriceField.setEnabled(isAddingMode || isEditingMode);
    }
    
    private void clearFormFields() {
        detailsOrderIdField.setText("");
        detailsSubscriberComboBox.setSelectedIndex(-1);
        detailsTaxiComboBox.removeAllItems();
        detailsDayField.setText("");
        detailsMonthField.setText("");
        detailsHourField.setText("");
        detailsOrderPriceField.setText("");
        currentTaxiLabel.setText("");
    }
    
    private void populateTable() {
        model.setRowCount(0); 
        ArrayList<Order> ordersForTable = ourSystem.getOrders().stream()
            .filter(o -> o.getManagerCode().equals(currentManager.getId()))
            .sorted(Comparator.comparing(Order::getOrderNum))
            .collect(Collectors.toCollection(ArrayList::new));

        for (Order order : ordersForTable) {
            String subName = ourSystem.getSubscriptions().stream()
                .filter(s -> s.getSubCode().equals(order.getSubCode()))
                .findFirst()
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .orElse("N/A");
            model.addRow(new Object[]{
                order.getOrderNum(), subName, order.getTaxi().getTaxiCode(),
                String.format("%02d/%02d", order.getDay(), order.getMonth()),
                String.format("%02d:00", order.getHour()),
                String.format("%.2f", order.getOrderPrice())
            });
        }
    }

    private void populateDetailsFromTable() {
        if (selectedRow != -1) {
            String orderId = model.getValueAt(selectedRow, 0).toString();
            Order order = findOrderById(orderId);
            if (order == null) return; 

            detailsOrderIdField.setText(order.getOrderNum());
            populateSubscribersComboBox();
            if (detailsSubscriberComboBox.getItemCount() > 0) {
                for (int i = 0; i < detailsSubscriberComboBox.getItemCount(); i++) {
                    if (detailsSubscriberComboBox.getItemAt(i).startsWith(order.getSubCode())) {
                        detailsSubscriberComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            detailsTaxiComboBox.removeAllItems();
            detailsTaxiComboBox.addItem(order.getTaxi().getTaxiCode() + " - (" + getTaxiType(order.getTaxi()) + ")");
            
            detailsDayField.setText(String.valueOf(order.getDay()));
            detailsMonthField.setText(String.valueOf(order.getMonth()));
            detailsHourField.setText(String.valueOf(order.getHour()));
            detailsOrderPriceField.setText(String.format("%.2f", order.getOrderPrice()));
        }
    }
    
    private void populateSubscribersComboBox() {
        detailsSubscriberComboBox.removeAllItems();
        ArrayList<Subscription> subs = ourSystem.getSubscriptions();
        if (subs.isEmpty()) {
            detailsSubscriberComboBox.addItem("No subscribers in system");
        } else {
            subs.forEach(sub -> 
                detailsSubscriberComboBox.addItem(sub.getSubCode() + " - " + sub.getFirstName() + " " + sub.getLastName())
            );
        }
    }

    private String getTaxiType(Taxi taxi) {
        if (taxi instanceof ExpressTaxi) return "Express";
        if (taxi instanceof IntercityTaxi) return "Intercity";
        return "Regular";
    }

    private void populateTaxisForAdd() {
        detailsTaxiComboBox.removeAllItems();
        List<Taxi> availableTaxis = ourSystem.getTaxis().stream()
            .filter(t -> t.isAvailable() && isManagerResponsibleForTaxi(currentManager, t))
            .collect(Collectors.toList());
        
        if (availableTaxis.isEmpty()) {
            detailsTaxiComboBox.addItem("No taxis available for " + currentManager.getFirstName());
        } else {
            availableTaxis.forEach(t -> detailsTaxiComboBox.addItem(t.getTaxiCode() + " - (" + getTaxiType(t) + ")"));
        }
    }

    private void populateTaxisForChange(Taxi oldTaxi) {
        detailsTaxiComboBox.removeAllItems();
        currentTaxiLabel.setText("Current: " + oldTaxi.getTaxiCode() + " (" + getTaxiType(oldTaxi) + ")");
        
        List<Taxi> suitableTaxis = ourSystem.getTaxis().stream()
            .filter(t -> t.isAvailable() && (t instanceof ExpressTaxi || t instanceof IntercityTaxi) && isManagerResponsibleForTaxi(currentManager, t))
            .collect(Collectors.toList());

        if (suitableTaxis.isEmpty()) {
            detailsTaxiComboBox.addItem("No suitable replacement taxis available");
        } else {
            suitableTaxis.forEach(t -> detailsTaxiComboBox.addItem(t.getTaxiCode() + " - (" + getTaxiType(t) + ")"));
        }
    }
    
    private String generateNewOrderId() {
        return "O" + (ourSystem.getOrders().stream()
            .map(o -> o.getOrderNum().substring(1))
            .mapToInt(Integer::parseInt)
            .max().orElse(0) + 1);
    }
    
    private boolean isManagerResponsibleForTaxi(Manager manager, Taxi taxi) {
        return manager.getTaxis().stream().anyMatch(t -> t.getTaxiCode().equals(taxi.getTaxiCode()));
    }
    
    private boolean canChangeTaxi() {
        if (selectedRow == -1) return false;
        Order order = findOrderById(model.getValueAt(selectedRow, 0).toString());
        return order != null && !(order.getTaxi() instanceof ExpressTaxi) && !(order.getTaxi() instanceof IntercityTaxi);
    }
    
    private Order findOrderById(String orderId) {
        return ourSystem.getOrders().stream().filter(o -> o.getOrderNum().equals(orderId)).findFirst().orElse(null);
    }
    
    private Taxi findTaxiById(String taxiId) {
        return ourSystem.getTaxis().stream().filter(t -> t.getTaxiCode().equals(taxiId)).findFirst().orElse(null);
    }
    
    private boolean validateInput() {
        String dayStr = detailsDayField.getText();
        String monthStr = detailsMonthField.getText();
        String hourStr = detailsHourField.getText();
        String priceStr = detailsOrderPriceField.getText();

        if (dayStr.isEmpty() || monthStr.isEmpty() || hourStr.isEmpty() || (isAddingMode || isEditingMode) && priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int day, month;
        try {
            day = Integer.parseInt(dayStr);
            month = Integer.parseInt(monthStr);
            if(isAddingMode || isEditingMode) Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for date, hour, and price.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (day < 1 || month < 1 || month > 12 || day > 31) {
             JOptionPane.showMessageDialog(this, "Please enter a valid day and month.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
             JOptionPane.showMessageDialog(this, "This month only has 30 days.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        if (month == 2 && day > 29) {
             JOptionPane.showMessageDialog(this, "February has at most 29 days.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == addNewOrderButton) {
            isAddingMode = true;
            isEditingMode = false;
            table.clearSelection();
            clearFormFields();
            setFormEnabled(true);
            detailsOrderIdField.setText(generateNewOrderId());
            populateSubscribersComboBox();
            populateTaxisForAdd();
            
            saveButton.setText("Add Order");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addNewOrderButton.setEnabled(false);
            editOrderButton.setEnabled(false);
            changeTaxiButton.setEnabled(false);
            removeOrderButton.setEnabled(false);
        }
        else if (source == editOrderButton) {
            isEditingMode = true;
            isAddingMode = false;
            setFormEnabled(true);
            saveButton.setText("Save Changes");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addNewOrderButton.setEnabled(false);
            editOrderButton.setEnabled(false);
            changeTaxiButton.setEnabled(false);
            removeOrderButton.setEnabled(false);
        }
        else if(source == changeTaxiButton) {
            isChangingTaxiMode = true;
            isEditingMode = false;
            setFormEnabled(false);
            detailsTaxiComboBox.setEnabled(true);
            populateTaxisForChange(findOrderById(detailsOrderIdField.getText()).getTaxi());
            
            saveButton.setText("Save Changes");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addNewOrderButton.setEnabled(false);
            editOrderButton.setEnabled(false);
            changeTaxiButton.setEnabled(false);
            removeOrderButton.setEnabled(false);
        }
        else if(source == saveButton) {
            if (!validateInput()) {
                return;
            }
            
            String orderId = detailsOrderIdField.getText();
            String subInfo = (String) detailsSubscriberComboBox.getSelectedItem();
            String taxiInfo = (String) detailsTaxiComboBox.getSelectedItem();
            
            if (subInfo == null || taxiInfo == null || subInfo.contains("No subscribers") || taxiInfo.contains("No taxis")) {
                JOptionPane.showMessageDialog(this, "Please ensure a subscriber and taxi are available.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int day = Integer.parseInt(detailsDayField.getText());
            int month = Integer.parseInt(detailsMonthField.getText());
            int hour = Integer.parseInt(detailsHourField.getText());

            String subCode = subInfo.split(" - ")[0];
            String taxiCode = taxiInfo.split(" - ")[0];
            Taxi newTaxi = findTaxiById(taxiCode);

            if(isAddingMode) {
                double price = (detailsOrderPriceField.getText().isEmpty()) ? newTaxi.getMinPrice() : Double.parseDouble(detailsOrderPriceField.getText());
                Order newOrder = new Order(orderId, currentManager.getId(), day, month, hour, subCode, newTaxi, price);
                if (ourSystem.addOrder(newOrder)) {
                    newTaxi.setAvailable(false);
                    JOptionPane.showMessageDialog(this, "Order added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add order. ID might exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (isEditingMode) {
                Order orderToChange = findOrderById(orderId);
                orderToChange.setDay(day);
                orderToChange.setMonth(month);
                orderToChange.setHour(hour);
                orderToChange.setOrderPrice(Double.parseDouble(detailsOrderPriceField.getText()));
                JOptionPane.showMessageDialog(this, "Order details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            else if (isChangingTaxiMode) {
                Order orderToChange = findOrderById(orderId);
                Taxi oldTaxi = orderToChange.getTaxi();
                
                orderToChange.setTaxi(newTaxi);
                orderToChange.setOrderPrice(newTaxi.getMinPrice());
                oldTaxi.setAvailable(true);
                newTaxi.setAvailable(false);
                
                ourSystem.getTaxisPerSub().get(subCode).remove(oldTaxi);
                ourSystem.getTaxisPerSub().get(subCode).add(newTaxi);
                JOptionPane.showMessageDialog(this, "Taxi for order " + orderId + " changed!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            populateTable();
            resetToInitialState();
        }
        else if (source == cancelButton) {
            resetToInitialState();
        }
        else if (source == removeOrderButton) {
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this order?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Order orderToRemove = findOrderById(model.getValueAt(selectedRow, 0).toString());
                    if (ourSystem.removeOrder(orderToRemove)) {
                        JOptionPane.showMessageDialog(this, "Order removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove order.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    populateTable();
                    resetToInitialState();
                }
            }
        }
        else if(source == logoutButton) {
            if(JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                parentFrame.showPanel(MainApplicationFrame.LOGIN_PANEL, null);
            }
        }
    }
}

// Custom DocumentFilter to restrict input to a numeric range
class RangeInputFilter extends DocumentFilter {
    private int maxValue;

    public RangeInputFilter(int max) {
        this.maxValue = max;
    }

    private boolean isValid(String newText) {
        if (newText.isEmpty()) return true;
        if (!newText.chars().allMatch(Character::isDigit)) return false;
        try {
            int val = Integer.parseInt(newText);
            return val <= maxValue;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void handleUpdate(FilterBypass fb, int offset, int length, String text) throws BadLocationException {
        int currentLength = fb.getDocument().getLength();
        String currentContent = fb.getDocument().getText(0, currentLength);
        String before = currentContent.substring(0, offset);
        String after = currentContent.substring(length + offset, currentLength);
        String newValue = before + (text == null ? "" : text) + after;

        if (isValid(newValue)) {
            super.replace(fb, offset, length, text, null);
        }
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        handleUpdate(fb, offset, 0, string);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        handleUpdate(fb, offset, length, text);
    }
}

// Custom DocumentFilter for decimal values
class DecimalInputFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        currentText += string;
        if (isValid(currentText)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        String before = currentText.substring(0, offset);
        String after = currentText.substring(length + offset, currentText.length());
        String newValue = before + (text == null ? "" : text) + after;
        if (isValid(newValue)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
    
    private boolean isValid(String text) {
        if (text.isEmpty()) return true;
        return text.matches("\\d*\\.?\\d*");
    }
}