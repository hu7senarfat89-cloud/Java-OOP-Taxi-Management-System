package GUI;

import Control.systemDataBase;
import Model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ManageOrdersPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    @SuppressWarnings("unused")
	private MainManager mainManager;
    private MainManagerPanel parentPanel;

    private JTable table;
    private DefaultTableModel model;

    // Details/Form fields
    private JTextField detailsOrderIdField;
    private JComboBox<String> detailsSubscriberComboBox;
    private JComboBox<String> detailsTaxiComboBox;
    private JComboBox<String> detailsManagerComboBox;
    private JTextField detailsDayField;
    private JTextField detailsMonthField;
    private JTextField detailsHourField;
    private JTextField detailsOrderPriceField;

    // Action buttons
    private JButton addNewOrderButton;
    private JButton editOrderButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton removeOrderButton;
    private JButton backButton;

    private JComboBox<String> managerFilterComboBox;

    private int selectedRow = -1;
    private boolean isAddingMode = false;
    private boolean isEditingMode = false;

    public ManageOrdersPanel(systemDataBase system, MainManagerPanel parentPanel, MainManager manager) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        this.mainManager = manager;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Manage All Orders", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // --- Manager Filter ---
        managerFilterComboBox = new JComboBox<>();
        populateManagerFilter();
        managerFilterComboBox.addActionListener(e -> populateTable());
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Manager:"));
        filterPanel.add(managerFilterComboBox);
        headerPanel.add(filterPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // --- Table View ---
        String[] columnNames = {"Order ID", "Manager", "Subscriber", "Taxi Code", "Date", "Time", "Price"};
        model = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);

        // --- Details Section & Form ---
        JPanel detailsPanel = createFormPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isAddingMode && !isEditingMode) {
                selectedRow = table.getSelectedRow();
                resetToViewState();
                if (selectedRow != -1) {
                    populateDetailsFromTable();
                    editOrderButton.setEnabled(true);
                    removeOrderButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                }
            }
        });

        resetToInitialState();
        populateTable();
    }

    private void populateManagerFilter() {
        managerFilterComboBox.removeAllItems();
        managerFilterComboBox.addItem("All Managers");
        ourSystem.getManagers().stream()
                .filter(m -> !(m instanceof MainManager))
                .sorted(Comparator.comparing(Manager::getFirstName))
                .forEach(m -> managerFilterComboBox.addItem(m.getId() + " - " + m.getFirstName()));
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
        
        gbc.gridx = 0; gbc.gridy = 1; fieldsPanel.add(new JLabel("Manager:"), gbc);
        gbc.gridx = 1; detailsManagerComboBox = new JComboBox<>(); fieldsPanel.add(detailsManagerComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; fieldsPanel.add(new JLabel("Subscriber:"), gbc);
        gbc.gridx = 1; detailsSubscriberComboBox = new JComboBox<>(); fieldsPanel.add(detailsSubscriberComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; fieldsPanel.add(new JLabel("Taxi:"), gbc);
        gbc.gridx = 1; detailsTaxiComboBox = new JComboBox<>(); fieldsPanel.add(detailsTaxiComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; fieldsPanel.add(new JLabel("Date (DD/MM):"), gbc);
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        detailsDayField = new JTextField(3);
        detailsMonthField = new JTextField(3);
        datePanel.add(detailsDayField);
        datePanel.add(new JLabel("/"));
        datePanel.add(detailsMonthField);
        gbc.gridx = 1; fieldsPanel.add(datePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; fieldsPanel.add(new JLabel("Hour (0-23):"), gbc);
        gbc.gridx = 1; detailsHourField = new JTextField(5); fieldsPanel.add(detailsHourField, gbc);

        gbc.gridx = 0; gbc.gridy = 6; fieldsPanel.add(new JLabel("Order Price:"), gbc);
        gbc.gridx = 1; detailsOrderPriceField = new JTextField(10); fieldsPanel.add(detailsOrderPriceField, gbc);
        
        ((AbstractDocument) detailsDayField.getDocument()).setDocumentFilter(new RangeInputFilter(31));
        ((AbstractDocument) detailsMonthField.getDocument()).setDocumentFilter(new RangeInputFilter(12));
        ((AbstractDocument) detailsHourField.getDocument()).setDocumentFilter(new RangeInputFilter(23));
        ((AbstractDocument) detailsOrderPriceField.getDocument()).setDocumentFilter(new DecimalInputFilter());

        formContainer.add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addNewOrderButton = new JButton("Add New Order");
        editOrderButton = new JButton("Edit Order");
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        removeOrderButton = new JButton("Remove Order");
        backButton = new JButton("Back to Dashboard");

        JButton[] buttons = {addNewOrderButton, editOrderButton, saveButton, cancelButton, removeOrderButton, backButton};
        for (JButton btn : buttons) {
            btn.addActionListener(this);
            buttonPanel.add(btn);
        }
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        return formContainer;
    }

    private void resetToInitialState() {
        isAddingMode = false;
        isEditingMode = false;
        table.clearSelection();
        clearFormFields();
        setFormEnabled(false);

        addNewOrderButton.setEnabled(true);
        editOrderButton.setEnabled(false);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        removeOrderButton.setEnabled(false);
    }

    private void resetToViewState() {
        isAddingMode = false;
        isEditingMode = false;
        clearFormFields();
        setFormEnabled(false);

        addNewOrderButton.setEnabled(true);
        editOrderButton.setEnabled(false);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        removeOrderButton.setEnabled(false);
    }
    
    private void setFormEnabled(boolean enabled) {
        detailsOrderIdField.setEnabled(false);
        detailsManagerComboBox.setEnabled(isAddingMode && enabled);
        detailsSubscriberComboBox.setEnabled(isAddingMode && enabled);
        detailsTaxiComboBox.setEnabled(isAddingMode && enabled);
        detailsDayField.setEnabled((isAddingMode || isEditingMode) && enabled);
        detailsMonthField.setEnabled((isAddingMode || isEditingMode) && enabled);
        detailsHourField.setEnabled((isAddingMode || isEditingMode) && enabled);
        detailsOrderPriceField.setEnabled((isAddingMode || isEditingMode) && enabled);
    }
    
    private void clearFormFields() {
        detailsOrderIdField.setText("");
        detailsManagerComboBox.removeAllItems();
        detailsSubscriberComboBox.removeAllItems();
        detailsTaxiComboBox.removeAllItems();
        detailsDayField.setText("");
        detailsMonthField.setText("");
        detailsHourField.setText("");
        detailsOrderPriceField.setText("");
    }
    
    private void populateTable() {
        model.setRowCount(0);
        String selectedManager = (String) managerFilterComboBox.getSelectedItem();
        List<Order> ordersForTable;

        if (selectedManager != null && !selectedManager.equals("All Managers")) {
            String managerId = selectedManager.split(" - ")[0];
            ordersForTable = ourSystem.getOrders().stream()
                    .filter(o -> o.getManagerCode().equals(managerId))
                    .sorted(Comparator.comparing(Order::getOrderNum))
                    .collect(Collectors.toList());
        } else {
            ordersForTable = ourSystem.getOrders().stream()
                    .sorted(Comparator.comparing(Order::getOrderNum))
                    .collect(Collectors.toList());
        }

        for (Order order : ordersForTable) {
            String subName = ourSystem.getSubscriptions().stream()
                    .filter(s -> s.getSubCode().equals(order.getSubCode()))
                    .findFirst()
                    .map(s -> s.getFirstName() + " " + s.getLastName())
                    .orElse("N/A");
            
            String managerName = ourSystem.getManagers().stream()
                    .filter(m -> m.getId().equals(order.getManagerCode()))
                    .findFirst()
                    .map(Manager::getFirstName)
                    .orElse("N/A");

            model.addRow(new Object[]{
                    order.getOrderNum(), managerName, subName, order.getTaxi().getTaxiCode(),
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
            
            populateManagerComboBox();
            selectComboBoxItem(detailsManagerComboBox, order.getManagerCode());
            
            populateSubscribersComboBox();
            selectComboBoxItem(detailsSubscriberComboBox, order.getSubCode());
            
            detailsTaxiComboBox.removeAllItems();
            detailsTaxiComboBox.addItem(order.getTaxi().getTaxiCode() + " - (" + getTaxiType(order.getTaxi()) + ")");

            detailsDayField.setText(String.valueOf(order.getDay()));
            detailsMonthField.setText(String.valueOf(order.getMonth()));
            detailsHourField.setText(String.valueOf(order.getHour()));
            detailsOrderPriceField.setText(String.format("%.2f", order.getOrderPrice()));
        }
    }
    
    private void selectComboBoxItem(JComboBox<String> comboBox, String prefix) {
        if (comboBox.getItemCount() > 0) {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                if (comboBox.getItemAt(i).startsWith(prefix)) {
                    comboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
    }
    
    private void populateManagerComboBox() {
        detailsManagerComboBox.removeAllItems();
        ourSystem.getManagers().stream()
                .filter(m -> !(m instanceof MainManager))
                .forEach(m -> detailsManagerComboBox.addItem(m.getId() + " - " + m.getFirstName()));
    }
    
    private void populateSubscribersComboBox() {
        detailsSubscriberComboBox.removeAllItems();
        ourSystem.getSubscriptions().forEach(sub ->
                detailsSubscriberComboBox.addItem(sub.getSubCode() + " - " + sub.getFirstName() + " " + sub.getLastName()));
    }

    private void populateTaxisComboBox() {
        detailsTaxiComboBox.removeAllItems();
        List<Taxi> availableTaxis = ourSystem.getTaxis().stream()
                .filter(Taxi::isAvailable)
                .collect(Collectors.toList());

        if (availableTaxis.isEmpty()) {
            detailsTaxiComboBox.addItem("No taxis available");
        } else {
            availableTaxis.forEach(t -> detailsTaxiComboBox.addItem(t.getTaxiCode() + " - (" + getTaxiType(t) + ")"));
        }
    }

    private String getTaxiType(Taxi taxi) {
        if (taxi instanceof ExpressTaxi) return "Express";
        if (taxi instanceof IntercityTaxi) return "Intercity";
        return "Regular";
    }

    private String generateNewOrderId() {
        return "O" + (ourSystem.getOrders().stream()
                .map(o -> o.getOrderNum().substring(1))
                .mapToInt(Integer::parseInt)
                .max().orElse(0) + 1);
    }
    
    private Order findOrderById(String orderId) {
        return ourSystem.getOrders().stream().filter(o -> o.getOrderNum().equals(orderId)).findFirst().orElse(null);
    }

    private Taxi findTaxiById(String taxiId) {
        return ourSystem.getTaxis().stream().filter(t -> t.getTaxiCode().equals(taxiId)).findFirst().orElse(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == backButton) {
            parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL);
            return;
        }
        
        if (source == addNewOrderButton) {
            isAddingMode = true;
            isEditingMode = false;
            table.clearSelection();
            clearFormFields();
            setFormEnabled(true);
            detailsOrderIdField.setText(generateNewOrderId());
            populateManagerComboBox();
            populateSubscribersComboBox();
            populateTaxisComboBox();

            saveButton.setText("Add Order");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addNewOrderButton.setEnabled(false);
            editOrderButton.setEnabled(false);
            removeOrderButton.setEnabled(false);
        } else if (source == editOrderButton) {
            isEditingMode = true;
            isAddingMode = false;
            setFormEnabled(true);
            saveButton.setText("Save Changes");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addNewOrderButton.setEnabled(false);
            editOrderButton.setEnabled(false);
            removeOrderButton.setEnabled(false);
        } else if (source == saveButton) {
            String orderId = detailsOrderIdField.getText();
            String managerInfo = (String) detailsManagerComboBox.getSelectedItem();
            String subInfo = (String) detailsSubscriberComboBox.getSelectedItem();
            String taxiInfo = (String) detailsTaxiComboBox.getSelectedItem();

            if (managerInfo == null || subInfo == null || taxiInfo == null) {
                JOptionPane.showMessageDialog(this, "Please ensure a manager, subscriber and taxi are selected.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int day = Integer.parseInt(detailsDayField.getText());
            int month = Integer.parseInt(detailsMonthField.getText());
            int hour = Integer.parseInt(detailsHourField.getText());

            String managerId = managerInfo.split(" - ")[0];
            String subCode = subInfo.split(" - ")[0];
            String taxiCode = taxiInfo.split(" - ")[0];
            Taxi taxi = findTaxiById(taxiCode);

            if (isAddingMode) {
                double price = (detailsOrderPriceField.getText().isEmpty()) ? taxi.getMinPrice() : Double.parseDouble(detailsOrderPriceField.getText());
                Order newOrder = new Order(orderId, managerId, day, month, hour, subCode, taxi, price);
                if (ourSystem.addOrder(newOrder)) {
                    taxi.setAvailable(false);
                    JOptionPane.showMessageDialog(this, "Order added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add order. ID might exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (isEditingMode) {
                Order orderToChange = findOrderById(orderId);
                orderToChange.setManagerCode(managerId);
                orderToChange.setSubCode(subCode);
                orderToChange.setDay(day);
                orderToChange.setMonth(month);
                orderToChange.setHour(hour);
                orderToChange.setOrderPrice(Double.parseDouble(detailsOrderPriceField.getText()));
                JOptionPane.showMessageDialog(this, "Order details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            populateTable();
            resetToInitialState();
        } else if (source == cancelButton) {
            resetToInitialState();
        } else if (source == removeOrderButton) {
            if (selectedRow != -1) {
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this order?", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
    }
}