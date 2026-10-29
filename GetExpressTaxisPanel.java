package GUI;

import Control.systemDataBase;
import Model.ExpressTaxi;
import Model.Subscription;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GetExpressTaxisPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JComboBox<String> subscriberComboBox;
    private JTextArea resultArea;
    private JButton showButton;
    private JButton backButton;
    private systemDataBase ourSystem;
    private MainManagerPanel parentPanel;

    public GetExpressTaxisPanel(systemDataBase system, MainManagerPanel parentPanel) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Express Taxis by Subscriber", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Subscriber:"));
        subscriberComboBox = new JComboBox<>();
        selectionPanel.add(subscriberComboBox);

        // Initialize showButton BEFORE calling populateSubscribers()
        showButton = new JButton("Show Express Taxis");
        showButton.addActionListener(this);
        selectionPanel.add(showButton);

        add(selectionPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);

        backButton = new JButton("Back");
        backButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Now call populateSubscribers after showButton is initialized
        populateSubscribers();
    }

    private void populateSubscribers() {
        subscriberComboBox.removeAllItems();
        if (ourSystem.getSubscriptions().isEmpty()) {
            subscriberComboBox.addItem("No subscribers available");
            showButton.setEnabled(false); // Now showButton is initialized
            return;
        }
        for (Subscription sub : ourSystem.getSubscriptions()) {
            subscriberComboBox.addItem(sub.getSubCode() + " - " + sub.getFirstName() + " " + sub.getLastName());
        }
        showButton.setEnabled(true); // Now showButton is initialized
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showButton) {
            String selectedSubInfo = (String) subscriberComboBox.getSelectedItem();

            if (selectedSubInfo == null || selectedSubInfo.equals("No subscribers available")) {
                JOptionPane.showMessageDialog(this, "Please select a subscriber.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                resultArea.setText("");
                return;
            }

            String subCode = selectedSubInfo.split(" - ")[0];
            Subscription chosenSub = null;
            for (Subscription sub : ourSystem.getSubscriptions()) {
                if (sub.getSubCode().equals(subCode)) {
                    chosenSub = sub;
                    break;
                }
            }

            if (chosenSub != null) {
                ArrayList<ExpressTaxi> expressTaxis = ourSystem.getExpressTaxis(chosenSub);
                if (expressTaxis.isEmpty()) {
                    resultArea.setText("No express taxis found for subscriber " + chosenSub.getFirstName() + " " + chosenSub.getLastName() + ".");
                } else {
                    StringBuilder sb = new StringBuilder("Express taxis for " + chosenSub.getFirstName() + " " + chosenSub.getLastName() + ":\n");
                    for (ExpressTaxi taxi : expressTaxis) {
                        sb.append("- ").append(taxi.getTaxiCode())
                          .append(" (Min Price: ").append(taxi.getMinPrice())
                          .append(", Extra Price: ").append(taxi.getExtraPrice()).append(")\n");
                    }
                    resultArea.setText(sb.toString());
                }
            } else {
                resultArea.setText("Error: Subscriber not found (should not happen if populated correctly).");
            }
        } else if (e.getSource() == backButton) {
            parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL);
        }
    }
}