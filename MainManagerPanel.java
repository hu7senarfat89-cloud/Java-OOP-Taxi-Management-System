package GUI;

import Control.systemDataBase;
import Model.MainManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainManagerPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private systemDataBase ourSystem;
    private MainManager mainManager;
    private MainApplicationFrame parentFrame;

    private JPanel cards; // Internal JPanel using CardLayout for sub-panels
    private JPanel welcomePanel; // Default panel

    // Card names for internal CardLayout
    public static final String WELCOME_PANEL = "WelcomePanel";
    public static final String SHOW_SUBSCRIBERS_PANEL = "ShowSubscribersPanel";
    public static final String SHOW_MANAGERS_PANEL = "ShowManagersPanel";
    public static final String SHOW_TAXIS_PANEL = "ShowTaxisPanel";
    public static final String SHOW_ORDERS_PANEL = "ShowOrdersPanel";
    public static final String GET_FREE_TAXIS_PANEL = "GetFreeTaxisPanel";
    public static final String GET_EXPRESS_TAXIS_PANEL = "GetExpressTaxisPanel";
    public static final String DOWNLOAD_DATA_PANEL = "DownloadDataPanel";
    public static final String ASSIGN_TAXI_TO_MANAGER_PANEL = "AssignTaxiToManagerPanel";

    public MainManagerPanel(systemDataBase system, MainApplicationFrame parentFrame, MainManager mainManager) {
        this.ourSystem = system;
        this.parentFrame = parentFrame;
        this.mainManager = mainManager;
        setLayout(new BorderLayout());

        cards = new JPanel(new CardLayout());

        // --- Create the new, beautiful Welcome Panel ---
        welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(new Color(245, 245, 245)); // A light grey background

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(welcomePanel.getBackground());

        // Title with Icon
        JLabel welcomeTitle = new JLabel("  Welcome, " + mainManager.getFirstName() + "!"); // Unicode for a bar chart/dashboard icon
        welcomeTitle.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle/Instruction
        JLabel welcomeSubtitle = new JLabel("Select an option from the left navigation to get started.");
        welcomeSubtitle.setFont(new Font("Arial", Font.ITALIC, 16));
        welcomeSubtitle.setForeground(Color.GRAY);
        welcomeSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(welcomeTitle);
        content.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        content.add(welcomeSubtitle);

        welcomePanel.add(content); // Add the content panel to the center
        cards.add(welcomePanel, WELCOME_PANEL);

        add(cards, BorderLayout.CENTER);
    }

    // Helper method to switch sub-panels within MainManagerPanel
    public void showSubPanel(String panelName) {
        CardLayout cl = (CardLayout) (cards.getLayout());

        Component[] components = cards.getComponents();
        for (Component comp : components) {
            if (!comp.equals(welcomePanel)) {
                cards.remove(comp);
            }
        }

        switch (panelName) {
            case WELCOME_PANEL:
                cl.show(cards, WELCOME_PANEL);
                break;
            case SHOW_SUBSCRIBERS_PANEL:
                SubscribersTablePanel subscribersTablePanel = new SubscribersTablePanel(ourSystem, this);
                cards.add(subscribersTablePanel, SHOW_SUBSCRIBERS_PANEL);
                cl.show(cards, SHOW_SUBSCRIBERS_PANEL);
                break;
            case SHOW_MANAGERS_PANEL:
                ManagersTablePanel managersTablePanel = new ManagersTablePanel(ourSystem, this);
                cards.add(managersTablePanel, SHOW_MANAGERS_PANEL);
                cl.show(cards, SHOW_MANAGERS_PANEL);
                break;
            case SHOW_TAXIS_PANEL:
                TaxisTablePanel taxisTablePanel = new TaxisTablePanel(ourSystem, this);
                cards.add(taxisTablePanel, SHOW_TAXIS_PANEL);
                cl.show(cards, SHOW_TAXIS_PANEL);
                break;
            case ASSIGN_TAXI_TO_MANAGER_PANEL:
                AssignTaxiToManagerPanel assignPanel = new AssignTaxiToManagerPanel(ourSystem, this);
                cards.add(assignPanel, ASSIGN_TAXI_TO_MANAGER_PANEL);
                cl.show(cards, ASSIGN_TAXI_TO_MANAGER_PANEL);
                break;
            case SHOW_ORDERS_PANEL:
                ManageOrdersPanel manageOrdersPanel = new ManageOrdersPanel(ourSystem, this, mainManager);
                cards.add(manageOrdersPanel, SHOW_ORDERS_PANEL);
                cl.show(cards, SHOW_ORDERS_PANEL);
                break;
            case GET_FREE_TAXIS_PANEL:
                GetFreeTaxisPanel getFreeTaxisPanel = new GetFreeTaxisPanel(ourSystem, this);
                cards.add(getFreeTaxisPanel, GET_FREE_TAXIS_PANEL);
                cl.show(cards, GET_FREE_TAXIS_PANEL);
                break;
            case GET_EXPRESS_TAXIS_PANEL:
                GetExpressTaxisPanel getExpressTaxisPanel = new GetExpressTaxisPanel(ourSystem, this);
                cards.add(getExpressTaxisPanel, GET_EXPRESS_TAXIS_PANEL);
                cl.show(cards, GET_EXPRESS_TAXIS_PANEL);
                break;
            case DOWNLOAD_DATA_PANEL:
                DownloadDataPanel downloadDataPanel = new DownloadDataPanel(ourSystem, this);
                cards.add(downloadDataPanel, DOWNLOAD_DATA_PANEL);
                cl.show(cards, DOWNLOAD_DATA_PANEL);
                break;
            default:
                System.err.println("Unknown sub-panel name: " + panelName);
                cl.show(cards, WELCOME_PANEL);
                break;
        }
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // This actionPerformed is effectively unused now that MainApplicationFrame handles navigation.
    }
}