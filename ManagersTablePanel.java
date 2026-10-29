package GUI;

import Control.systemDataBase;
import Model.Manager;
import Model.MainManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
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
import java.util.StringTokenizer;

public class ManagersTablePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private MainManagerPanel parentPanel; 

    private JTable table;
    private DefaultTableModel model;
    
    // --- Form components ---
    private JTextField formIdField;
    private JTextField formFirstNameField;
    private JTextField formLastNameField;
    private JTextField formPhoneField;
    private JTextField formAddressField;
    private JRadioButton formRegularManagerRadio;
    private JRadioButton formMainManagerRadio;
    private JTextField formUsernameField;
    private JPasswordField formPasswordField;

    // --- Action Buttons ---
    private JButton addButton;
    private JButton addFromFileButton;
    private JButton editButton;
    private JButton saveButton; // A single save button for both add and edit
    private JButton cancelButton;
    private JButton removeButton;
    private JButton backToDashboardButton;

    private int selectedRow = -1; 
    private boolean isAddingMode = false;

    public ManagersTablePanel(systemDataBase system, MainManagerPanel parentPanel) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout(10, 10)); 

        JLabel titleLabel = new JLabel("Manage Managers", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // --- Table View ---
        String[] columnNames = {"ID", "First Name", "Last Name", "Phone", "Address", "Type"};
        model = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !isAddingMode) {
                selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    populateDetailsFromTable();
                    setFormEnabled(false);
                    editButton.setEnabled(true);
                    
                    String managerId = model.getValueAt(selectedRow, 0).toString();
                    Manager selectedManager = findManagerById(managerId);
                    removeButton.setEnabled(!(selectedManager instanceof MainManager));
                    
                    saveButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                    addButton.setEnabled(true);
                }
            }
        });

        // --- Details & Form Panel ---
        JPanel detailsPanel = createFormPanel();
        
        // --- Split Pane for Table and Details ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        resetToInitialState();
        populateTable();
    }
    
    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        formContainer.setBorder(BorderFactory.createTitledBorder("Manager Details / Input"));

        // --- Form Fields ---
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Manager Type Radios
        ButtonGroup managerTypeGroup = new ButtonGroup();
        formRegularManagerRadio = new JRadioButton("Regular Manager");
        formMainManagerRadio = new JRadioButton("Main Manager");
        managerTypeGroup.add(formRegularManagerRadio);
        managerTypeGroup.add(formMainManagerRadio);
        
        formRegularManagerRadio.addActionListener(this);
        formMainManagerRadio.addActionListener(this);
        
        gbc.gridx = 0; gbc.gridy = 0; fieldsPanel.add(formRegularManagerRadio, gbc);
        gbc.gridx = 1; gbc.gridy = 0; fieldsPanel.add(formMainManagerRadio, gbc);

        // Other fields...
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; fieldsPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; formIdField = new JTextField(20); fieldsPanel.add(formIdField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; fieldsPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; formFirstNameField = new JTextField(20); fieldsPanel.add(formFirstNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; fieldsPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; formLastNameField = new JTextField(20); fieldsPanel.add(formLastNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; fieldsPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; formPhoneField = new JTextField(20); fieldsPanel.add(formPhoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; fieldsPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; formAddressField = new JTextField(20); fieldsPanel.add(formAddressField, gbc);

        gbc.gridx = 0; gbc.gridy = 6; fieldsPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; formUsernameField = new JTextField(20); fieldsPanel.add(formUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; fieldsPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; formPasswordField = new JPasswordField(20); fieldsPanel.add(formPasswordField, gbc);
        
        // Apply DocumentFilters
        ((AbstractDocument) formFirstNameField.getDocument()).setDocumentFilter(new InputFilter(12, "NO_DIGITS_SPACE"));
        ((AbstractDocument) formLastNameField.getDocument()).setDocumentFilter(new InputFilter(12, "NO_DIGITS_SPACE"));
        ((AbstractDocument) formPhoneField.getDocument()).setDocumentFilter(new InputFilter(10, "DIGITS_ONLY"));
        ((AbstractDocument) formAddressField.getDocument()).setDocumentFilter(new InputFilter(30, "ANY"));
        ((AbstractDocument) formUsernameField.getDocument()).setDocumentFilter(new InputFilter(20, "ANY"));
        ((AbstractDocument) formPasswordField.getDocument()).setDocumentFilter(new InputFilter(20, "ANY"));
        
        formContainer.add(fieldsPanel, BorderLayout.CENTER);

        // --- Action Buttons ---
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addButton = new JButton("Add New");
        addFromFileButton = new JButton("Add from File");
        editButton = new JButton("Edit Selected");
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        removeButton = new JButton("Remove Selected");
        backToDashboardButton = new JButton("Back to Dashboard");

        addButton.addActionListener(this);
        addFromFileButton.addActionListener(this);
        editButton.addActionListener(this);
        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        removeButton.addActionListener(this);
        backToDashboardButton.addActionListener(this);
        
        actionButtonsPanel.add(addButton);
        actionButtonsPanel.add(addFromFileButton);
        actionButtonsPanel.add(editButton);
        actionButtonsPanel.add(saveButton);
        actionButtonsPanel.add(cancelButton);
        actionButtonsPanel.add(removeButton);
        actionButtonsPanel.add(backToDashboardButton);
        
        formContainer.add(actionButtonsPanel, BorderLayout.SOUTH);
        
        return formContainer;
    }

    private void resetToInitialState() {
        isAddingMode = false;
        table.clearSelection();
        clearFormFields();
        setFormEnabled(false);
        
        addButton.setEnabled(true);
        addFromFileButton.setEnabled(true);
        editButton.setEnabled(false);
        saveButton.setEnabled(false);
        removeButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }
    
    private void populateTable() {
        model.setRowCount(0); 
        ArrayList<Manager> managers = ourSystem.getManagers();
        if(managers != null) {
            Collections.sort(managers, Comparator.comparing(Manager::getFirstName));
            for (Manager manager : managers) {
                String managerType = (manager instanceof MainManager) ? "Main Manager" : "Regular Manager";
                model.addRow(new Object[]{manager.getId(), manager.getFirstName(), manager.getLastName(), manager.getPhone(), manager.getAddress(), managerType});
            }
        }
    }
    
    private void populateDetailsFromTable() {
        if (selectedRow != -1) {
            Manager manager = findManagerById(model.getValueAt(selectedRow, 0).toString());
            if (manager == null) return;
            
            formIdField.setText(manager.getId());
            formFirstNameField.setText(manager.getFirstName());
            formLastNameField.setText(manager.getLastName());
            formPhoneField.setText(manager.getPhone());
            formAddressField.setText(manager.getAddress());

            if (manager instanceof MainManager) {
                MainManager mm = (MainManager) manager;
                formMainManagerRadio.setSelected(true);
                formUsernameField.setText(mm.getUserName());
                formPasswordField.setText(mm.getPassword());
            } else {
                formRegularManagerRadio.setSelected(true);
                formUsernameField.setText("");
                formPasswordField.setText("");
            }
        }
    }
    
    private void setFormEnabled(boolean enabled) {
        formIdField.setEnabled(false); // ID is always read-only
        formFirstNameField.setEnabled(enabled);
        formLastNameField.setEnabled(enabled);
        formPhoneField.setEnabled(enabled);
        formAddressField.setEnabled(enabled);
        
        formRegularManagerRadio.setEnabled(isAddingMode && enabled);
        formMainManagerRadio.setEnabled(isAddingMode && enabled);

        // Only enable username/password if it's a main manager AND the form is active
        formUsernameField.setEnabled(formMainManagerRadio.isSelected() && enabled);
        formPasswordField.setEnabled(formMainManagerRadio.isSelected() && enabled);
    }

    private void clearFormFields() {
        formIdField.setText("");
        formFirstNameField.setText("");
        formLastNameField.setText("");
        formPhoneField.setText("");
        formAddressField.setText("");
        formUsernameField.setText("");
        formPasswordField.setText("");
        formRegularManagerRadio.setSelected(true);
    }
    
    private String generateNewManagerId(boolean isMainManager) {
        String prefix = isMainManager ? "MM" : "M";
        int maxId = 0;
        
        for (Manager manager : ourSystem.getManagers()) {
            String id = manager.getId();
            boolean isCurrentManagerMain = manager instanceof MainManager;

            if (isMainManager == isCurrentManagerMain && id.startsWith(prefix)) {
                // For regular managers, make sure we don't parse "MM" IDs
                if (!isMainManager && id.startsWith("MM")) continue;
                
                try {
                    int currentId = Integer.parseInt(id.substring(prefix.length()));
                    if (currentId > maxId) maxId = currentId;
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // Ignore malformed IDs
                }
            }
        }
        return prefix + (maxId + 1);
    }
    
    private boolean validateManagerInput(String firstName, String lastName, String phone, boolean isMain, String username, String password) {
        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all basic manager fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
        if (isMain && (username.isEmpty() || password.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Username and password are required for Main Manager.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private Manager findManagerById(String managerId) {
        return ourSystem.getManagers().stream().filter(m -> m.getId().equals(managerId)).findFirst().orElse(null);
    }

    private void addManagersFromFile() {
        Set<String> existingIds = new HashSet<>();
        ourSystem.getManagers().forEach(m -> existingIds.add(m.getId()));

        int addedCount = 0;
        int duplicateCount = 0;
        StringBuilder duplicateDetails = new StringBuilder();

        // Updated path to look in src/importMe
        File importDir = new File("src/importMe");
        if (!importDir.exists()) {
            // Optional: Create the directory if it doesn't exist.
            // For importing, it's often expected to exist, or user picks file.
            // But if we want to ensure it, uncomment:
            // importDir.mkdirs();
        }
        
        File fileToRead = new File(importDir, "SystemManagers.txt");

        try (Scanner scanner = new Scanner(fileToRead)) { // Use fileToRead
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                StringTokenizer st = new StringTokenizer(line, " ");
                if (st.countTokens() < 5) {
                    JOptionPane.showMessageDialog(this, "Invalid file format: Not enough details in line:\n" + line, "File Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                String id = st.nextToken();
                String firstName = st.nextToken();
                String lastName = st.nextToken();
                String phone = st.nextToken();
                String address = st.nextToken();

                if (existingIds.contains(id)) {
                    duplicateCount++;
                    if (duplicateDetails.length() > 0) duplicateDetails.append(", ");
                    duplicateDetails.append(id);
                    continue;
                }

                ourSystem.addManager(new Manager(id, firstName, lastName, phone, address));
                existingIds.add(id);
                addedCount++;
            }

            String summaryMessage = "Manager Import Complete.\n\n" +
                    "Successfully added: " + addedCount + "\n" +
                    "Duplicates found (not added): " + duplicateCount;
            if (duplicateCount > 0) {
                summaryMessage += "\nDuplicate IDs: " + duplicateDetails.toString();
            }
            JOptionPane.showMessageDialog(this, summaryMessage, "Import Summary", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "SystemManagers.txt not found in " + fileToRead.getAbsolutePath() + "!", "File Error", JOptionPane.ERROR_MESSAGE);
        }
        populateTable();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // --- Radio button clicks during Add Mode ---
        if (isAddingMode && (e.getSource() == formRegularManagerRadio || e.getSource() == formMainManagerRadio)) {
            boolean isMain = formMainManagerRadio.isSelected();
            formIdField.setText(generateNewManagerId(isMain));
            setFormEnabled(true); // Re-set field enabled states
            return;
        }
        
        // --- Action button clicks ---
        Object source = e.getSource();
        if (source == addButton) {
            isAddingMode = true;
            table.clearSelection();
            clearFormFields();
            setFormEnabled(true);
            formIdField.setText(generateNewManagerId(false)); // Default to regular manager
            
            saveButton.setText("Add Manager");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        } else if (source == addFromFileButton) {
            addManagersFromFile();
        }
        else if (source == editButton) {
            if (selectedRow != -1) {
                isAddingMode = false;
                setFormEnabled(true);
                
                saveButton.setText("Save Changes");
                saveButton.setEnabled(true);
                cancelButton.setEnabled(true);
                addButton.setEnabled(false);
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
            }
        }
        else if (source == saveButton) {
            String id = formIdField.getText();
            String firstName = formFirstNameField.getText();
            String lastName = formLastNameField.getText();
            String phone = formPhoneField.getText();
            String address = formAddressField.getText();
            String username = formUsernameField.getText();
            String password = new String(formPasswordField.getPassword());
            boolean isMain = formMainManagerRadio.isSelected();

            if (!validateManagerInput(firstName, lastName, phone, isMain, username, password)) {
                return;
            }

            if (isAddingMode) {
                Manager newManager = isMain ? 
                    new MainManager(id, firstName, lastName, phone, address, username, password) :
                    new Manager(id, firstName, lastName, phone, address);
                
                if (ourSystem.addManager(newManager)) {
                    JOptionPane.showMessageDialog(this, "Manager added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    resetToInitialState();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add manager. ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else { // Editing
                Manager managerToUpdate = findManagerById(id);
                if (managerToUpdate != null) {
                    managerToUpdate.setFirstName(firstName);
                    managerToUpdate.setLastName(lastName);
                    managerToUpdate.setPhone(phone);
                    managerToUpdate.setAddress(address);
                    if (managerToUpdate instanceof MainManager) {
                        ((MainManager) managerToUpdate).setUserName(username);
                        ((MainManager) managerToUpdate).setPassword(password);
                    }
                    JOptionPane.showMessageDialog(this, "Manager " + id + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    resetToInitialState();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update manager. Not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else if (source == cancelButton) {
            resetToInitialState();
        }
        else if (source == removeButton) {
            if (selectedRow != -1) {
                String managerId = model.getValueAt(selectedRow, 0).toString();
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + managerId + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Manager managerToRemove = findManagerById(managerId);
                    if (ourSystem.removeManager(managerToRemove)) {
                        JOptionPane.showMessageDialog(this, "Manager " + managerId + " removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        populateTable();
                        resetToInitialState();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove manager " + managerId + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        else if (source == backToDashboardButton) {
            parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL); 
        }
    }
}

// Custom DocumentFilter to restrict length and character types
class InputFilter extends DocumentFilter {
    private int maxLength;
    private String filterType;

    public InputFilter(int maxLength, String filterType) {
        this.maxLength = maxLength;
        this.filterType = filterType;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) return;
        if ((fb.getDocument().getLength() + string.length()) <= maxLength && test(string)) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) return;
        if ((fb.getDocument().getLength() + text.length() - length) <= maxLength && test(text)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
    
    private boolean test(String text) {
        if (text.isEmpty()) {
            return true;
        }
        switch (filterType) {
            case "NO_DIGITS_SPACE": // For names, allows letters and spaces
                return text.chars().allMatch(c -> Character.isLetter(c) || Character.isWhitespace(c));
            case "NO_DIGITS": // Strict letters only
                return text.chars().allMatch(Character::isLetter);
            case "DIGITS_ONLY":
                return text.chars().allMatch(Character::isDigit);
            case "ANY":
            default:
                return true;
        }
    }
}