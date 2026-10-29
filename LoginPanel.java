package GUI;

import Control.systemDataBase;
import Model.MainManager;
import Model.Manager;
import Model.Subscription;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LoginPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private MainApplicationFrame parentFrame;

    // Login type selection
    private JRadioButton mainManagerRadio;
    private JRadioButton managerRadio;
    private JRadioButton subscriberRadio;

    // Panels for different login fields
    private JPanel fieldsPanel;
    private CardLayout fieldsCardLayout;

    // Fields for Main Manager
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Field for Regular Manager
    private JTextField managerIdField;

    // Field for Subscriber
    private JTextField subscriberIdField;

    private JButton loginButton;
    private JButton exitButton;

    public LoginPanel(systemDataBase system, MainApplicationFrame parentFrame) {
        this.ourSystem = system;
        this.parentFrame = parentFrame;

        // Main panel setup
        setBackground(new Color(240, 245, 249)); // Light blue-gray background
        setLayout(new GridBagLayout()); // Center the loginBox

        // The white login box
        JPanel loginBox = new JPanel(new BorderLayout(10, 10));
        loginBox.setBackground(Color.WHITE);
        loginBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // --- Title and Subtitle ---
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Taxi Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Please select your login type");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);

        loginBox.add(titlePanel, BorderLayout.NORTH);

        // --- Main Content (Radio buttons and dynamic fields) ---
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);

        // Radio Buttons Panel
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        radioPanel.setBackground(Color.WHITE);
        mainManagerRadio = new JRadioButton("Main Manager");
        managerRadio = new JRadioButton("Regular Manager");
        subscriberRadio = new JRadioButton("Subscriber");
        
        mainManagerRadio.setBackground(Color.WHITE);
        managerRadio.setBackground(Color.WHITE);
        subscriberRadio.setBackground(Color.WHITE);

        ButtonGroup loginTypeGroup = new ButtonGroup();
        loginTypeGroup.add(mainManagerRadio);
        loginTypeGroup.add(managerRadio);
        loginTypeGroup.add(subscriberRadio);

        mainManagerRadio.addActionListener(this);
        managerRadio.addActionListener(this);
        subscriberRadio.addActionListener(this);

        radioPanel.add(mainManagerRadio);
        radioPanel.add(managerRadio);
        radioPanel.add(subscriberRadio);
        contentPanel.add(radioPanel, BorderLayout.NORTH);

        // Dynamic Fields Panel (using CardLayout)
        fieldsCardLayout = new CardLayout();
        fieldsPanel = new JPanel(fieldsCardLayout);
        fieldsPanel.setBackground(Color.WHITE);

        // Panel for Main Manager
        JPanel mainManagerPanel = createMainManagerPanel();
        fieldsPanel.add(mainManagerPanel, "MAIN_MANAGER");

        // Panel for Regular Manager
        JPanel managerPanel = createSingleFieldPanel("Manager ID:");
        managerIdField = (JTextField) ((JPanel) managerPanel.getComponent(0)).getComponent(1); // CORRECTED
        fieldsPanel.add(managerPanel, "REGULAR_MANAGER");

        // Panel for Subscriber
        JPanel subscriberPanel = createSingleFieldPanel("Subscriber ID:");
        subscriberIdField = (JTextField) ((JPanel) subscriberPanel.getComponent(0)).getComponent(1); // CORRECTED
        fieldsPanel.add(subscriberPanel, "SUBSCRIBER");
        
        contentPanel.add(fieldsPanel, BorderLayout.CENTER);
        loginBox.add(contentPanel, BorderLayout.CENTER);

        // --- Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        loginButton = new JButton("Login");
        exitButton = new JButton("Exit");
        
        loginButton.setPreferredSize(new Dimension(100, 30));
        exitButton.setPreferredSize(new Dimension(100, 30));

        loginButton.addActionListener(this);
        exitButton.addActionListener(this);

        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        loginBox.add(buttonPanel, BorderLayout.SOUTH);

        add(loginBox); // Add the white box to the main panel

        mainManagerRadio.setSelected(true); // Default selection
        fieldsCardLayout.show(fieldsPanel, "MAIN_MANAGER");
    }

    private JPanel createMainManagerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; usernameField = new JTextField(15); panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; passwordField = new JPasswordField(15); panel.add(passwordField, gbc);

        return panel;
    }

    private JPanel createSingleFieldPanel(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setBackground(Color.WHITE);
        
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; innerPanel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; innerPanel.add(new JTextField(15), gbc);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        return panel;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle radio button changes
        if (e.getSource() == mainManagerRadio) {
            fieldsCardLayout.show(fieldsPanel, "MAIN_MANAGER");
        } else if (e.getSource() == managerRadio) {
            fieldsCardLayout.show(fieldsPanel, "REGULAR_MANAGER");
        } else if (e.getSource() == subscriberRadio) {
            fieldsCardLayout.show(fieldsPanel, "SUBSCRIBER");
        }

        // Handle button clicks
        if (e.getSource() == loginButton) {
            boolean loginSuccessful = false;

            if (mainManagerRadio.isSelected()) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                for (Manager m : ourSystem.getManagers()) {
                    if (m instanceof MainManager) {
                        MainManager mm = (MainManager) m;
                        if (mm.getUserName().equals(username) && mm.getPassword().equals(password)) {
                            JOptionPane.showMessageDialog(this, "Welcome Main Manager " + mm.getFirstName(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
                            parentFrame.showPanel(MainApplicationFrame.MAIN_MANAGER_PANEL, mm);
                            loginSuccessful = true;
                            break;
                        }
                    }
                }
                if (!loginSuccessful) {
                    try (Scanner scanner = new Scanner(new File("administrators.txt"))) {
                        while (scanner.hasNextLine()) {
                            String[] creds = scanner.nextLine().split(" ");
                            if (creds.length == 2 && creds[0].equals(username) && creds[1].equals(password)) {
                                MainManager tempManager = new MainManager(username, "Admin", "User", "N/A", "N/A", username, password);
                                JOptionPane.showMessageDialog(this, "Welcome Main Manager " + tempManager.getFirstName(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
                                parentFrame.showPanel(MainApplicationFrame.MAIN_MANAGER_PANEL, tempManager);
                                loginSuccessful = true;
                                break;
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        // File not found, handled by the final check
                    }
                }

            } else if (managerRadio.isSelected()) {
                String managerId = managerIdField.getText();
                for (Manager m : ourSystem.getManagers()) {
                    if (!(m instanceof MainManager) && m.getId().equals(managerId)) { 
                        JOptionPane.showMessageDialog(this, "Hello Manager " + m.getFirstName(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
                        parentFrame.showPanel(MainApplicationFrame.MANAGER_PANEL, m);
                        loginSuccessful = true;
                        break;
                    }
                }
            } else if (subscriberRadio.isSelected()) {
                String subCode = subscriberIdField.getText();
                for (Subscription s : ourSystem.getSubscriptions()) {
                    if (s.getSubCode().equals(subCode)) { 
                        JOptionPane.showMessageDialog(this, "Hello Subscriber " + s.getFirstName(), "Login Success", JOptionPane.INFORMATION_MESSAGE);
                        parentFrame.showPanel(MainApplicationFrame.SUBSCRIBER_PANEL, s);
                        loginSuccessful = true;
                        break;
                    }
                }
            }

            if (!loginSuccessful) {
                JOptionPane.showMessageDialog(this, "Login failed. Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == exitButton) {
            JOptionPane.showMessageDialog(this, "Goodbye!", "Exit", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }
}