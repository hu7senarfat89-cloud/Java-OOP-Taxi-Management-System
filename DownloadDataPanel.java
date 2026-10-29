package GUI;

import Control.systemDataBase;
import Model.MainManager;
import Model.Manager;
import Model.Order;
import Model.Subscription;
import Model.Taxi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class DownloadDataPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private MainManagerPanel parentPanel;

    private JButton backButton;

    public DownloadDataPanel(systemDataBase system, MainManagerPanel parentPanel) {
        this.ourSystem = system;
        this.parentPanel = parentPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Title ---
        JLabel titleLabel = new JLabel("Download System Data", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // --- Main Grid for Cards ---
        JPanel cardsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Create and add cards
        gbc.gridx = 0; gbc.gridy = 0;
        cardsPanel.add(createDownloadCard(
            "Managers",
            "Download details of all regular managers.",
            "SystemManagers.txt",
            "\uD83D\uDC64" // User icon
        ), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        cardsPanel.add(createDownloadCard(
            "Subscribers",
            "Download details of all subscribers.",
            "members.txt",
            "\uD83D\uDC65" // Users icon
        ), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        cardsPanel.add(createDownloadCard(
            "Orders",
            "Download details of all orders.",
            "orders.txt",
            "\uD83D\uDCDD" // Document icon
        ), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        cardsPanel.add(createDownloadCard(
            "Taxis",
            "Download details of all taxis and their managers.",
            "Taxi.txt",
            "\uD83D\uDE95" // Taxi icon
        ), gbc);

        add(cardsPanel, BorderLayout.CENTER);

        // --- Back Button Panel ---
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(this);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createDownloadCard(String title, String description, String defaultFileName, String iconText) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea descArea = new JTextArea(description);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(getBackground());
        contentPanel.add(descArea, BorderLayout.CENTER);

        JButton button = new JButton("Download");
        button.setActionCommand(defaultFileName); // Use file name as command
        button.addActionListener(this);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.add(button);
        contentPanel.add(buttonWrapper, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof JButton) {
            String command = ((JButton) source).getActionCommand();
            switch (command) {
                case "SystemManagers.txt":
                    downloadManagers();
                    break;
                case "members.txt":
                    downloadSubscribers();
                    break;
                case "orders.txt":
                    downloadOrders();
                    break;
                case "Taxi.txt":
                    downloadTaxis();
                    break;
                default:
                    if (source == backButton) {
                        parentPanel.showSubPanel(MainManagerPanel.WELCOME_PANEL);
                    }
                    break;
            }
        }
    }

    private void downloadManagers() {
        File downloadDir = new File("src/downloadHere");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs(); // Create the directory if it doesn't exist
        }
        File fileToSave = new File(downloadDir, "SystemManagers.txt");
        try (FileWriter writer = new FileWriter(fileToSave)) {
            ArrayList<Manager> managers = ourSystem.getManagers().stream()
                    .filter(m -> !(m instanceof MainManager))
                    .collect(Collectors.toCollection(ArrayList::new));

            // Corrected numerical sort
            managers.sort(Comparator.comparingInt(m -> Integer.parseInt(m.getId().substring(1))));

            for (Manager manager : managers) {
                writer.write(manager.getId() + " " +
                             manager.getFirstName() + " " +
                             manager.getLastName() + " " +
                             manager.getPhone() + " " +
                             manager.getAddress() + System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this, "Managers data downloaded successfully to " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadSubscribers() {
        File downloadDir = new File("src/downloadHere");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs(); // Create the directory if it doesn't exist
        }
        File fileToSave = new File(downloadDir, "members.txt");
        try (FileWriter writer = new FileWriter(fileToSave)) {
            ArrayList<Subscription> subscriptions = new ArrayList<>(ourSystem.getSubscriptions());
            
            // Corrected: Sort by last name
            subscriptions.sort(Comparator.comparing(Subscription::getLastName));

            for (Subscription sub : subscriptions) {
                writer.write(sub.getSubCode() + " " +
                             sub.getFirstName() + " " +
                             sub.getLastName() + " " +
                             sub.getAddress() + " " +
                             sub.getPhone() + System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this, "Subscribers data downloaded successfully to " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadOrders() {
        File downloadDir = new File("src/downloadHere");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs(); // Create the directory if it doesn't exist
        }
        File fileToSave = new File(downloadDir, "orders.txt");
        try (FileWriter writer = new FileWriter(fileToSave)) {
            ArrayList<Order> orders = new ArrayList<>(ourSystem.getOrders());
            
            // Corrected numerical sort
            orders.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getOrderNum().substring(1))));

            for (Order order : orders) {
                writer.write(order.getOrderNum() + " " +
                             order.getManagerCode() + " " +
                             order.getDay() + " " +
                             order.getMonth() + " " +
                             order.getHour() + " " +
                             order.getSubCode() + " " +
                             order.getTaxi().getTaxiCode() + " " +
                             order.getOrderPrice() + System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this, "Orders data downloaded successfully to " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadTaxis() {
        File downloadDir = new File("src/downloadHere");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs(); // Create the directory if it doesn't exist
        }
        File fileToSave = new File(downloadDir, "Taxi.txt");
        try (FileWriter writer = new FileWriter(fileToSave)) {
            ArrayList<Taxi> taxis = new ArrayList<>(ourSystem.getTaxis());
            
            // Corrected numerical sort
            taxis.sort(Comparator.comparingInt(t -> Integer.parseInt(t.getTaxiCode().substring(1))));

            for (Taxi taxi : taxis) {
                writer.write(taxi.toString() + System.lineSeparator());
                writer.write("  Responsible Managers:" + System.lineSeparator());
                boolean foundManager = false;
                for (Manager manager : ourSystem.getManagers()) {
                    for (Taxi mTaxi : manager.getTaxis()) {
                        if (mTaxi.getTaxiCode().equals(taxi.getTaxiCode())) {
                            writer.write("    ID: " + manager.getId() + ", Name: " + manager.getFirstName() + " " + manager.getLastName() + System.lineSeparator());
                            foundManager = true;
                        }
                    }
                }
                if (!foundManager) {
                    writer.write("    None" + System.lineSeparator());
                }
                writer.write(System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this, "Taxis data downloaded successfully to " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}