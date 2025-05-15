package Controllers.tasnim;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainAdminController {

    @FXML
    private StackPane contentPane;

    @FXML
    private VBox homeSection;

    @FXML
    private VBox productManagementSection;

    @FXML
    private VBox orderManagementSection;

    @FXML
    private VBox stockManagementSection;

    @FXML
    private void loadHome() {
        homeSection.setVisible(true);
        productManagementSection.setVisible(false);
        orderManagementSection.setVisible(false);
        stockManagementSection.setVisible(false);
    }

    @FXML
    private void loadProductManagement() {
        homeSection.setVisible(false);
        productManagementSection.setVisible(true);
        orderManagementSection.setVisible(false);
        stockManagementSection.setVisible(false);
    }

    @FXML
    private void loadOrderManagement() {
        homeSection.setVisible(false);
        productManagementSection.setVisible(false);
        orderManagementSection.setVisible(true);
        stockManagementSection.setVisible(false);
    }

    @FXML
    private void loadStockManagement() {
        homeSection.setVisible(false);
        productManagementSection.setVisible(false);
        orderManagementSection.setVisible(false);
        stockManagementSection.setVisible(true);
    }

    @FXML
    private void returnToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-admin-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}