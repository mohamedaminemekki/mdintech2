package Controllers.amine.userController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import Singleton.loggedInUser;

import java.io.IOException;

public class mainAdminViewController {
    @FXML
    private AnchorPane rootPane; // Use this as the root node to get the stage

    @FXML private MenuItem goToHomeView;
    @FXML private MenuItem goToParkings;
    @FXML private MenuItem goToParkingTickets;
    @FXML private MenuItem goToUsers;
    @FXML private MenuItem goToCreateParking;

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Get stage from the rootPane's scene
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHomeView() {
        loadView("/main-admin-view.fxml");
    }

    @FXML
    private void goToParkings() {
        loadView("/amine/ParkingModule/display-parkings-view.fxml");
    }

    @FXML
    private void goToParkingTickets() {
        loadView("/amine/ParkingModule/display-parkingtickets-view.fxml");
    }

    public void goToUsers(ActionEvent actionEvent) {loadView("/amine/userModule/display-users-view.fxml");}

    public void goToCreateParking(ActionEvent actionEvent) {loadView("/amine/ParkingModule/create-parking-view.fxml");}

    public void logout(ActionEvent actionEvent) {
        loggedInUser.clearSession();
        loadView("/amine/userModule/login-view.fxml");
    }
    @FXML
    private void goToMarketView() {
        loadView("/tasnim/mainAdmin_view.fxml");
    }
    @FXML
    private void goToHospitalView() {
        loadView("/ines/admin.fxml");
    }
    @FXML
    private void goToTransportView() {
        loadView("/views/admin_dashboard.fxml");
    }
    @FXML
    private void goToBlogView() {
        loadView("/Rahim/admin_blog.fxml");
    }
    @FXML
    private void goToReclamationView() {
        loadView("/Mohamed/AfficherReclamation.fxml");
    }
}

