package GUI;

import Control.systemDataBase;
import Model.MainManager;
import Model.Manager;
import Model.Subscription;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainApplicationFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private JPanel cards;
    private LoginPanel loginPanel;
    private MainManagerPanel mainManagerPanel;
    private ManagerPanel managerPanel;
    private SubscriberPanel subscriberPanel;

    // Card names for CardLayout
    public static final String LOGIN_PANEL = "LoginPanel";
    public static final String MAIN_MANAGER_PANEL = "MainManagerPanel";
    public static final String MANAGER_PANEL = "ManagerPanel";
    public static final String SUBSCRIBER_PANEL = "SubscriberPanel";

    // Navigation buttons
    private JPanel navPanel;
    private JButton navManageSubscribers;
    private JButton navManageManagers;
    private JButton navManageTaxis;
    private JButton navAssignTaxiToManager;
    private JButton navManageOrders;
    private JButton navReports;
    private JButton navDownloadData;
    private JButton navLogout;

    private Object currentUser;

    public MainApplicationFrame(systemDataBase system) {
        this.ourSystem = system;
        setTitle("Taxi Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setSize(1200, 800); // Keep size for non-maximized state
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // --- Navigation Panel (Left Sidebar) ---
        navPanel = new JPanel();
        navPanel.setLayout(new GridBagLayout());
        navPanel.setPreferredSize(new Dimension(200, 700));
        navPanel.setBackground(new Color(230, 230, 230));
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        navManageSubscribers = new JButton("Manage Subscribers");
        navManageManagers = new JButton("Manage Managers");
        navManageTaxis = new JButton("Manage Taxis");
        navAssignTaxiToManager = new JButton("Assign Taxi to Manager");
        navManageOrders = new JButton("Manage Orders");
        navReports = new JButton("Reports");
        navDownloadData = new JButton("Download Data");
        navLogout = new JButton("Logout");

        JButton[] navButtons = {navManageSubscribers, navManageManagers, navManageTaxis, navAssignTaxiToManager, navManageOrders, navReports, navDownloadData};
        for (JButton btn : navButtons) {
            btn.addActionListener(this);
            gbc.weighty = 0;
            navPanel.add(btn, gbc);
        }

        gbc.weighty = 1;
        navPanel.add(Box.createVerticalGlue(), gbc);

        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTH;
        navLogout.addActionListener(this);
        navPanel.add(navLogout, gbc);

        add(navPanel, BorderLayout.WEST);

        // --- Main Content Area (CardLayout) ---
        cards = new JPanel(new CardLayout());
        loginPanel = new LoginPanel(ourSystem, this);
        cards.add(loginPanel, LOGIN_PANEL);
        add(cards, BorderLayout.CENTER);

        setNavButtonsPrivileges(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ourSystem.saveDataToDB();
            }
        });
    }

    public void setNavButtonsPrivileges(Object user) {
        this.currentUser = user;
        boolean isMainManager = (user instanceof MainManager);
        navPanel.setVisible(isMainManager);
    }

    public void showPanel(String panelName, Object userObject) {
        CardLayout cl = (CardLayout) (cards.getLayout());

        if (mainManagerPanel != null)
            cards.remove(mainManagerPanel);
        if (managerPanel != null)
            cards.remove(managerPanel);
        if (subscriberPanel != null)
            cards.remove(subscriberPanel);

        mainManagerPanel = null;
        managerPanel = null;
        subscriberPanel = null;

        switch (panelName) {
            case LOGIN_PANEL:
                setNavButtonsPrivileges(null);
                break;
            case MAIN_MANAGER_PANEL:
                mainManagerPanel = new MainManagerPanel(ourSystem, this, (MainManager) userObject);
                cards.add(mainManagerPanel, MAIN_MANAGER_PANEL);
                setNavButtonsPrivileges(userObject);
                break;
            case MANAGER_PANEL:
                managerPanel = new ManagerPanel(ourSystem, this, (Manager) userObject);
                cards.add(managerPanel, MANAGER_PANEL);
                setNavButtonsPrivileges(userObject);
                break;
            case SUBSCRIBER_PANEL:
                subscriberPanel = new SubscriberPanel(ourSystem, this, (Subscription) userObject);
                cards.add(subscriberPanel, SUBSCRIBER_PANEL);
                setNavButtonsPrivileges(userObject);
                break;
            default:
                System.err.println("Unknown panel name: " + panelName);
                break;
        }
        cl.show(cards, panelName);
        this.setTitle("Taxi Management System - " + panelName.replace("Panel", ""));
        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentUser == null)
            return;

        Object source = e.getSource();

        if (source == navManageSubscribers) {
            mainManagerPanel.showSubPanel(MainManagerPanel.SHOW_SUBSCRIBERS_PANEL);
        } else if (source == navManageManagers) {
            mainManagerPanel.showSubPanel(MainManagerPanel.SHOW_MANAGERS_PANEL);
        } else if (source == navManageTaxis) {
            mainManagerPanel.showSubPanel(MainManagerPanel.SHOW_TAXIS_PANEL);
        } else if (source == navAssignTaxiToManager) {
            mainManagerPanel.showSubPanel(MainManagerPanel.ASSIGN_TAXI_TO_MANAGER_PANEL);
        } else if (source == navManageOrders) {
            if (currentUser instanceof MainManager) {
                mainManagerPanel.showSubPanel(MainManagerPanel.SHOW_ORDERS_PANEL);
            }
        } else if (source == navReports) {
            mainManagerPanel.showSubPanel(MainManagerPanel.GET_FREE_TAXIS_PANEL);
        } else if (source == navDownloadData) {
            mainManagerPanel.showSubPanel(MainManagerPanel.DOWNLOAD_DATA_PANEL);
        } else if (source == navLogout) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                showPanel(LOGIN_PANEL, null);
            }
        }
    }
}