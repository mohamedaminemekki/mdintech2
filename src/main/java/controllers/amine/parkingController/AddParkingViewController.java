package controllers.amine.parkingController;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import entities.amine.ParkingModule.Parking;
import services.amine.ParkingModule.ParkingService;
import utils.amine.navigation;

import java.io.IOException;

public class AddParkingViewController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField capacityField;
    @FXML
    private Button addParkingButton;
    @FXML
    private Label nameErrorLabel;
    @FXML
    private Label locationErrorLabel;
    @FXML
    private Label capacityErrorLabel;

    private final ParkingService parkingService = new ParkingService();

    @FXML
    private void handleAddParking(ActionEvent event) throws IOException {
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        String capacityText = capacityField.getText().trim();

        // Reset error labels
        nameErrorLabel.setVisible(false);
        locationErrorLabel.setVisible(false);
        capacityErrorLabel.setVisible(false);

        // Validate input fields
        boolean valid = true;

        if (name.isEmpty()) {
            nameErrorLabel.setText("Name is required.");
            nameErrorLabel.setVisible(true);
            valid = false;
        }

        if (location.isEmpty()) {
            locationErrorLabel.setText("Location is required.");
            locationErrorLabel.setVisible(true);
            valid = false;
        }

        if (capacityText.isEmpty()) {
            capacityErrorLabel.setText("Capacity is required.");
            capacityErrorLabel.setVisible(true);
            valid = false;
        } else {
            try {
                int capacity = Integer.parseInt(capacityText);
                if (capacity <= 0) {
                    capacityErrorLabel.setText("Capacity must be a positive number.");
                    capacityErrorLabel.setVisible(true);
                    valid = false;
                }
            } catch (NumberFormatException e) {
                capacityErrorLabel.setText("Capacity must be a valid number.");
                capacityErrorLabel.setVisible(true);
                valid = false;
            }
        }

        if (!valid) {
            return; // Stop the process if validation fails
        }

        // Create a new Parking object
        Parking newParking = new Parking(name, location, Integer.parseInt(capacityText));

        // Save it using the service
        boolean success = parkingService.save(newParking);

        if (success) {
            showAlert("Success", "Parking added successfully!");
            clearFields();
            navigation.switchScene(event, "/main-admin-view.fxml");
        } else {
            showAlert("Error", "Failed to add parking. Please try again.");
        }
    }

    private void clearFields() {
        nameField.clear();
        locationField.clear();
        capacityField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-admin-view.fxml");
    }
}
