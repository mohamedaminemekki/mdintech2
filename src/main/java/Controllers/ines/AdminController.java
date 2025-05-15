package Controllers.ines;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import services.ines.CalendarQuickstart;
import services.ines.ServiceHospitalierServices;
import entities.ines.ServiceHospitalier;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

public class AdminController {

    @FXML
    private ListView<ServiceHospitalier> serviceListView;

    @FXML
    private Button addServiceButton, doctorListButton, appointmentListButton, statsButton, calendarButton, modifyServiceButton, deleteServiceButton;


    private ServiceHospitalierServices serviceHospitalierServices = new ServiceHospitalierServices();

    @FXML
    public void initialize() {
        loadServices();

        // Handle button actions
        statsButton.setOnAction(this::showStatistics);
        calendarButton.setOnAction(event -> openGoogleCalendar());
        addServiceButton.setOnAction(this::handleAddService);
        doctorListButton.setOnAction(event -> openWindow("/ines/listeMedecins.fxml", "Liste des médecins"));
        appointmentListButton.setOnAction(event -> openWindow("/ines/listeRendezVous.fxml", "Liste des rendez-vous"));

        modifyServiceButton.setOnAction(this::handleModifyService);
        deleteServiceButton.setOnAction(this::handleDeleteService);
    }



    @FXML
    private void openGoogleCalendar() {
        try {

            CalendarQuickstart.main();
        } catch (IllegalArgumentException e) {
            System.err.println("Date format error: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @FXML
    private void showStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ines/StatsView.fxml"));
            Parent root = loader.load();
            Stage statsStage = new Stage();
            statsStage.setTitle("📊 Statistique des Rendez-vous");
            statsStage.setScene(new Scene(root));
            statsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadServices() {
        try {
            var services = serviceHospitalierServices.readList();
            serviceListView.setCellFactory(param -> new ServiceCell());
            serviceListView.getItems().setAll(services);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les services.", Alert.AlertType.ERROR);
        }
    }

    private void handleAddService(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Service");
        dialog.setHeaderText("Veuillez entrer les informations du service");

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom du Service");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField nombreLitsField = new TextField();
        nombreLitsField.setPromptText("Nombre de lits disponibles");

        grid.add(new Label("Nom du Service:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Nombre de Lits Disponibles:"), 0, 2);
        grid.add(nombreLitsField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(ActionEvent.ACTION, e -> {
            String nom = nomField.getText().trim();
            String description = descriptionField.getText().trim();
            String nombreLits = nombreLitsField.getText().trim();

            // Input validation (no invalid characters or numbers)
            if (nom.isEmpty() || description.isEmpty() || nombreLits.isEmpty()) {
                showAlert("Champs vides", "Tous les champs sont obligatoires !", Alert.AlertType.ERROR);
                e.consume();
                return;
            }

            if (!nom.matches("^[A-Za-zÀ-ÿ\\s\\-.,!?:;()]+$")) {
                showAlert("Erreur", "Le nom du service ne doit contenir que des lettres et des caractères spéciaux valides.", Alert.AlertType.ERROR);
                e.consume();
                return;
            }

            if (!description.matches("^[A-Za-zÀ-ÿ\\s\\-.,!?:;()]+$")) {
                showAlert("Erreur", "La description ne doit contenir que des lettres et des caractères spéciaux valides.", Alert.AlertType.ERROR);
                e.consume();
                return;
            }

            try {
                Integer.parseInt(nombreLits); // Vérification si le nombre de lits est un entier
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Le nombre de lits disponibles doit être un nombre valide.", Alert.AlertType.ERROR);
                e.consume();
                return;
            }

            // Check if service exists by name
            try {
                if (serviceHospitalierServices.existsByName(nom)) {
                    showAlert("Erreur", "Ce service existe déjà.", Alert.AlertType.ERROR);
                    e.consume();
                    return;
                }
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur de vérification de l'unicité du service.", Alert.AlertType.ERROR);
                e.consume();
                ex.printStackTrace();
            }

            try {
                int nombreLitsDisponibles = Integer.parseInt(nombreLits);
                ServiceHospitalier service = new ServiceHospitalier(0, nom, description, nombreLitsDisponibles);
                serviceHospitalierServices.add(service);
                showAlert("Succès", "Service ajouté avec succès !", Alert.AlertType.INFORMATION);
                loadServices();
            } catch (SQLException ex) {
                showAlert("Erreur", "Impossible d'ajouter le service.", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleModifyService(ActionEvent event) {
        ServiceHospitalier selectedService = serviceListView.getSelectionModel().getSelectedItem();

        if (selectedService == null) {
            showAlert("Erreur", "Veuillez sélectionner un service à modifier.", Alert.AlertType.WARNING);
            return;
        }

        // Ouvrir une boîte de dialogue pour modifier le service
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier un Service");
        dialog.setHeaderText("Modification des informations du service");

        ButtonType updateButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nomField = new TextField(selectedService.getNomService());
        TextField descriptionField = new TextField(selectedService.getDescription());
        TextField nombreLitsField = new TextField(String.valueOf(selectedService.getNombreLitsDisponibles()));

        grid.add(new Label("Nom du Service:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Nombre de Lits Disponibles:"), 0, 2);
        grid.add(nombreLitsField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node updateButton = dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.addEventFilter(ActionEvent.ACTION, e -> {
            String nom = nomField.getText().trim();
            String description = descriptionField.getText().trim();
            String nombreLits = nombreLitsField.getText().trim();

            // Validation des champs
            if (nom.isEmpty() || description.isEmpty() || nombreLits.isEmpty()) {
                showAlert("Erreur", "Tous les champs sont obligatoires !", Alert.AlertType.ERROR);
                e.consume();
                return;
            }

            try {
                Integer.parseInt(nombreLits); // Vérification si le nombre de lits est un entier
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Le nombre de lits disponibles doit être un nombre valide.", Alert.AlertType.ERROR);
                e.consume();
                return;
            }

            try {
                selectedService.setNomService(nom);
                selectedService.setDescription(description);
                selectedService.setNombreLitsDisponibles(Integer.parseInt(nombreLits));
                serviceHospitalierServices.update(selectedService);
                showAlert("Succès", "Service modifié avec succès !", Alert.AlertType.INFORMATION);
                loadServices();
            } catch (SQLException ex) {
                showAlert("Erreur", "Impossible de modifier le service.", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteService(ActionEvent event) {
        ServiceHospitalier selectedService = serviceListView.getSelectionModel().getSelectedItem();

        if (selectedService == null) {
            showAlert("Erreur", "Veuillez sélectionner un service à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        // Confirmation de suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer ce service ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceHospitalierServices.delete(selectedService.getIdService());
                    showAlert("Succès", "Service supprimé avec succès.", Alert.AlertType.INFORMATION);
                    loadServices();
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression du service.", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre : " + title, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class ServiceCell extends ListCell<ServiceHospitalier> {
        private final GridPane gridPane = new GridPane();
        private final Label nomLabel = new Label();
        private final Label descriptionLabel = new Label();
        private final Label nombreLitsLabel = new Label();

        // Font and color
        {
            nomLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            descriptionLabel.setFont(Font.font("Arial", 12));
            nombreLitsLabel.setFont(Font.font("Arial", 12));
            gridPane.setHgap(10);
            gridPane.setVgap(5);
            gridPane.add(nomLabel, 0, 0);
            gridPane.add(descriptionLabel, 0, 1);
            gridPane.add(nombreLitsLabel, 0, 2);
        }

        @Override
        protected void updateItem(ServiceHospitalier service, boolean empty) {
            super.updateItem(service, empty);
            if (empty || service == null) {
                setText(null);
                setGraphic(null);
            } else {
                nomLabel.setText(service.getNomService());
                descriptionLabel.setText(service.getDescription());
                nombreLitsLabel.setText("Lits Disponibles: " + service.getNombreLitsDisponibles());
                setGraphic(gridPane);
            }
        }
    }
}