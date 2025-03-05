package controllers.amine;

import Singleton.loggedInUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class UserBlockedView {

    @FXML
    private ImageView imageView;

    public void initialize() {
        String imagePath = "/images/amine/cadenas.png";
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        imageView.setImage(image);
    }



    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Get the current stage from the imageView's scene
            Stage stage = (Stage) imageView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(ActionEvent actionEvent) {
        loggedInUser.clearSession();
        loadView("/amine/userModule/login-view.fxml");
    }
}