package controllers.amine.userController;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import Singleton.loggedInUser;

import java.io.IOException;

public class mainUserViewController {

    @FXML private MenuBar menuBar;  // Add this line

    @FXML private MenuItem goToHomeView;
    @FXML private MenuItem goToParkings;
    @FXML private MenuItem goToParkingTickets;
    @FXML private MenuItem goToUpdateProfile;
    @FXML private MenuItem logout;

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Get stage from the menuBar's scene
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHomeView() {
        loadView("/main-user-view.fxml");
    }

    @FXML
    private void goToParkings() {
        loadView("/amine/userModule/parking/display-parkings.fxml");
    }

    @FXML
    private void goToParkingTickets() {
        loadView("/amine/userModule/parking/display-tickets.fxml");
    }

    public void goToUpdatePorfile(ActionEvent actionEvent) {loadView("/amine/userModule/update-user-view.fxml");}

    public void logout(ActionEvent actionEvent) {
        loggedInUser.clearSession();
        loadView("/amine/userModule/login-view.fxml");
    }
    @FXML
    private void goToUserInterface() {
        loadView("/rahim/UserInterface.fxml");
    }
    @FXML
    private void goToMarketInterface() {
        loadView("/tasnim/main_view.fxml");
    }
    @FXML
    private void goToTransportInterface() {
        loadView("/views/MainWindow.fxml");
    }
    @FXML
    private void goToHopitalInterface() {
        loadView("/ines/service-view.fxml");
    }
    @FXML
    private void goToReclamationInterface() {
        loadView("/Mohamed/AfficherReclamationClient.fxml");
    }
}