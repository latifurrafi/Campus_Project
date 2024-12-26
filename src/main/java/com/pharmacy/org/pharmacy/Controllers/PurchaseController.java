package com.pharmacy.org.pharmacy.Controllers;

import DataBase.DatabaseConnection;
import com.pharmacy.org.pharmacy.Models.InventoryItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;

import static com.pharmacy.org.pharmacy.Controllers.SummaryController.decimalFormat;

public class PurchaseController {

    public TableView<InventoryItem> inventoryTable;
    @SuppressWarnings("exports")
    public TextField quantityField;
    @SuppressWarnings("exports")
    public Button purchaseButton;

    private final ObservableList<InventoryItem> inventory = FXCollections.observableArrayList();

    public TableColumn<InventoryItem, String> brandNameColumn;
    public TableColumn<InventoryItem, String> genericNameColumn;
    public TableColumn<InventoryItem, String> strengthColumn;
    public TableColumn<InventoryItem, String> manufacturerColumn;
    public TableColumn<InventoryItem, String> priceColumn;
    public TableColumn<InventoryItem, Integer> quantityColumn;
    @SuppressWarnings("exports")
    public Label purchaseAmountLabel;
    @SuppressWarnings("exports")
    public Label purchaseDateLabel;

    public void initialize() {
        // Configure the inventoryTable with necessary columns
        brandNameColumn.setCellValueFactory(new PropertyValueFactory<>("brandName"));
        genericNameColumn.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        strengthColumn.setCellValueFactory(new PropertyValueFactory<>("strength"));
        manufacturerColumn.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        inventoryTable.setItems(inventory);

        // Load inventory data
        loadInventoryData();

        // Update purchase amount in real-time
        quantityField.textProperty().addListener((_, _, _) -> updatePurchaseAmount());
    }

    private void updatePurchaseAmount() {
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText().trim();

        if (selected != null && !quantityText.isEmpty()) {
            try {
                int purchaseQuantity = Integer.parseInt(quantityText);
                if (purchaseQuantity > 0) {
                    // Calculate purchase amount in real-time
                    double price = Double.parseDouble(selected.getPrice());
                    double purchaseAmount = price * purchaseQuantity;
                    purchaseAmountLabel.setText("Purchase Amount: ৳ " + purchaseAmount);

                }
            } catch (NumberFormatException e) {
                purchaseAmountLabel.setText("Invalid quantity");
            }
        }
    }


    private void loadInventoryData() {
        String query = "SELECT brand_name, generic_name, strength, manufacturer, price, quantity FROM Inventory";
        try (Connection conn = DatabaseConnection.con();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String brandName = rs.getString("brand_name");
                String genericName = rs.getString("generic_name");
                String strength = rs.getString("strength");
                String manufacturer = rs.getString("manufacturer");
                String price = rs.getString("price");
                int quantity = rs.getInt("quantity");

                inventory.add(new InventoryItem(brandName, genericName, strength, manufacturer, price, quantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error loading inventory data.");
        }
    }

    public void onPurchase(@SuppressWarnings("exports") ActionEvent actionEvent) {
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText().trim();

        if (selected != null && !quantityText.isEmpty()) {
            try {
                int purchaseQuantity = Integer.parseInt(quantityText);

                if (purchaseQuantity <= 0) {
                    showErrorAlert("Quantity must be a positive number.");
                    return;
                }

                int currentQuantity = selected.getQuantity();
                if (purchaseQuantity > currentQuantity) {
                    showErrorAlert("Not enough stock available.");
                    return;
                }

                // Calculate purchase amount
                double price = Double.parseDouble(selected.getPrice());
                double purchaseAmount = price * purchaseQuantity;

                // Update the inventory quantity in the UI
                selected.setQuantity(currentQuantity - purchaseQuantity);
                inventoryTable.refresh();

                // Update the database
                String updateQuery = "UPDATE Inventory SET quantity = ? WHERE brand_name = ?";
                try (Connection conn = DatabaseConnection.con();
                     PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

                    stmt.setInt(1, selected.getQuantity());
                    stmt.setString(2, selected.getBrandName());
                    stmt.executeUpdate();

                    // Insert the purchase record into Purchases table
                    String insertQuery = "INSERT INTO Purchases (brand_name, quantity, purchase_amount, purchase_date) VALUES (?, ?, ?, CURRENT_DATE)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, selected.getBrandName());
                        insertStmt.setInt(2, purchaseQuantity);
                        insertStmt.setDouble(3, Double.parseDouble(decimalFormat.format(purchaseAmount)));
                        insertStmt.executeUpdate();
                    }

                    // Update purchase details in UI
                    purchaseAmountLabel.setText("Purchase Amount: ৳ " + purchaseAmount);

                    purchaseDateLabel.setText("Purchase Date: " + LocalDate.now());

                    showInfoAlert();
                }

            } catch (NumberFormatException e) {
                showErrorAlert("Invalid quantity format.");
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Error updating inventory.");
            }
        } else {
            showErrorAlert("Please select a medicine and enter a valid quantity.");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Purchase successful.");
        alert.showAndWait();
    }
}