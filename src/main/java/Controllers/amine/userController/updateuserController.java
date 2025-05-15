package Controllers.amine.userController;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import Singleton.loggedInUser;
import entities.amine.User;
import services.amine.NotificationModule.mailNotificationService;
import services.amine.userService;
import utils.amine.PasswordVerification;
import utils.amine.navigation;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

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

    private userService userService = new userService();
    private User currentUser;
    private User originalUser;

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


            // You'll need to parse the birthday string to LocalDate for the DatePicker
            // birthdayPicker.setValue(parseBirthday(originalUser.getBirthday()));
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
            if (!PasswordVerification.verifyPassword(newPassword, originalUser.getPassword())) {
                currentUser.setPassword(newPassword);
                passwordChanged = true;
                changesDetected = true;
            } else {
                passwordField.clear(); // Same password, no change
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

            currentUser.setEmail(newEmail); // Only set if verified
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
        int code = 100000 + random.nextInt(900000); // Generates a 6-digit number
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

    // Add this method to handle birthday parsing if needed
    /*
    private LocalDate parseBirthday(String birthday) {
        try {
            return LocalDate.parse(birthday, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }
    */
}