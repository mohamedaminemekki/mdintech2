package Controllers.amine.userController;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import Singleton.loggedInUser;
import entities.amine.User;
import services.amine.NotificationModule.mailNotificationService;
import services.amine.userService;
import utils.amine.PasswordVerification;
import utils.amine.navigation;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

public class updateuserController {
    @FXML
    private TextField nameField, emailField, phoneField, addressField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel, cinLabel;
    @FXML
    private TextArea bioField;
    @FXML
    private DatePicker birthdayPicker;
    @FXML
    private ProgressBar passwordStrengthBar;
    @FXML
    private Label passwordStrengthLabel;
    @FXML
    private VBox passwordRequirements;

    private userService userService = new userService();
    private User currentUser;
    private User originalUser;

    // Password requirement patterns
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_NUMBER = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @FXML
    public void initialize() throws JsonProcessingException {
        currentUser = loggedInUser.getInstance().getLoggedUser();
        if (currentUser != null) {
            originalUser = new User(
                    currentUser.getName(),
                    currentUser.getCIN(),
                    currentUser.getEmail(),
                    currentUser.getPassword(),
                    currentUser.getRoles().get(0),
                    currentUser.getPhone(),
                    currentUser.getAddress(),
                    currentUser.getPathtopic(),
                    currentUser.getBirthday(),
                    currentUser.getBio()
            );

            // Initialize fields with current values
            nameField.setText(originalUser.getName());
            emailField.setText(originalUser.getEmail());
            phoneField.setText(originalUser.getPhone());
            addressField.setText(originalUser.getAddress());
            cinLabel.setText(originalUser.getCIN());
            bioField.setText(originalUser.getBio());

            System.out.println("Google ID: " + currentUser.getGoogleId());

            if (currentUser.getGoogleId() != null ) {
                emailField.setDisable(true);
            }

            // Initialize password strength elements
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("");
            updatePasswordRequirements("");
        }
    }

    @FXML
    private void handlePasswordChange() {
        String password = passwordField.getText();
        updatePasswordStrength(password);
        updatePasswordRequirements(password);
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("");
            return;
        }

        int strength = calculatePasswordStrength(password);
        double progress = strength / 5.0;
        passwordStrengthBar.setProgress(progress);

        // Update progress bar and label styles
        passwordStrengthBar.getStyleClass().removeAll("weak", "medium", "strong");
        passwordStrengthLabel.getStyleClass().removeAll("weak", "medium", "strong");

        if (progress < 0.5) {
            passwordStrengthBar.getStyleClass().add("weak");
            passwordStrengthLabel.getStyleClass().add("weak");
            passwordStrengthLabel.setText("Weak");
        } else if (progress < 0.8) {
            passwordStrengthBar.getStyleClass().add("medium");
            passwordStrengthLabel.getStyleClass().add("medium");
            passwordStrengthLabel.setText("Medium");
        } else {
            passwordStrengthBar.getStyleClass().add("strong");
            passwordStrengthLabel.getStyleClass().add("strong");
            passwordStrengthLabel.setText("Strong");
        }
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        
        if (password.length() >= 8) score++;
        if (HAS_LOWERCASE.matcher(password).find()) score++;
        if (HAS_UPPERCASE.matcher(password).find()) score++;
        if (HAS_NUMBER.matcher(password).find()) score++;
        if (HAS_SPECIAL.matcher(password).find()) score++;

        return score;
    }

    private void updatePasswordRequirements(String password) {
        for (Node node : passwordRequirements.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                String requirement = label.getText().substring(2); // Remove bullet point
                boolean isMet = false;

                switch (requirement) {
                    case "At least 8 characters":
                        isMet = password.length() >= 8;
                        break;
                    case "At least one uppercase letter":
                        isMet = HAS_UPPERCASE.matcher(password).find();
                        break;
                    case "At least one lowercase letter":
                        isMet = HAS_LOWERCASE.matcher(password).find();
                        break;
                    case "At least one number":
                        isMet = HAS_NUMBER.matcher(password).find();
                        break;
                    case "At least one special character":
                        isMet = HAS_SPECIAL.matcher(password).find();
                        break;
                }

                label.getStyleClass().remove("met");
                if (isMet) {
                    label.getStyleClass().add("met");
                }
            }
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (currentUser == null) {
            statusLabel.setText("No user is logged in.");
            return;
        }

        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPhone = phoneField.getText().trim();
        String newAddress = addressField.getText().trim();
        String newPassword = passwordField.getText().trim();
        String newBio = bioField.getText().trim();

        boolean changesDetected = false;
        boolean emailChanged = false;

        if (!newName.equals(originalUser.getName())) {
            currentUser.setName(newName);
            changesDetected = true;
        }

        if (!newEmail.equals(originalUser.getEmail())) {
            emailChanged = true;
            changesDetected = true;
        }

        if (!newPhone.equals(originalUser.getPhone())) {
            currentUser.setPhone(newPhone);
            changesDetected = true;
        }

        if (!newAddress.equals(originalUser.getAddress())) {
            currentUser.setAddress(newAddress);
            changesDetected = true;
        }

        if (!newBio.equals(originalUser.getBio())) {
            currentUser.setBio(newBio);
            changesDetected = true;
        }

        boolean passwordChanged = false;
        if (!newPassword.isEmpty()) {
            // Check password strength before allowing update
            if (calculatePasswordStrength(newPassword) < 3) {
                statusLabel.setText("Password is too weak. Please make it stronger.");
                return;
            }

            if (!PasswordVerification.verifyPassword(newPassword, originalUser.getPassword())) {
                currentUser.setPassword(newPassword);
                passwordChanged = true;
                changesDetected = true;
            } else {
                passwordField.clear();
            }
        }

        if (!changesDetected) {
            statusLabel.setText("No changes detected.");
            return;
        }

        // Handle email verification if email changed
        if (emailChanged) {
            String verificationCode = generateVerificationCode();
            mailNotificationService mailService = new mailNotificationService();
            mailService.sendEmail(newEmail, "Email Verification", "Your verification code is: " + verificationCode);

            String userEnteredCode = showVerificationPopup();
            if (userEnteredCode == null || !userEnteredCode.equals(verificationCode)) {
                statusLabel.setText("Email verification failed. Update cancelled.");
                return;
            }

            currentUser.setEmail(newEmail);
        }

        try {
            if (passwordChanged) {
                userService.update(currentUser);
            } else {
                userService.updateUserWithoutPassword(currentUser);
            }

            statusLabel.setText("User updated successfully!");
            passwordField.clear();
            navigation.switchScene(event, "/main-user-view.fxml");

        } catch (Exception e) {
            statusLabel.setText("Update failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-user-view.fxml");
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private String showVerificationPopup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Email Verification");
        dialog.setHeaderText("A verification code has been sent to your email.");
        dialog.setContentText("Enter the code:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}