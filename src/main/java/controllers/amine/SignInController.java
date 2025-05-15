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

    @FXML
    private Label cinValidationLabel;

    @FXML
    private Label phoneValidationLabel;

    @FXML
    private Label birthdayValidationLabel;

    private File selectedImageFile;



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

        birthdayPicker.setValue(LocalDate.now().minusYears(18));

        // Add listeners for real-time validation
        cinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cinField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 8) {
                cinField.setText(oldValue);
            }
            validateCIN();
        });

        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 8) {
                phoneField.setText(oldValue);
            }
            validatePhone();
        });

        // Add listener for birthday picker
        birthdayPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateAge();
        });

        // Initial validation
        validateCIN();
        validatePhone();
        validateAge();
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        try {
            // Validate all fields first
            validateCIN();
            validatePhone();
            validateAge();
            checkPasswordStrength();

            // Check if there are any validation errors
            if (cinValidationLabel.isVisible() || phoneValidationLabel.isVisible() || 
                birthdayValidationLabel.isVisible() || passwordStrengthLabel.getText().startsWith("Weak")) {
                showAlert("Validation Error", "Please fix all validation errors before proceeding.");
                return;
            }

            String name = nameField.getText();
            String cin = cinField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            String bio = bioField.getText();
            LocalDate localDate = birthdayPicker.getValue();

            if (name.isEmpty() || cin.isEmpty() || email.isEmpty() || password.isEmpty() ||
                    phone.isEmpty() || address.isEmpty() || bio.isEmpty() || localDate == null) {
                showAlert("Validation Error", "Please fill in all fields and select a valid birthday.");
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

    @FXML
    public void handleBackButton(ActionEvent event) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/amine/userModule/login-view.fxml"));
            Parent signInRoot = loader.load();

            // Get the current stage (window)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(signInRoot);
            scene.getStylesheets().add(getClass().getResource("/amine/userModule/style.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @FXML
    public void validateCIN() {
        String cin = cinField.getText();
        boolean isValid = true;
        String message = "";

        // Remove any styling classes first
        cinField.getStyleClass().removeAll("valid", "invalid");

        if (cin.isEmpty()) {
            message = "CIN is required";
            isValid = false;
        } else {
            // Check if it's exactly 8 digits
            if (!cin.matches("\\d{8}")) {
                message = "CIN must be exactly 8 digits";
                isValid = false;
            }
        }

        // Apply appropriate styling
        if (!cin.isEmpty()) {
            cinField.getStyleClass().add(isValid ? "valid" : "invalid");
        }
        
        cinValidationLabel.setText(message);
        cinValidationLabel.setVisible(!message.isEmpty());
    }

    @FXML
    public void validatePhone() {
        String phone = phoneField.getText();
        boolean isValid = true;
        String message = "";

        // Remove any styling classes first
        phoneField.getStyleClass().removeAll("valid", "invalid");

        if (phone.isEmpty()) {
            message = "Phone number is required";
            isValid = false;
        } else {
            // Check if it's exactly 8 digits
            if (!phone.matches("\\d{8}")) {
                message = "Phone number must be exactly 8 digits";
                isValid = false;
            }
        }

        // Apply appropriate styling
        if (!phone.isEmpty()) {
            phoneField.getStyleClass().add(isValid ? "valid" : "invalid");
        }
        
        phoneValidationLabel.setText(message);
        phoneValidationLabel.setVisible(!message.isEmpty());
    }

    @FXML
    public void validateAge() {
        LocalDate birthday = birthdayPicker.getValue();
        boolean isValid = true;
        String message = "";

        // Remove any styling classes first
        birthdayPicker.getStyleClass().removeAll("valid", "invalid");

        if (birthday == null) {
            message = "Birthday is required";
            isValid = false;
        } else {
            // Calculate age
            LocalDate now = LocalDate.now();
            int age = now.getYear() - birthday.getYear();
            if (birthday.plusYears(age).isAfter(now)) {
                age--;
            }

            if (age < 18) {
                message = "Must be at least 18 years old";
                isValid = false;
            }
        }

        // Apply appropriate styling
        if (birthday != null) {
            birthdayPicker.getStyleClass().add(isValid ? "valid" : "invalid");
        }
        
        birthdayValidationLabel.setText(message);
        birthdayValidationLabel.setVisible(!message.isEmpty());
    }
}
