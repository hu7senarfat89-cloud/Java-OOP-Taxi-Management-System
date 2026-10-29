package GUI;

import Control.systemDataBase;
import Model.Station;
import Model.Taxi;
import Model.ExpressTaxi;
import Model.IntercityTaxi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GetFreeTaxisPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JComboBox<String> stationComboBox;
    private JPanel taxisDisplayPanel; // Panel to show taxi details
    private JButton backButton;
    private systemDataBase ourSystem;
    private MainManagerPanel parentPanel;

    public GetFreeTaxisPanel(systemDataBase system, MainManagerPanel parentPanel) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Title ---
        JLabel titleLabel = new JLabel("Free Taxis by Station", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // --- Selection Panel ---
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        selectionPanel.add(new JLabel("Select Station:"));
        stationComboBox = new JComboBox<>();
        stationComboBox.addActionListener(this); // Action listener for real-time updates
        selectionPanel.add(stationComboBox);
        add(selectionPanel, BorderLayout.NORTH);

        // --- Panel for Displaying Taxis ---
        taxisDisplayPanel = new JPanel();
        taxisDisplayPanel.setLayout(new GridLayout(0, 3, 10, 10)); // 3 columns, auto rows
        JScrollPane scrollPane = new JScrollPane(taxisDisplayPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Taxis"));
        add(scrollPane, BorderLayout.CENTER);

        // --- Back Button ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(this);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        populateStations();
    }

    private void populateStations() {
        stationComboBox.removeAllItems();
        if (ourSystem.getStations().isEmpty()) {
            stationComboBox.addItem("No stations available");
            stationComboBox.setEnabled(false);
        } else {
            stationComboBox.setEnabled(true);
            stationComboBox.addItem("--- Select a Station ---");
            for (Station station : ourSystem.getStations()) {
                stationComboBox.addItem(station.getStationName());
            }
        }
    }

    private void updateTaxisDisplay() {
        taxisDisplayPanel.removeAll();

        if (stationComboBox.getSelectedIndex() <= 0) {
            taxisDisplayPanel.revalidate();
            taxisDisplayPanel.repaint();
            return;
        }

        String selectedStationName = (String) stationComboBox.getSelectedItem();
        Station chosenStation = ourSystem.getStations().stream()
                .filter(s -> s.getStationName().equals(selectedStationName))
                .findFirst()
                .orElse(null);

        if (chosenStation != null) {
            ArrayList<String> freeTaxiCodes = ourSystem.getFreeTaxis(chosenStation);
            if (freeTaxiCodes.isEmpty()) {
                JLabel noTaxisLabel = new JLabel("No free taxis found in this station.", SwingConstants.CENTER);
                noTaxisLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                taxisDisplayPanel.setLayout(new BorderLayout());
                taxisDisplayPanel.add(noTaxisLabel, BorderLayout.CENTER);
            } else {
                taxisDisplayPanel.setLayout(new GridLayout(0, 3, 10, 10)); // Reset layout
                for (String taxiCode : freeTaxiCodes) {
                    Taxi taxi = ourSystem.getTaxis().stream()
                            .filter(t -> t.getTaxiCode().equals(taxiCode))
                            .findFirst().orElse(null);
                    if (taxi != null) {
                        taxisDisplayPanel.add(createTaxiDetailCard(taxi));
                    }
                }
            }
        }
        
        taxisDisplayPanel.revalidate();
        taxisDisplayPanel.repaint();
    }
    
    private JPanel createTaxiDetailCard(Taxi taxi) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // --- Title: Taxi Code ---
        JLabel taxiCodeLabel = new JLabel(taxi.getTaxiCode(), SwingConstants.CENTER);
        taxiCodeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        taxiCodeLabel.setOpaque(true);
        taxiCodeLabel.setBackground(new Color(220, 220, 220));
        card.add(taxiCodeLabel, BorderLayout.NORTH);

        // --- Details Panel ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        String type = "Regular";
        if (taxi instanceof ExpressTaxi) type = "Express";
        if (taxi instanceof IntercityTaxi) type = "Intercity";
        
        detailsPanel.add(createDetailRow("Type:", type), gbc);
        detailsPanel.add(createDetailRow("Min Price:", String.format("%.2f", taxi.getMinPrice())), gbc);
        
        if (taxi instanceof ExpressTaxi) {
            ExpressTaxi et = (ExpressTaxi) taxi;
            detailsPanel.add(createDetailRow("City Taxi:", et.isCityTaxi() ? "Yes" : "No"), gbc);
            detailsPanel.add(createDetailRow("Extra Price:", String.format("%.2f", et.getExtraPrice())), gbc);
        } else if (taxi instanceof IntercityTaxi) {
            IntercityTaxi it = (IntercityTaxi) taxi;
             detailsPanel.add(createDetailRow("Extra Price:", String.format("%.2f", it.getExtraPrice())), gbc);
            detailsPanel.add(createDetailRow("Max Hours:", String.valueOf(it.getMaxHours())), gbc);
        }

        card.add(detailsPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 12));
        row.add(labelComponent);
        row.add(new JLabel(value));
        return row;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == stationComboBox) {
            updateTaxisDisplay();
        } else if (e.getSource() == backButton) {
            parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL);
        }
    }
}