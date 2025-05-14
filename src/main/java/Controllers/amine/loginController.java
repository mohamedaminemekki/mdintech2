package Controllers.amine;


import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Singleton.loggedInUser;
import entities.amine.User;
import services.amine.NotificationModule.mailNotificationService;
import services.amine.userService;
import utils.amine.GoogleCallbackServer;
import utils.UserRole;
import utils.amine.VerificationCodeStorage;

import java.io.IOException;
import java.util.*;

public class loginController {
    static Dotenv dotenv = Dotenv.load();
    private static final String CLIENT_ID = dotenv.get("CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("CLIENT_SECRET");
    private static final String REDIRECT_URI = "http://localhost:8082/callback";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    @FXML
    private TextField emailField;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    userService us=new userService();

    @FXML
    public void gotoSignIn(ActionEvent event) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/amine/userModule/sign-in-view.fxml"));
            Parent signInRoot = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(signInRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void login(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();
        User user=us.login(email, password);
        if (user != null) {
            loggedInUser.initializeSession((user));

            if (!user.isActive()) {
                goToDashboard(event, "/user-blocked-view.fxml");
                return;
            }

            if (user.getRoles().contains("ROLE_ADMIN")) {
                goToDashboard(event, "/main-admin-view.fxml");
            } else if (user.getRoles().contains("ROLE_USER")) {
                goToDashboard(event, "/main-user-view.fxml");
            } else {
                showAlert("Access Denied", "You do not have permission to access this application.");
            }

        }else{
            showAlert("User Not Found ","no credentials are matching the ones you gave us !!!!! .????");
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void forgotPassword(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Reset Your Password");
        dialog.setContentText("Enter your email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (email.isEmpty()) {
                showAlert("Error", "Email field cannot be empty!");
                return;
            }

            if (!us.doesEmailExist(email)) {
                showAlert("Error", "Email not found in our records.");
                return;
            }

            // Generate 6-digit verification code
            String code = generateVerificationCode();

            // Send email
            mailNotificationService mailService = new mailNotificationService();
            mailService.sendEmail(email, "Password Reset Code", "Your verification code is: " + code);

            // Store the generated code somewhere (session, database, temporary cache, etc.)
            VerificationCodeStorage.store(email, code);

            // Redirect to the verification page
            goToVerificationPage(event, email);
        });
    }

    private void goToVerificationPage(ActionEvent event, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/amine/userModule/verify-code-view.fxml"));
            Parent verificationRoot = loader.load();

            VerificationController controller = loader.getController();
            controller.setEmail(email); // Pass email to verification controller

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(verificationRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Ensures a 6-digit number
        return String.valueOf(code);
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        try {
            GoogleCallbackServer.setEvent(event);
            GoogleCallbackServer.startServer(); // Start local server

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    CLIENT_ID,
                    CLIENT_SECRET,
                    SCOPES
            ).setAccessType("offline").build();

            String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
            java.awt.Desktop.getDesktop().browse(new java.net.URI(authorizationUrl));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Login Error", "Failed to initiate Google login.");
        }
    }

}
