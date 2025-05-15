package Controllers.amine;


import entities.amine.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import services.amine.userService;
import utils.amine.PasswordVerification;

import java.io.IOException;

public class ResetPasswordController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    private String email;
    private userService us=new userService();
    public PasswordVerification ps=new PasswordVerification();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void resetPassword(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Fields cannot be empty.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }
        User user=us.findByEmail(email);
        System.out.println("the new password is "+newPassword);
        System.out.println("the hashed password is "+PasswordVerification.hashPassword(newPassword));
        if (PasswordVerification.verifyPassword(newPassword, user.getPassword())) {
            showAlert("Error", "New password cannot be the same as the old password.");
            return;
        }
        us.updatePassword(email, newPassword);
        showAlert("Success", "Password reset successfully!");
        goToLoginPage(event);
    }

    @FXML
    private void goToLoginPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/amine/userModule/login-view.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void goToDashboard(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent dashboardRoot = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(dashboardRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

