package Controllers.amine;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.amine.User;
import services.amine.NotificationModule.mailNotificationService;
import utils.amine.PasswordVerification;
import utils.UserRole;
import services.amine.userService;
import utils.amine.navigation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

public class SignInController {

    @FXML
    private TextField bioField,nameField, cinField, emailField, phoneField, addressField, cityField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView profileImageView;

    @FXML
    private DatePicker birthdayPicker;

    @FXML
    private Button signInButton;

    @FXML
    private Label passwordStrengthLabel;

    private File selectedImageFile;

    @FXML
    private ScrollPane scrollPane;

    private final userService userService = new userService(); // Service for saving users

    @FXML
    public void initialize() {
        // Set the initial image
        String imagePath = "C:\\Users\\amine\\Desktop\\mdintech\\src\\main\\resources\\amine\\images\\defaultprofile.png"; // Replace with your image path
        File file = new File(imagePath);

        // Check if the file exists
        if (file.exists()) {
            // Convert the file path to a URL
            String imageUrl = file.toURI().toString();
            Image image = new Image(imageUrl);
            profileImageView.setImage(image);
        } else {
            System.err.println("Image file not found: " + imagePath);
        }

        birthdayPicker.setValue(LocalDate.now().minusYears(15));
        scrollPane.setVvalue(0); // Vertical scroll position (0 = top, 1 = bottom)
        scrollPane.setHvalue(0); // Horizontal scroll position (0 = left, 1 = right)
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        try {
            String name = nameField.getText();
            String cin = cinField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            String bio = bioField.getText();
            LocalDate localDate = birthdayPicker.getValue();

            if (localDate == null) {
                showAlert("Error", "Please select a valid birthday.");
                return;
            }
            Date birthday = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (!PasswordVerification.isStrongPassword(password)) {
                showAlert("Weak Password", "Password must be at least 8 characters long, contain at least one uppercase letter, " +
                        "one lowercase letter, one number, and one special character.");
                return;
            }

            // Save only relative image path in the database
            String profileImagePath = (selectedImageFile != null)
                    ? "profile_images/" + selectedImageFile.getName()
                    : "profile_images/default.png";

            // Email verification
            String verificationCode = generateVerificationCode();
            mailNotificationService mailService = new mailNotificationService();
            mailService.sendEmail(email, "Verification Code", "Your verification code is: " + verificationCode);

            String userEnteredCode = showVerificationPopup();
            if (userEnteredCode == null || !userEnteredCode.equals(verificationCode)) {
                showAlert("Error", "Incorrect verification code. Please try again.");
                return;
            }

            // Save user in the database
            User newUser = new User(name, cin, email, password, "ROLE_USER", phone, address, profileImagePath, birthday,bio);
            userService.save(newUser);

            showAlert("Success", "User registered successfully!");

            navigation.switchScene(event, "/amine/userModule/login-view.fxml");

        } catch (NumberFormatException e) {
            showAlert("Error", "CIN must be a valid number!");
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String showVerificationPopup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Email Verification");
        dialog.setHeaderText("A verification code has been sent to your email.");
        dialog.setContentText("Enter the code:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }


    private void goToDashboard(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent dashboardRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(dashboardRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void checkPasswordStrength() {
        String password = passwordField.getText();
        if (PasswordVerification.isStrongPassword(password)) {
            passwordStrengthLabel.setText("Strong password.");
            passwordStrengthLabel.setStyle("-fx-text-fill: green;");
        } else {
            passwordStrengthLabel.setText("Weak password! Must have at least 8 characters, one uppercase, one lowercase, one number, and one special character.");
            passwordStrengthLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/amine/userModule/login-view.fxml");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Define the XAMPP htdocs image folder
                String destinationDir = "C:\\xampp\\htdocs\\profile_images";
                File destFolder = new File(destinationDir);

                if (!destFolder.exists()) {
                    destFolder.mkdirs(); // Create directory if it doesn't exist
                }

                // Create a unique filename to prevent conflicts
                String uniqueFileName = System.currentTimeMillis() + "_" + file.getName();
                File destFile = new File(destFolder, uniqueFileName);

                // Copy the file to the destination
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Store only the relative path for database
                selectedImageFile = destFile;

                // Display the image in the UI
                Image image = new Image(destFile.toURI().toString());
                profileImageView.setImage(image);

                showAlert("Success", "Profile image selected successfully!");

            } catch (IOException e) {
                showAlert("Error", "Failed to save the image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Generates a 6-digit number
        return String.valueOf(code);
    }
}
