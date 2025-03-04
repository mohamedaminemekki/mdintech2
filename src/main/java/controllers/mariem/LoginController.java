package controllers.mariem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import views.SharedMain;

public class LoginController {
    private SharedMain mainApp;

    public void setMainApp(SharedMain mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleAdminButton(ActionEvent event) {
        mainApp.showAdminInterface();
    }

    @FXML
    private void handleUserButton(ActionEvent event) {
        mainApp.showUserInterface();
    }
}