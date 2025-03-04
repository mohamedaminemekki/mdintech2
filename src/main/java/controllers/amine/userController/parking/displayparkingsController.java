package controllers.amine.userController.parking;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entities.amine.ParkingModule.Parking;
import services.amine.ParkingModule.ParkingService;
import services.amine.ParkingModule.ParkingSlotService;
import utils.amine.navigation;

import java.io.IOException;
import java.util.List;

public class displayparkingsController {

    @FXML
    private ListView<String> parkingListView;
    @FXML
    private TableView<Parking> parkingTableView;

    @FXML
    private TableColumn<Parking, String> nameColumn;

    @FXML
    private TableColumn<Parking, String> locationColumn;

    private final ParkingService parkingService = new ParkingService();
    private final ParkingSlotService parkingSlotService = new ParkingSlotService();

    @FXML
    public void initialize() {
        setupTable();
        loadParkings();
    }
    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));

        parkingTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Parking selectedParking = parkingTableView.getSelectionModel().getSelectedItem();
                if (selectedParking != null) {
                    openParkingPopup(selectedParking.getName() + " - " + selectedParking.getLocalisation());
                }
            }
        });
    }
    private void loadParkings() {
        List<Parking> parkings = parkingService.findAll();
        parkingTableView.getItems().setAll(parkings);
    }

    private void openParkingPopup(String listViewItem) {
        try {
            // Split the list view item to extract the parking name
            String parkingName = listViewItem.split(" - ")[0]; // Split on " - " and take first part
            Parking parking = parkingService.findByName(parkingName);

            if (parking == null) {
                showAlert("Error: Parking not found!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/amine/userModule/parking/ParkingPopup.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Parking Details");

            ParkingPopupController controller = loader.getController();
            controller.initData(parking); // Pass the valid parking object

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.show();
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-user-view.fxml");
    }
}
