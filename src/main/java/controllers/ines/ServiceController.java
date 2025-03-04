package controllers.ines;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.ines.ServiceHospitalierServices;
import entities.ines.ServiceHospitalier;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ServiceController {

    @FXML
    private ListView<ServiceItem> serviceListView;
    @FXML
    private Button btnMesRendezVous;
    @FXML
    private  Button btnBesoinLit;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> comboService;



    private final ServiceHospitalierServices serviceHospitalierServices = new ServiceHospitalierServices();



    // Méthode pour effectuer la recherche
    @FXML
    private void onSearch() {
        String searchText = searchField.getText().trim();
        searchService(searchText);
    }







    // Initialisation de la liste des services
    @FXML
    public void initialize() {
        loadServicesFromDatabase();
        populateComboBox();


        // Ajouter l'événement de clic sur un service
        serviceListView.setOnMouseClicked(event -> {
            ServiceItem selectedService = serviceListView.getSelectionModel().getSelectedItem();
            if (selectedService != null) {
                // Récupérer l'ID du service sélectionné
                int idService = getServiceIdFromName(selectedService.getName());
                if (idService != 0) {  // Vérifie que l'ID est valide
                    // Appeler la méthode pour afficher les médecins de ce service
                    showMedecinsForService(idService);
                } else {
                    System.err.println("Service non trouvé !");
                }
            }
        });
    }

    // Méthode pour remplir le ComboBox avec les services
    private void populateComboBox() {
        try {
            List<ServiceHospitalier> services = serviceHospitalierServices.readList();
            ObservableList<String> serviceNames = FXCollections.observableArrayList();
            for (ServiceHospitalier service : services) {
                serviceNames.add(service.getNomService());
            }
            comboService.setItems(serviceNames);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les services.", Alert.AlertType.ERROR);
        }
    }

    // Charger les services depuis la base de données
    private void loadServicesFromDatabase() {
        ObservableList<ServiceItem> services = FXCollections.observableArrayList();

        try {
            // Récupérer la liste des services depuis la base de données
            List<ServiceHospitalier> serviceList = serviceHospitalierServices.readList();

            // Convertir en objets `ServiceItem` et ajouter à l'ObservableList
            for (ServiceHospitalier s : serviceList) {
                String imageUrl = getImagePathForService(s.getNomService()); // Obtenir l'image spécifique
                services.add(new ServiceItem(s.getNomService(), s.getDescription(), imageUrl));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des services: " + e.getMessage());
        }

        serviceListView.setItems(services);
        serviceListView.setCellFactory(listView -> new ServiceListCell());
    }

    // Méthode pour retourner le chemin de l'image selon le nom du service
    private String getImagePathForService(String serviceName) {
        switch (serviceName.toLowerCase()) {
            case "neurologie":
                return "/ines/images/Neuro.jpg";
            case "pédiatrie":
                return "/ines/images/pediatrie.jpg";
            case "gynécologie":
                return "/ines/images/genico.jpg";
            case "orthopédie":
                return "/ines/images/ortho.jpg";
            case "dermatologie":
                return "/ines/images/derma.jpg";
            case "cardiologie":
                return "/ines/images/cardio.jpg";
            default:
                return "/ines/images/default.jpg"; // Image par défaut si le service n'est pas reconnu
        }
    }

    // Méthode pour obtenir l'ID du service à partir de son nom
    private int getServiceIdFromName(String serviceName) {
        try {
            return serviceHospitalierServices.getServiceIdFromName(serviceName);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'ID du service : " + e.getMessage());
            return -1; // Valeur d'erreur
        }
    }

    // Méthode pour afficher les médecins du service
    private void showMedecinsForService(int idService) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ines/medecin_list.fxml"));
            AnchorPane medecinListView = loader.load();
            MedecinController controller = loader.getController();
            controller.loadMedecinsForService(idService);
            Stage stage = new Stage();
            stage.setTitle("Médecins spécialisés");
            stage.setScene(new Scene(medecinListView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe interne pour représenter un service
    public static class ServiceItem {
        private final String name;
        private final String description;
        private final String imageUrl;

        public ServiceItem(String name, String description, String imageUrl) {
            this.name = name;
            this.description = description;
            this.imageUrl = imageUrl;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    // Classe interne pour personnaliser chaque cellule de la ListView
    private static class ServiceListCell extends ListCell<ServiceItem> {
        private final HBox content;
        private final ImageView imageView;
        private final VBox textContainer;
        private final Text name;
        private final Text description;

        public ServiceListCell() {
            imageView = new ImageView();
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);

            name = new Text();
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            description = new Text();
            description.setStyle("-fx-font-size: 12px;");

            textContainer = new VBox(name, description);
            textContainer.setSpacing(5);

            content = new HBox(imageView, textContainer);
            content.setSpacing(10);
        }

        @Override
        protected void updateItem(ServiceItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                try {
                    Image image = new Image(getClass().getResource(item.getImageUrl()).toExternalForm());
                    imageView.setImage(image);
                    name.setText(item.getName());
                    description.setText(item.getDescription());
                    setGraphic(content);
                } catch (Exception e) {
                    System.err.println("Erreur de chargement de l'image : " + item.getImageUrl());
                    e.printStackTrace();
                }
            }
        }
    }

    // Méthode pour ouvrir la vue des rendez-vous
    @FXML
    private void openMesRendezVous() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ines/rendezvous-view.fxml"));
            AnchorPane rendezvousView = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Mes Rendez-vous");
            stage.setScene(new Scene(rendezvousView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour rechercher un service
    @FXML
    private void searchService(String searchText) {
        ObservableList<ServiceItem> filteredServices = FXCollections.observableArrayList();

        try {
            // Récupérer tous les services depuis la base de données
            List<ServiceHospitalier> serviceList = serviceHospitalierServices.readList();

            // Filtrer les services dont le nom correspond à la recherche
            for (ServiceHospitalier s : serviceList) {
                if (s.getNomService().toLowerCase().contains(searchText.toLowerCase())) {
                    String imageUrl = getImagePathForService(s.getNomService());
                    filteredServices.add(new ServiceItem(s.getNomService(), s.getDescription(), imageUrl));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des services: " + e.getMessage());
        }

        // Mettre à jour la liste avec les résultats filtrés
        serviceListView.setItems(filteredServices);
    }


    @FXML
    private void onBesoinLitClicked(ActionEvent event) {
        // Récupérer le service sélectionné dans le ComboBox
        String selectedServiceName = comboService.getSelectionModel().getSelectedItem();

        if (selectedServiceName == null) {
            showAlert("Erreur", "Veuillez sélectionner un service.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Récupérer le service complet depuis la base de données
            int serviceId = serviceHospitalierServices.getServiceIdFromName(selectedServiceName);
            ServiceHospitalier service = serviceHospitalierServices.getServiceById(serviceId);

            // Vérifier le nombre de lits disponibles
            if (service.getNombreLitsDisponibles() > 0) {
                // Décrémenter le nombre de lits disponibles
                service.setNombreLitsDisponibles(service.getNombreLitsDisponibles() - 1);

                // Mettre à jour le service dans la base de données
                serviceHospitalierServices.update(service);

                // Afficher un message de confirmation
                showAlert("Succès", "Votre lit est réservé pour le service " + selectedServiceName + ".", Alert.AlertType.INFORMATION);
            } else {
                // Afficher un message d'erreur si aucun lit n'est disponible
                showAlert("Indisponible", "Aucun lit disponible pour le service " + selectedServiceName + ".", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de la réservation du lit.", Alert.AlertType.ERROR);
        }
    }

    // Méthode utilitaire pour afficher une alerte
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
}}
