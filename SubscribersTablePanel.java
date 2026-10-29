package GUI;

import Control.systemDataBase;
import Model.Subscription;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class SubscribersTablePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private MainManagerPanel parentPanel; 

    private JTable table;
    private DefaultTableModel model;
    
    // Details/Form fields
    private JTextField detailsSubCodeField;
    private JTextField detailsFirstNameField;
    private JTextField detailsLastNameField;
    private JTextField detailsPhoneField;
    private JTextField detailsAddressField;

    // Action buttons
    private JButton addNewButton;
    private JButton receiveFromFileButton;
    private JButton addSubscriberButton;
    private JButton editButton;
    private JButton saveChangesButton;
    private JButton cancelButton;
    private JButton removeButton;
    private JButton backToDashboardButton; 

    private int selectedRow = -1;
    private boolean isAddingMode = false;

    public SubscribersTablePanel(systemDataBase system, MainManagerPanel parentPanel) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Manage Subscribers", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // --- Table View ---
        String[] columnNames = {"Subscriber Code", "First Name", "Last Name", "Address", "Phone"};
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
        
        // --- Details Section & Action Buttons ---
        JPanel detailsAndButtonsPanel = new JPanel(new BorderLayout(10, 10));

        // Details Section
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Subscriber Details / Input"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; detailsPanel.add(new JLabel("Subscriber Code:"), gbc);
        gbc.gridx = 1; detailsSubCodeField = new JTextField(20); detailsPanel.add(detailsSubCodeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; detailsPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; detailsFirstNameField = new JTextField(20); detailsPanel.add(detailsFirstNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; detailsPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; detailsLastNameField = new JTextField(20); detailsPanel.add(detailsLastNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; detailsPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; detailsPhoneField = new JTextField(20); detailsPanel.add(detailsPhoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; detailsPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; detailsAddressField = new JTextField(20); detailsPanel.add(detailsAddressField, gbc);
        

        // Apply DocumentFilters to restrict input
        ((AbstractDocument) detailsFirstNameField.getDocument()).setDocumentFilter(new InputFilter(12, "NO_DIGITS"));
        ((AbstractDocument) detailsLastNameField.getDocument()).setDocumentFilter(new InputFilter(12, "NO_DIGITS"));
        ((AbstractDocument) detailsAddressField.getDocument()).setDocumentFilter(new InputFilter(30, "ANY"));
        ((AbstractDocument) detailsPhoneField.getDocument()).setDocumentFilter(new InputFilter(10, "DIGITS_ONLY"));

        detailsAndButtonsPanel.add(detailsPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addNewButton = new JButton("Add New");
        receiveFromFileButton = new JButton("Receive from File");
        addSubscriberButton = new JButton("Add Subscriber");
        editButton = new JButton("Edit Selected");
        saveChangesButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");
        removeButton = new JButton("Remove Selected");
        backToDashboardButton = new JButton("Back to Dashboard");

        addNewButton.addActionListener(this);
        receiveFromFileButton.addActionListener(this);
        addSubscriberButton.addActionListener(this);
        editButton.addActionListener(this);
        saveChangesButton.addActionListener(this);
        cancelButton.addActionListener(this);
        removeButton.addActionListener(this);
        backToDashboardButton.addActionListener(this);

        actionButtonsPanel.add(addNewButton);
        actionButtonsPanel.add(receiveFromFileButton);
        actionButtonsPanel.add(addSubscriberButton);
        actionButtonsPanel.add(editButton);
        actionButtonsPanel.add(saveChangesButton);
        actionButtonsPanel.add(cancelButton);
        actionButtonsPanel.add(removeButton);
        actionButtonsPanel.add(backToDashboardButton);
        
        detailsAndButtonsPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsAndButtonsPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isAddingMode) {
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    populateDetailsFromTable();
                    setFormEnabled(false);
                    editButton.setEnabled(true);
                    removeButton.setEnabled(true);
                    addNewButton.setEnabled(true);
                    saveChangesButton.setEnabled(false);
                    addSubscriberButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                }
            }
        });

        resetToInitialState();
        populateTable();
    }
    
    private void resetToInitialState() {
        isAddingMode = false;
        table.clearSelection();
        clearFormFields();
        setFormEnabled(false);
        
        addNewButton.setEnabled(true);
        receiveFromFileButton.setEnabled(true);
        addSubscriberButton.setEnabled(false);
        editButton.setEnabled(false);
        saveChangesButton.setEnabled(false);
        removeButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    private String generateNewSubId() {
        ArrayList<Subscription> subscriptions = ourSystem.getSubscriptions();
        if (subscriptions == null || subscriptions.isEmpty()) {
            return "S1";
        }
        int maxId = 0;
        for (Subscription sub : subscriptions) {
            try {
                int currentId = Integer.parseInt(sub.getSubCode().substring(1));
                if (currentId > maxId) maxId = currentId;
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                System.err.println("Skipping invalid subscriber code format: " + sub.getSubCode());
            }
        }
        return "S" + (maxId + 1);
    }

    private void populateTable() {
        String selectedSubCode = null;
        if (selectedRow != -1 && selectedRow < model.getRowCount()) {
            selectedSubCode = model.getValueAt(selectedRow, 0).toString();
        }

        model.setRowCount(0); 
        ArrayList<Subscription> subscribers = ourSystem.getSubscriptions();
        if (subscribers != null) {
            Collections.sort(subscribers, Comparator.comparing(Subscription::getLastName));
            for (Subscription sub : subscribers) {
                model.addRow(new Object[]{sub.getSubCode(), sub.getFirstName(), sub.getLastName(), sub.getAddress(), sub.getPhone()});
            }
        }

        if (selectedSubCode != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if(model.getValueAt(i,0).toString().equals(selectedSubCode)) {
                    table.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void populateDetailsFromTable() {
        if (selectedRow != -1) {
            detailsSubCodeField.setText(model.getValueAt(selectedRow, 0).toString());
            detailsFirstNameField.setText(model.getValueAt(selectedRow, 1).toString());
            detailsLastNameField.setText(model.getValueAt(selectedRow, 2).toString());
            detailsAddressField.setText(model.getValueAt(selectedRow, 3).toString()); // CORRECTED
            detailsPhoneField.setText(model.getValueAt(selectedRow, 4).toString());   // CORRECTED
        }
    }

    private void clearFormFields() {
        detailsSubCodeField.setText("");
        detailsFirstNameField.setText("");
        detailsLastNameField.setText("");
        detailsAddressField.setText("");
        detailsPhoneField.setText("");
    }

    private void setFormEnabled(boolean enabled) {
        detailsSubCodeField.setEnabled(false);
        detailsFirstNameField.setEnabled(enabled);
        detailsLastNameField.setEnabled(enabled);
        detailsAddressField.setEnabled(enabled);
        detailsPhoneField.setEnabled(enabled);
    }
    
    private boolean validateInput(String firstName, String lastName, String phone, String address) {
        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all the fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (firstName.length() < 3) {
            JOptionPane.showMessageDialog(this, "First name must be at least 3 characters.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (lastName.length() < 3) {
            JOptionPane.showMessageDialog(this, "Last name must be at least 3 characters.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!phone.matches("0\\d{9}")) {
            JOptionPane.showMessageDialog(this, "Phone must be 10 digits, starting with 0.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void receiveSubscribersFromFile() {
        Set<String> existingIds = new HashSet<>();
        ourSystem.getSubscriptions().forEach(s -> existingIds.add(s.getSubCode()));

        int addedCount = 0;
        int duplicateCount = 0;
        StringBuilder duplicateDetails = new StringBuilder();

        // New path to look in src/importMe
        File importDir = new File("src/importMe");
        if (!importDir.exists()) {
            // It's generally good practice to create the directory if it might not exist.
            // If the expectation is that the user manually places the file, you might
            // choose not to create it, but for robustness, it's often better to.
            // If the directory isn't created and the file doesn't exist, a FileNotFoundException will occur.
            // For this specific request, the prompt implies the directory exists or should be created
            // if we're moving files there.
            importDir.mkdirs(); // Create the directory if it doesn't exist
        }
        
        File fileToRead = new File(importDir, "members.txt");

        try (Scanner scanner = new Scanner(fileToRead)) { // Use fileToRead
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] details = line.split(" ");
                if (details.length < 5) {
                    JOptionPane.showMessageDialog(this, "Invalid file format: Not enough details in line:\n" + line, "File Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                String id = details[0];
                String firstName = details[1];
                String lastName = details[2];
                String address = details[3];
                String phone = details[4];


                if (existingIds.contains(id)) {
                    duplicateCount++;
                    if (duplicateDetails.length() > 0) duplicateDetails.append(", ");
                    duplicateDetails.append(id);
                    continue;
                }

                ourSystem.addSubscription(new Subscription(id, firstName, lastName, phone, address));
                existingIds.add(id);
                addedCount++;
            }

            String summaryMessage = "Subscriber Import Complete.\n\n" +
                    "Successfully added: " + addedCount + "\n" +
                    "Duplicates found (not added): " + duplicateCount;
            if (duplicateCount > 0) {
                summaryMessage += "\nDuplicate IDs: " + duplicateDetails.toString();
            }
            JOptionPane.showMessageDialog(this, summaryMessage, "Import Summary", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "members.txt not found in " + fileToRead.getAbsolutePath() + "!", "File Error", JOptionPane.ERROR_MESSAGE);
        }
        populateTable();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addNewButton) {
            isAddingMode = true;
            table.clearSelection();
            clearFormFields();
            setFormEnabled(true);
            detailsSubCodeField.setText(generateNewSubId());
            detailsFirstNameField.requestFocusInWindow();
            
            addNewButton.setEnabled(false);
            addSubscriberButton.setEnabled(true);
            editButton.setEnabled(false);
            saveChangesButton.setEnabled(false);
            removeButton.setEnabled(false);
            cancelButton.setEnabled(true);
        } else if (e.getSource() == receiveFromFileButton) {
            receiveSubscribersFromFile();
        }
        else if (e.getSource() == addSubscriberButton) {
            String subCode = detailsSubCodeField.getText().trim();
            String firstName = detailsFirstNameField.getText().trim();
            String lastName = detailsLastNameField.getText().trim();
            String phone = detailsPhoneField.getText().trim();
            String address = detailsAddressField.getText().trim();

            if (!validateInput(firstName, lastName, phone, address)) {
                return;
            }

            Subscription newSub = new Subscription(subCode, firstName, lastName, phone, address);
            if (ourSystem.addSubscription(newSub)) {
                JOptionPane.showMessageDialog(this, "Subscriber added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                populateTable();
                resetToInitialState();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add subscriber. Code might exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else if (e.getSource() == editButton) {
            if (selectedRow != -1) {
                isAddingMode = false;
                setFormEnabled(true);
                detailsFirstNameField.requestFocusInWindow();
                
                editButton.setEnabled(false);
                saveChangesButton.setEnabled(true);
                addNewButton.setEnabled(false);
                removeButton.setEnabled(false);
                cancelButton.setEnabled(true);
            }
        } 
        else if (e.getSource() == saveChangesButton) {
            String subCode = detailsSubCodeField.getText().trim();
            String firstName = detailsFirstNameField.getText().trim();
            String lastName = detailsLastNameField.getText().trim();
            String phone = detailsPhoneField.getText().trim();
            String address = detailsAddressField.getText().trim();
            
            if (!validateInput(firstName, lastName, phone, address)) {
                return;
            }
            
            Subscription subToUpdate = ourSystem.getSubscriptions().stream()
                .filter(s -> s.getSubCode().equals(subCode))
                .findFirst().orElse(null);

            if (subToUpdate != null) {
                subToUpdate.setFirstName(firstName);
                subToUpdate.setLastName(lastName);
                subToUpdate.setAddress(address);
                subToUpdate.setPhone(phone);
                JOptionPane.showMessageDialog(this, "Subscriber " + subCode + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                populateTable();
                resetToInitialState();

            } else {
                JOptionPane.showMessageDialog(this, "Failed to update subscriber. Not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        else if (e.getSource() == cancelButton) {
            resetToInitialState();
        } 
        else if (e.getSource() == removeButton) {
            if (selectedRow != -1) {
                String subCode = model.getValueAt(selectedRow, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + subCode + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Subscription subToRemove = ourSystem.getSubscriptions().stream()
                        .filter(s -> s.getSubCode().equals(subCode))
                        .findFirst().orElse(null);
                        
                    if (subToRemove != null && ourSystem.removeSubscription(subToRemove)) {
                        JOptionPane.showMessageDialog(this, "Subscriber " + subCode + " removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        populateTable();
                        resetToInitialState();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove subscriber " + subCode + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } 
        else if (e.getSource() == backToDashboardButton) {
            parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL); 
        }
    }
}