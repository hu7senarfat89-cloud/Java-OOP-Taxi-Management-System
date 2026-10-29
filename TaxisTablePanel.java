package GUI;

import Control.systemDataBase;
import Model.ExpressTaxi;
import Model.IntercityTaxi;
import Model.Taxi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class TaxisTablePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private MainManagerPanel parentPanel;

    private JTable table;
    private DefaultTableModel model;

    // --- Form Components ---
    private JTextField formTaxiCodeField;
    private JCheckBox formAvailableCheckBox;
    private JTextField formMinPriceField;

    private JRadioButton formRegularRadio;
    private JRadioButton formExpressRadio;
    private JRadioButton formIntercityRadio;

    private JPanel specificFieldsPanel;
    private CardLayout specificFieldsCardLayout;

    // Express fields
    private JCheckBox formCityTaxiCheckBox;
    private JTextField formExpressExtraPriceField;

    // Intercity fields
    private JTextField formCitiesField;
    private JTextField formIntercityExtraPriceField;
    private JTextField formMaxHoursField;
    
    // --- Action Buttons ---
    private JButton addButton;
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton removeButton;
    private JButton backToDashboardButton;

    private int selectedRow = -1;
    private boolean isAddingMode = false;

    public TaxisTablePanel(systemDataBase system, MainManagerPanel parentPanel) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Manage Taxis", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // --- Table View ---
        String[] columnNames = {"Taxi Code", "Available", "Min Price", "Type", "Extra Info"};
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
                    removeButton.setEnabled(true);
                    addButton.setEnabled(true);
                    saveButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                }
            }
        });
        
        // --- Form and Details Panel ---
        JPanel detailsPanel = createFormPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsPanel);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        resetToInitialState();
        populateTable();
    }

    private JPanel createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        formContainer.setBorder(BorderFactory.createTitledBorder("Taxi Details / Input"));

        // --- Form Fields ---
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Taxi Type Radios
        formRegularRadio = new JRadioButton("Regular");
        formExpressRadio = new JRadioButton("Express");
        formIntercityRadio = new JRadioButton("Intercity");
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(formRegularRadio);
        typeGroup.add(formExpressRadio);
        typeGroup.add(formIntercityRadio);
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(formRegularRadio);
        radioPanel.add(formExpressRadio);
        radioPanel.add(formIntercityRadio);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; fieldsPanel.add(radioPanel, gbc);
        gbc.gridwidth = 1;

        // Row 1: Common Fields
        gbc.gridx = 0; gbc.gridy = 1; fieldsPanel.add(new JLabel("Taxi Code:"), gbc);
        gbc.gridx = 1; formTaxiCodeField = new JTextField(15); fieldsPanel.add(formTaxiCodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; fieldsPanel.add(new JLabel("Available:"), gbc);
        gbc.gridx = 1; formAvailableCheckBox = new JCheckBox(); fieldsPanel.add(formAvailableCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; fieldsPanel.add(new JLabel("Min Price:"), gbc);
        gbc.gridx = 1; formMinPriceField = new JTextField(15); fieldsPanel.add(formMinPriceField, gbc);

        // Row 2: Specific Fields Panel using CardLayout
        specificFieldsCardLayout = new CardLayout();
        specificFieldsPanel = new JPanel(specificFieldsCardLayout);
        
        // Express Panel
        JPanel expressPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 0; expressPanel.add(new JLabel("City Taxi:"), gbc);
        gbc.gridx = 1; formCityTaxiCheckBox = new JCheckBox(); expressPanel.add(formCityTaxiCheckBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; expressPanel.add(new JLabel("Extra Price:"), gbc);
        gbc.gridx = 1; formExpressExtraPriceField = new JTextField(15); expressPanel.add(formExpressExtraPriceField, gbc);
        
        // Intercity Panel
        JPanel intercityPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 0; intercityPanel.add(new JLabel("Cities (comma-sep):"), gbc);
        gbc.gridx = 1; formCitiesField = new JTextField(15); intercityPanel.add(formCitiesField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; intercityPanel.add(new JLabel("Extra Price:"), gbc);
        gbc.gridx = 1; formIntercityExtraPriceField = new JTextField(15); intercityPanel.add(formIntercityExtraPriceField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; intercityPanel.add(new JLabel("Max Hours:"), gbc);
        gbc.gridx = 1; formMaxHoursField = new JTextField(15); intercityPanel.add(formMaxHoursField, gbc);

        specificFieldsPanel.add(new JPanel(), "REGULAR");
        specificFieldsPanel.add(expressPanel, "EXPRESS");
        specificFieldsPanel.add(intercityPanel, "INTERCITY");

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; fieldsPanel.add(specificFieldsPanel, gbc);
        formContainer.add(fieldsPanel, BorderLayout.CENTER);
        
        // Add ActionListeners to radio buttons to swap cards
        formRegularRadio.addActionListener(e -> specificFieldsCardLayout.show(specificFieldsPanel, "REGULAR"));
        formExpressRadio.addActionListener(e -> specificFieldsCardLayout.show(specificFieldsPanel, "EXPRESS"));
        formIntercityRadio.addActionListener(e -> specificFieldsCardLayout.show(specificFieldsPanel, "INTERCITY"));

        // Document Filters
        ((AbstractDocument) formMinPriceField.getDocument()).setDocumentFilter(new InputFilter(10, "DIGITS_ONLY"));
        ((AbstractDocument) formExpressExtraPriceField.getDocument()).setDocumentFilter(new InputFilter(10, "DIGITS_ONLY"));
        ((AbstractDocument) formIntercityExtraPriceField.getDocument()).setDocumentFilter(new InputFilter(10, "DIGITS_ONLY"));
        ((AbstractDocument) formMaxHoursField.getDocument()).setDocumentFilter(new InputFilter(3, "DIGITS_ONLY"));
        ((AbstractDocument) formCitiesField.getDocument()).setDocumentFilter(new InputFilter(100, "ANY"));

        // --- Action Buttons ---
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        addButton = new JButton("Add New");
        editButton = new JButton("Edit Selected");
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        removeButton = new JButton("Remove Selected");
        backToDashboardButton = new JButton("Back to Dashboard");

        JButton[] buttons = {addButton, editButton, saveButton, cancelButton, removeButton, backToDashboardButton};
        for (JButton btn : buttons) {
            btn.addActionListener(this);
            actionButtonsPanel.add(btn);
        }
        formContainer.add(actionButtonsPanel, BorderLayout.SOUTH);
        
        return formContainer;
    }

    private void resetToInitialState() {
        isAddingMode = false;
        table.clearSelection();
        clearFormFields();
        setFormEnabled(false);
        
        addButton.setEnabled(true);
        editButton.setEnabled(false);
        saveButton.setEnabled(false);
        removeButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }
    
    private void setFormEnabled(boolean enabled) {
        formTaxiCodeField.setEnabled(false);
        formAvailableCheckBox.setEnabled(enabled);
        formMinPriceField.setEnabled(enabled);
        formRegularRadio.setEnabled(isAddingMode);
        formExpressRadio.setEnabled(isAddingMode);
        formIntercityRadio.setEnabled(isAddingMode);
        
        formCityTaxiCheckBox.setEnabled(enabled);
        formExpressExtraPriceField.setEnabled(enabled);
        formCitiesField.setEnabled(enabled);
        formIntercityExtraPriceField.setEnabled(enabled);
        formMaxHoursField.setEnabled(enabled);
    }
    
    private void clearFormFields() {
        formTaxiCodeField.setText("");
        formAvailableCheckBox.setSelected(false);
        formMinPriceField.setText("");
        formRegularRadio.setSelected(true);
        specificFieldsCardLayout.show(specificFieldsPanel, "REGULAR");

        formCityTaxiCheckBox.setSelected(false);
        formExpressExtraPriceField.setText("");
        formCitiesField.setText("");
        formIntercityExtraPriceField.setText("");
        formMaxHoursField.setText("");
    }
    
    private String generateNewTaxiId() {
        int maxId = 0;
        for (Taxi taxi : ourSystem.getTaxis()) {
            try {
                int currentId = Integer.parseInt(taxi.getTaxiCode().substring(1));
                if (currentId > maxId) maxId = currentId;
            } catch (Exception e) { /* Ignore */ }
        }
        return "T" + (maxId + 1);
    }

    private void populateTable() {
        model.setRowCount(0); 
        ArrayList<Taxi> taxis = ourSystem.getTaxis();
        taxis.sort(Comparator.comparing(t -> Integer.parseInt(t.getTaxiCode().substring(1))));

        for (Taxi taxi : taxis) {
            String taxiType = "Regular";
            String extraInfo = "";
            if (taxi instanceof ExpressTaxi) {
                taxiType = "Express";
                ExpressTaxi et = (ExpressTaxi) taxi;
                extraInfo = "City Taxi: " + et.isCityTaxi() + ", Extra: " + et.getExtraPrice();
            } else if (taxi instanceof IntercityTaxi) {
                taxiType = "Intercity";
                IntercityTaxi it = (IntercityTaxi) taxi;
                extraInfo = "Cities: " + it.getCities() + ", Extra: " + it.getExtraPrice() + ", Hours: " + it.getMaxHours();
            }
            model.addRow(new Object[]{taxi.getTaxiCode(), taxi.isAvailable(), taxi.getMinPrice(), taxiType, extraInfo});
        }
    }
    
    private void populateDetailsFromTable() {
        if (selectedRow != -1) {
            Taxi taxi = findTaxiById(model.getValueAt(selectedRow, 0).toString());
            if (taxi == null) return;
            
            formTaxiCodeField.setText(taxi.getTaxiCode());
            formAvailableCheckBox.setSelected(taxi.isAvailable());
            formMinPriceField.setText(String.valueOf(taxi.getMinPrice()));
            
            if (taxi instanceof ExpressTaxi) {
                formExpressRadio.setSelected(true);
                ExpressTaxi et = (ExpressTaxi) taxi;
                formCityTaxiCheckBox.setSelected(et.isCityTaxi());
                formExpressExtraPriceField.setText(String.valueOf(et.getExtraPrice()));
                specificFieldsCardLayout.show(specificFieldsPanel, "EXPRESS");
            } else if (taxi instanceof IntercityTaxi) {
                formIntercityRadio.setSelected(true);
                IntercityTaxi it = (IntercityTaxi) taxi;
                formCitiesField.setText(String.join(",", it.getCities()));
                formIntercityExtraPriceField.setText(String.valueOf(it.getExtraPrice()));
                formMaxHoursField.setText(String.valueOf(it.getMaxHours()));
                specificFieldsCardLayout.show(specificFieldsPanel, "INTERCITY");
            } else {
                formRegularRadio.setSelected(true);
                specificFieldsCardLayout.show(specificFieldsPanel, "REGULAR");
            }
        }
    }
    
    private boolean validateInput(String minPrice, boolean isExpress, String expressPrice, boolean isIntercity, String intercityPrice, String maxHours, String cities) {
        try {
            if (Double.parseDouble(minPrice) <= 0) {
                JOptionPane.showMessageDialog(this, "Min Price must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (isExpress && Double.parseDouble(expressPrice) <= 0) {
                JOptionPane.showMessageDialog(this, "Express Extra Price must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (isIntercity) {
                if (Double.parseDouble(intercityPrice) <= 0) {
                    JOptionPane.showMessageDialog(this, "Intercity Extra Price must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (Integer.parseInt(maxHours) <= 0) {
                    JOptionPane.showMessageDialog(this, "Max Hours must be a positive number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (cities.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Cities cannot be empty for Intercity taxi.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for prices and hours.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private Taxi findTaxiById(String taxiId) {
        return ourSystem.getTaxis().stream().filter(t -> t.getTaxiCode().equals(taxiId)).findFirst().orElse(null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == addButton) {
            isAddingMode = true;
            table.clearSelection();
            clearFormFields();
            setFormEnabled(true);
            formTaxiCodeField.setText(generateNewTaxiId());
            saveButton.setText("Add Taxi");
            saveButton.setEnabled(true);
            cancelButton.setEnabled(true);
            addButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
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
            String code = formTaxiCodeField.getText();
            boolean available = formAvailableCheckBox.isSelected();
            String minPriceStr = formMinPriceField.getText();

            boolean isExpress = formExpressRadio.isSelected();
            String expressPriceStr = formExpressExtraPriceField.getText();
            
            boolean isIntercity = formIntercityRadio.isSelected();
            String intercityPriceStr = formIntercityExtraPriceField.getText();
            String maxHoursStr = formMaxHoursField.getText();
            String citiesStr = formCitiesField.getText();
            
            if (!validateInput(minPriceStr, isExpress, expressPriceStr, isIntercity, intercityPriceStr, maxHoursStr, citiesStr)) {
                return;
            }
            
            double minPrice = Double.parseDouble(minPriceStr);

            if (isAddingMode) {
                Taxi newTaxi = null;
                if(formRegularRadio.isSelected()){
                    newTaxi = new Taxi(code, available, minPrice);
                } else if (isExpress) {
                    newTaxi = new ExpressTaxi(code, available, minPrice, formCityTaxiCheckBox.isSelected(), Double.parseDouble(expressPriceStr));
                } else if (isIntercity) {
                    ArrayList<String> cities = new ArrayList<>(Arrays.asList(citiesStr.split(",")));
                    newTaxi = new IntercityTaxi(code, available, minPrice, cities, Double.parseDouble(intercityPriceStr), Integer.parseInt(maxHoursStr));
                }
                
                if (newTaxi != null && ourSystem.addTaxi(newTaxi)) {
                    JOptionPane.showMessageDialog(this, "Taxi added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    resetToInitialState();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add taxi. ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else { // Editing
                Taxi taxiToUpdate = findTaxiById(code);
                if (taxiToUpdate != null) {
                    taxiToUpdate.setAvailable(available);
                    taxiToUpdate.setMinPrice(minPrice);
                    if (taxiToUpdate instanceof ExpressTaxi) {
                        ((ExpressTaxi) taxiToUpdate).setCityTaxi(formCityTaxiCheckBox.isSelected());
                        ((ExpressTaxi) taxiToUpdate).setExtraPrice(Double.parseDouble(expressPriceStr));
                    } else if (taxiToUpdate instanceof IntercityTaxi) {
                        ((IntercityTaxi) taxiToUpdate).setCities(new ArrayList<>(Arrays.asList(citiesStr.split(","))));
                        ((IntercityTaxi) taxiToUpdate).setExtraPrice(Double.parseDouble(intercityPriceStr));
                        ((IntercityTaxi) taxiToUpdate).setMaxHours(Integer.parseInt(maxHoursStr));
                    }
                    JOptionPane.showMessageDialog(this, "Taxi " + code + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    populateTable();
                    resetToInitialState();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update taxi. Not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else if (source == cancelButton) {
            resetToInitialState();
        }
        else if (source == removeButton) {
            if (selectedRow != -1) {
                String taxiId = model.getValueAt(selectedRow, 0).toString();
                if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + taxiId + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Taxi taxiToRemove = findTaxiById(taxiId);
                    if (ourSystem.removeTaxi(taxiToRemove)) {
                        JOptionPane.showMessageDialog(this, "Taxi " + taxiId + " removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        populateTable();
                        resetToInitialState();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove taxi " + taxiId + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        else if (source == backToDashboardButton) {
            parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL); 
        }
    }
}