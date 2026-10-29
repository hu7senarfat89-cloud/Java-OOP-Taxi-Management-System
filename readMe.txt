================================
Taxi Management System - README
================================

General Description
-------------------
Taxi company management system using Swing.
The system simulates the management of managers, subscribers, taxis, and orders, and provides different functionalities based on the user type.

---

How to Run the System
--------------------
To run the system, execute the `Main.java` file located in the `View` package.
Upon startup, the system loads initial data from `DB.txt` and saves the updated state back to the file when the application is closed.

---

System Functionality by Screen
-----------------------------

### 1. Login Panel
This is the first screen that appears when the application starts.

* **Select User Type:** Choose one of the three radio buttons: "Main Manager", "Regular Manager", or "Subscriber".
* **Enter Credentials:** Based on the selection, the appropriate fields will appear:
    * **Main Manager:** Fields for username and password.
    * **Regular Manager:** A field for "Manager ID".
    * **Subscriber:** A field for "Subscriber ID".
* **Login Button:** Validates the entered credentials against the internal database. On successful login, the user is navigated to the corresponding panel. If it fails, an error message is displayed.
* **Exit Button:** Closes the application.

### 2. Main Manager Dashboard
This screen is the system's control center, featuring a side navigation menu.

* **Manage Subscribers:** Opens the `SubscribersTablePanel`, which displays a table of all subscribers.
    * **Add New:** Opens a form to add a new subscriber manually.
    * **Edit Selected:** Allows editing the details of a subscriber selected from the table.
    * **Remove Selected:** Deletes a selected subscriber.
    * **Receive from File:** Imports a list of subscribers from the `members.txt` file.

* **Manage Managers:** Opens the `ManagersTablePanel`.
    * Similar to the subscribers panel, allows adding, editing, deleting, and importing managers from the `SystemManagers.txt` file.

* **Manage Taxis:** Opens the `TaxisTablePanel`.
    * **Add New:** Allows adding a new taxi and selecting its type (Regular, Express, Intercity).
    * **Edit/Remove:** Allows editing and deleting existing taxis.

* **Assign Taxi to Manager:** Opens the `AssignTaxiToManagerPanel`. This screen shows existing assignments and allows you to select a taxi and a manager from dropdown lists and assign them.

* **Manage Orders:** Opens the `ManageOrdersPanel`, allowing the main manager to view all system orders, filter them by manager, and add, edit, or delete them.

* **Reports:** Opens the "Free Taxis by Station" screen (`GetFreeTaxisPanel`). You can select a station from a dropdown to view cards with details of all available taxis at that station.

* **Download Data:** Opens the `DownloadDataPanel`. This screen has four cards for downloading data:
    * **Managers:** Saves the current list of managers to `SystemManagers.txt`.
    * **Subscribers:** Saves the current list of subscribers to `members.txt`.
    * **Orders:** Saves the current list of orders to `orders.txt`.
    * **Taxis:** Saves the list of taxis (including their assigned managers) to `Taxi.txt`.

* **Logout:** Logs the user out and returns to the Login Panel.

### 3. Regular Manager Panel
A screen that displays only the orders associated with the logged-in manager.

* **Orders Table:** Shows a list of orders the current manager is responsible for.
* **Add New Order:** Opens a form to add a new order. The taxi list is filtered to show only available taxis assigned to that manager.
* **Change Taxi:** This button is enabled if an order with a "Regular" taxi is selected. It allows changing the taxi to an Express or Intercity type.
* **Edit/Remove:** Allows editing order details or deleting the selected order.
* **Logout:** Returns to the Login Panel.

### 4. Subscriber Panel
A personal dashboard for the logged-in subscriber, with a side navigation menu.

* **My Orders:** The default view, which displays a list of all the orders placed by the subscriber in a text area.
* **Update My Details:** Switches to a view where the subscriber can update their personal address and phone number.
* **View Taxi Details:** Switches to a view where the subscriber can select any taxi from a dropdown list and see its full details (type, price, availability, etc.).
* **Logout:** Returns to the Login Panel.