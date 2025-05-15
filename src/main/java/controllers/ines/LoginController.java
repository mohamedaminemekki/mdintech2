package Controllers.ines;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.MainFX;
import utils.MyDataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField identifiantField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    private MainFX mainApp; // Référence à MainFX pour changer les vues

    public void handleLogin() {
        String identifiant = identifiantField.getText();
        String password = passwordField.getText();

        if (identifiant.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
        } else {
            try {
                String query = "SELECT idUser, role FROM user WHERE identifiant = ? AND mdp = SHA2(?, 256)";
                try (PreparedStatement statement = MyDataBase.getInstance().getCon().prepareStatement(query)) {
                    statement.setString(1, identifiant);
                    statement.setString(2, password);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            String role = resultSet.getString("role");
                            int userId = resultSet.getInt("idUser");

                            if (role.equals("user")) {
                                mainApp.showServiceView(); // Rediriger vers la vue des services
                            } else if (role.equals("admin")) {
                                mainApp.showAdminView(); // Rediriger vers la vue admin
                            }
                        } else {
                            showAlert("Erreur", "Identifiants incorrects.");
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur de connexion à la base de données.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainApp(MainFX mainApp) {
        this.mainApp = mainApp;
    }
}