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

    private void loadView(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Get stage from the event source (button or any node)
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHomeView(ActionEvent event) {
        loadView("/main-user-view.fxml", event);
    }

    @FXML
    private void goToParkings(ActionEvent event) {
        loadView("/amine/userModule/parking/display-parkings.fxml", event);
    }

    @FXML
    private void goToParkingTickets(ActionEvent event) {
        loadView("/amine/userModule/parking/display-tickets.fxml", event);
    }

    public void goToUpdatePorfile(ActionEvent event) {
        loadView("/amine/userModule/update-user-view.fxml", event);
    }

    public void logout(ActionEvent event) {
        loggedInUser.clearSession();
        loadView("/amine/userModule/login-view.fxml", event);
    }
    @FXML
    private void goToUserInterface(ActionEvent event) {
        loadView("/rahim/UserInterface.fxml", event);
    }
    @FXML
    private void goToMarketInterface(ActionEvent event) {
        loadView("/tasnim/main_view.fxml", event);
    }
    @FXML
    private void goToTransportInterface(ActionEvent event) {
        loadView("/views/MainWindow.fxml", event);
    }
    @FXML
    private void goToHopitalInterface(ActionEvent event) {
        loadView("/ines/service-view.fxml", event);
    }
    @FXML
    private void goToReclamationInterface(ActionEvent event) {
        loadView("/Mohamed/AfficherReclamationClient.fxml", event);
    }
}