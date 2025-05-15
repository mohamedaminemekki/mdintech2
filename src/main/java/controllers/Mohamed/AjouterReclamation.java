package controllers.Mohamed;

import Singleton.loggedInUser;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
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
import entities.Mohamed.Reclamation;
import services.Mohamed.ReclamationServices;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AjouterReclamation {

    private static final Logger logger = Logger.getLogger(AjouterReclamation.class.getName());

    @FXML
    private TextField emailField; // New email field
    @FXML
    private TextArea descriptionField;
    @FXML
    private CheckBox stateCheckBox;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private ImageView photoPreview;
    @FXML
    private Button uploadButton;
    @FXML
    private Button submitButton;
    @FXML
    private Button afficherReclamationsButton;

    private File selectedImageFile = null;

    @FXML
    public void initialize() {
        if (uploadButton == null) {
            logger.log(Level.SEVERE, "Upload Button is null!");
        } else {
            uploadButton.setOnAction(event -> uploadImage());
        }

        typeComboBox.setPromptText("Veuillez sélectionner le type");
        typeComboBox.getItems().addAll(
                "Problème d'application",
                "Réclamation Service administratif",
                "Réclamation service de transport",
                "Réclamation service hospitalier",
                "Réclamation service supermarché en ligne",
                "Autre problème"
        );

        submitButton.setOnAction(event -> soumettreReclamation());
    }

    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            photoPreview.setImage(image);
        }
    }

    private void soumettreReclamation() {
        try {
            // Validate input fields
            if (emailField.getText().isEmpty() || descriptionField.getText().isEmpty() || typeComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires !");
                return;
            }

            if (!isValidEmail(emailField.getText())) {
                showAlert("Erreur", "Veuillez entrer une adresse email valide !");
                return;
            }

            int clientId = Integer.parseInt(loggedInUser.getInstance().getLoggedUser().getCIN());
            String email = emailField.getText(); // Get email from the field
            LocalDate date = LocalDate.now();
            String description = descriptionField.getText();
            String type = typeComboBox.getValue();

            // Determine priority using Hugging Face API
            controllers.Mohamed.HuggingFaceAPI huggingFaceAPI = new controllers.Mohamed.HuggingFaceAPI();
            String priorite = huggingFaceAPI.determinePriority(description);

            String photoPath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : "Aucune photo";

            // Create Reclamation object
            Reclamation reclamation = new Reclamation(clientId, date.toString(), description, false, type, photoPath, priorite, email);

            // Send to API or database
            ReclamationServices reclamationService = new ReclamationServices();
            reclamationService.add(reclamation);

            // Send confirmation email using SendGrid
            sendEmail(email, "Votre réclamation a été soumise avec succès",
                    "Nous avons reçu votre réclamation et nous la traiterons dès que possible.");

            showAlert("Succès", "Réclamation ajoutée avec succès !");
            resetForm();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "L'ID du client doit être un nombre valide !");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de la détermination de la priorité", e);
            showAlert("Erreur", "Problème lors de la détermination de la priorité : " + e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ajout de la réclamation", e);
            showAlert("Erreur", "Problème lors de l'ajout : " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        try {
            // Replace with your SendGrid API key
            String apiKey = "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"; // Replace with your actual API key

            // Set up the email
            Email from = new Email("dridi.mohammed01@gmail.com", "Smart City Support"); // Replace with your verified sender email
            Email to = new Email(toEmail);
            // Use HTML content for formatted emails
            String htmlBody = "<html><body><p><strong>" + body + "</strong></p></body></html>"; // Bold text
            Content content = new Content("text/html", htmlBody); // Use "text/html" for HTML emails
            Mail mail = new Mail(from, subject, to, content);

            // Send the email
            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() == 202) { // 202 means the email was accepted for delivery
                logger.info("Email sent successfully.");
            } else {
                logger.severe("Failed to send email: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'envoi de l'email", e);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetForm() {
        emailField.clear(); // Clear the email field
        descriptionField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        photoPreview.setImage(null);
        selectedImageFile = null;
    }

    public void retournerPagePrecedente(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Mohamed/AfficherReclamationClient.fxml")); // Replace with the correct FXML file
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu Principal");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement de la page précédente", e);
        }
    }
}