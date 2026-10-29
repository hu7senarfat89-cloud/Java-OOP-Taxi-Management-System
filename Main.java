package View;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import Control.systemDataBase;
import GUI.MainApplicationFrame;
import Model.ExpressTaxi;
import Model.IntercityTaxi;
import Model.MainManager;
import Model.Manager;
import Model.Order;
import Model.Station;
import Model.Subscription;
import Model.Taxi;

public class Main {

    static systemDataBase ourSystem = new systemDataBase();

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        initializeData();
        SwingUtilities.invokeLater(() -> new MainApplicationFrame(ourSystem));
    }

    public static void initializeData() {
        File databaseDir = new File("src/database");
        if (!databaseDir.exists()) {
            databaseDir.mkdirs(); // Create the directory if it doesn't exist
        }
        File dbFile = new File(databaseDir, "DB.txt");

        try (Scanner scanner = new Scanner(dbFile)) { // Use dbFile
            String currentSection = "";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line;
                    continue;
                }

                if (line.isEmpty()) {
                    continue;
                }

                switch (currentSection) {
                    case "[Managers]":
                        parseManager(line);
                        break;
                    case "[Subscribers]":
                        parseSubscriber(line);
                        break;
                    case "[Taxis]":
                        parseTaxi(line);
                        break;
                    case "[Stations]":
                        parseStation(line);
                        break;
                    case "[ManagerTaxiAssignments]":
                        parseManagerTaxiAssignment(line);
                        break;
                    case "[Orders]":
                        parseOrder(line);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("DB.txt not found at " + dbFile.getAbsolutePath() + "!");
            e.printStackTrace();
        }
    }

    private static void parseManager(String line) {
        String[] parts = line.split(",");
        if (parts[0].startsWith("MM")) {
            ourSystem.addManager(new MainManager(parts[0], parts[3], parts[4], parts[5], parts[6], parts[1], parts[2]));
        } else {
            ourSystem.addManager(new Manager(parts[0], parts[1], parts[2], parts[3], parts[4]));
        }
    }

    private static void parseSubscriber(String line) {
        String[] parts = line.split(",");
        ourSystem.addSubscription(new Subscription(parts[0], parts[1], parts[2], parts[3], parts[4]));
    }

    private static void parseTaxi(String line) {
        String[] parts = line.split(",");
        String taxiCode = parts[0];
        // Taxis are available by default unless assigned to an order.
        boolean available = true; 
        double minPrice = Double.parseDouble(parts[2]);

        if (parts.length == 3) { // Regular Taxi
            ourSystem.addTaxi(new Taxi(taxiCode, available, minPrice));
        } else if (parts.length == 5) { // Express Taxi
            boolean cityTaxi = Boolean.parseBoolean(parts[3]);
            double extraPrice = Double.parseDouble(parts[4]);
            ourSystem.addTaxi(new ExpressTaxi(taxiCode, available, minPrice, cityTaxi, extraPrice));
        } else if (parts.length == 6) { // Intercity Taxi
            ArrayList<String> cities = new ArrayList<>(Arrays.asList(parts[3].replace("\"", "").split(",")));
            double extraPrice = Double.parseDouble(parts[4]);
            int maxHours = Integer.parseInt(parts[5]);
            ourSystem.addTaxi(new IntercityTaxi(taxiCode, available, minPrice, cities, extraPrice, maxHours));
        }
    }

    private static void parseStation(String line) {
        String[] parts = line.split(",");
        String stationName = parts[0].replace("\"", "");
        ArrayList<Taxi> taxis = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            String taxiCode = parts[i];
            ourSystem.getTaxis().stream()
                .filter(t -> t.getTaxiCode().equals(taxiCode))
                .findFirst()
                .ifPresent(taxis::add);
        }
        ourSystem.addStation(new Station(stationName, taxis));
    }

    private static void parseManagerTaxiAssignment(String line) {
        String[] parts = line.split(",");
        String managerId = parts[0];
        ourSystem.getManagers().stream()
            .filter(m -> m.getId().equals(managerId))
            .findFirst()
            .ifPresent(manager -> {
                for (int i = 1; i < parts.length; i++) {
                    String taxiCode = parts[i];
                    ourSystem.getTaxis().stream()
                        .filter(t -> t.getTaxiCode().equals(taxiCode))
                        .findFirst()
                        .ifPresent(manager::addTaxi);
                }
            });
    }

    private static void parseOrder(String line) {
        String[] parts = line.split(",");
        String orderNum = parts[0];
        String managerCode = parts[1];
        int day = Integer.parseInt(parts[2]);
        int month = Integer.parseInt(parts[3]);
        int hour = Integer.parseInt(parts[4]);
        String subCode = parts[5];
        String taxiCode = parts[6];

        ourSystem.getTaxis().stream()
            .filter(t -> t.getTaxiCode().equals(taxiCode))
            .findFirst()
            .ifPresent(taxi -> {
                // A taxi in an order is not available.
                taxi.setAvailable(false); 
                ourSystem.addOrder(new Order(orderNum, managerCode, day, month, hour, subCode, taxi, taxi.getMinPrice()));
            });
    }
}