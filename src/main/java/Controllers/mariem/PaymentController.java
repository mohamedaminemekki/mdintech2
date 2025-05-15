package Controllers.mariem;

import Singleton.dbConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import entities.mariem.Reservation;
import services.mariem.ReservationService;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {

    @FXML
    private Label paymentDetailsLabel;

    @FXML
    private TextField cardNumberField;

    @FXML
    private DatePicker expirationDatePicker;

    @FXML
    private TextField securityCodeField;

    @FXML
    private TextField cardHolderNameField;

    @FXML
    private Button confirmPaymentButton;

    private Reservation reservation;
    private ReservationService reservationService;
    private double totalPrice;

    public PaymentController() {
        try {
            Connection connection = dbConnection.getInstance().getConn();
            reservationService = new ReservationService(connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure le DatePicker pour jj/MM/aaaa
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringConverter<LocalDate> converter = new LocalDateStringConverter(formatter, null);
        expirationDatePicker.setConverter(converter);
        expirationDatePicker.setPromptText("jj/MM/aaaa");
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        displayPaymentDetails();
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        displayPaymentDetails();
    }

    @FXML
    private void displayPaymentDetails() {
        if (paymentDetailsLabel != null && reservation != null) {
            String details = "Détails du paiement :\n" +
                    "Trajet : " + reservation.getTripId() + "\n" +
                    "Nombre de passagers : " + reservation.getSeatNumber() + "\n" +
                    "Type de siège : " + reservation.getSeatType() + "\n" +
                    "Montant à payer : " + totalPrice + " DT";
            paymentDetailsLabel.setText(details);
        }
    }

    @FXML
    private void handleConfirmPayment() {
        if (!validateCardNumber() ||
                !validateExpirationDate() ||
                !validateSecurityCode() ||
                !validateCardHolderName()) {
            return;
        }

        boolean paymentSuccess = simulatePayment();

        if (paymentSuccess) {
            try {
                reservation.setPaymentStatus("Paid");
                reservation.setStatus("Confirmed");

                if (reservation.getId() == 0) {
                    reservationService.add(reservation);
                } else {
                    reservationService.update(reservation);
                }

                showAlert("Paiement réussi", "Votre paiement a été confirmé avec succès !");
                ((Stage) confirmPaymentButton.getScene().getWindow()).close();

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement de la réservation.");
            }
        } else {
            showAlert("Paiement échoué", "Le paiement n'a pas pu être traité. Veuillez réessayer.");
        }
    }

    private boolean validateCardNumber() {
        String cardNumber = cardNumberField.getText().replaceAll("\\s", "");
        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            showAlert("Erreur", "Le numéro de carte doit contenir exactement 16 chiffres.");
            return false;
        }
        return true;
    }

    private boolean validateExpirationDate() {
        LocalDate expirationDate = expirationDatePicker.getValue();
        if (expirationDate == null || expirationDate.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date d'expiration doit être au format jj/MM/aaaa et dans le futur.");
            return false;
        }
        return true;
    }

    private boolean validateSecurityCode() {
        String securityCode = securityCodeField.getText();
        if (securityCode.length() != 3 || !securityCode.matches("\\d+")) {
            showAlert("Erreur", "Le code de sécurité doit contenir exactement 3 chiffres.");
            return false;
        }
        return true;
    }

    private boolean validateCardHolderName() {
        String name = cardHolderNameField.getText();
        if (name.isEmpty() || !name.matches("[a-zA-Z\\s]+")) {
            showAlert("Erreur", "Le nom du titulaire ne doit contenir que des lettres et des espaces.");
            return false;
        }
        return true;
    }

    private boolean simulatePayment() {
        return Math.random() < 0.8;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
