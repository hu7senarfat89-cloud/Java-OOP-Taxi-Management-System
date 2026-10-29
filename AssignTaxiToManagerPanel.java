package GUI;

import Control.systemDataBase;
import Model.Manager;
import Model.MainManager;
import Model.Taxi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AssignTaxiToManagerPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private MainManagerPanel parentPanel;

    private JTable assignmentsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> taxiCodeComboBox;
    private JComboBox<String> managerIdComboBox;
    private JButton assignButton;
    private JButton backButton;

    public AssignTaxiToManagerPanel(systemDataBase system, MainManagerPanel parentPanel) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Title ---
        JLabel titleLabel = new JLabel("Assign Taxi to Manager", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);
        
        // --- Assignments Table ---
        String[] columnNames = {"Manager ID", "Manager Name", "Assigned Taxi Code"};
        tableModel = new DefaultTableModel(columnNames, 0) {
             private static final long serialVersionUID = 1L;

			 @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        assignmentsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(assignmentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Assignments"));

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("New Assignment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Select Taxi:"), gbc); // Updated Label
        gbc.gridx = 1; taxiCodeComboBox = new JComboBox<>(); formPanel.add(taxiCodeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Select Manager:"), gbc);
        gbc.gridx = 1; managerIdComboBox = new JComboBox<>(); formPanel.add(managerIdComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        assignButton = new JButton("Assign Taxi");
        assignButton.addActionListener(this);
        buttonPanel.add(assignButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; formPanel.add(buttonPanel, gbc);

        // --- Split Pane Layout ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, formPanel);
        splitPane.setResizeWeight(0.6);
        add(splitPane, BorderLayout.CENTER);

        // --- Back Button ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(this);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        // Initial data population
        refreshAllData();
    }

    private void populateAssignmentsTable() {
        tableModel.setRowCount(0);
        for (Manager manager : ourSystem.getManagers()) {
            if (manager.getTaxis() != null && !manager.getTaxis().isEmpty()) {
                for (Taxi taxi : manager.getTaxis()) {
                    tableModel.addRow(new Object[]{
                        manager.getId(),
                        manager.getFirstName() + " " + manager.getLastName(),
                        taxi.getTaxiCode()
                    });
                }
            }
        }
    }
    
    private void populateTaxiCodes() {
        taxiCodeComboBox.removeAllItems();
        
        // ** THIS IS THE FIX: Show ALL taxis, not just unassigned ones **
        ArrayList<Taxi> allTaxis = ourSystem.getTaxis();
        if (allTaxis.isEmpty()) {
            taxiCodeComboBox.addItem("No taxis available");
        } else {
            for (Taxi taxi : allTaxis) {
                taxiCodeComboBox.addItem(taxi.getTaxiCode());
            }
        }
    }

    private void populateManagerIds() {
        managerIdComboBox.removeAllItems();
        ArrayList<Manager> regularManagers = new ArrayList<>();
        if (ourSystem.getManagers() != null) {
            for (Manager manager : ourSystem.getManagers()) {
                if (!(manager instanceof MainManager)) {
                    regularManagers.add(manager);
                }
            }
        }
        
        if (regularManagers.isEmpty()) {
            managerIdComboBox.addItem("No regular managers available");
        } else {
            for (Manager manager : regularManagers) {
                managerIdComboBox.addItem(manager.getId() + " - " + manager.getFirstName() + " " + manager.getLastName());
            }
        }
    }

    private void updateAssignButtonState() {
        boolean taxisAvailable = taxiCodeComboBox.getItemCount() > 0 && !taxiCodeComboBox.getSelectedItem().toString().contains("No taxis");
        boolean managersAvailable = managerIdComboBox.getItemCount() > 0 && !managerIdComboBox.getSelectedItem().toString().contains("No regular managers");
        assignButton.setEnabled(taxisAvailable && managersAvailable);
    }
    
    private void refreshAllData() {
        populateAssignmentsTable();
        populateTaxiCodes();
        populateManagerIds();
        updateAssignButtonState();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == assignButton) {
            String selectedTaxiInfo = (String) taxiCodeComboBox.getSelectedItem();
            String selectedManagerInfo = (String) managerIdComboBox.getSelectedItem();

            if (selectedTaxiInfo == null || selectedManagerInfo == null || selectedTaxiInfo.contains("No taxis") || selectedManagerInfo.contains("No managers")) {
                JOptionPane.showMessageDialog(this, "Please select both a taxi and a manager.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String taxiCode = selectedTaxiInfo.split(" ")[0];
            String managerId = selectedManagerInfo.split(" - ")[0]; 

            Taxi taxi = ourSystem.getTaxis().stream().filter(t -> t.getTaxiCode().equals(taxiCode)).findFirst().orElse(null);
            Manager manager = ourSystem.getManagers().stream().filter(m -> m.getId().equals(managerId)).findFirst().orElse(null);

            if (taxi == null || manager == null) {
                JOptionPane.showMessageDialog(this, "Selected taxi or manager not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (manager.addTaxi(taxi)) {
                JOptionPane.showMessageDialog(this, "Taxi " + taxiCode + " assigned to Manager " + manager.getFirstName() + " successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAllData(); // Refresh the table and combo boxes
            } else {
                JOptionPane.showMessageDialog(this, "Manager already has this taxi.", "Assignment Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == backButton) {
            parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL); 
        }
    }
}