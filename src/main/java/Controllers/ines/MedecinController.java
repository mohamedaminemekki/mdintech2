package Controllers.ines;

import entities.ines.Medecin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import services.ines.MedecinServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.sql.SQLException;
import java.util.List;

public class MedecinController {

    @FXML
    private ListView<MedecinItem> medecinListView;

    public void loadMedecinsForService(int idService) {
        MedecinServices medecinServices = new MedecinServices();
        ObservableList<MedecinItem> medecins = FXCollections.observableArrayList();

        try {
            List<Medecin> medecinList = medecinServices.getMedecinsByService(idService);
            System.out.println("Service ID: " + idService);
            if (medecinList.isEmpty()) {
                System.out.println("Aucun médecin trouvé pour ce service.");
            }
            for (Medecin medecin : medecinList) {
                String imagePath = "/ines/images/" + medecin.getNomM().toLowerCase() + ".jpg";  // Image du médecin
                medecins.add(new MedecinItem(medecin.getIdMedecin(), medecin.getNomM(), medecin.getPrenomM(), medecin.getSpecialite(), medecin.getContact(), imagePath));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        medecinListView.setItems(medecins);
        medecinListView.setCellFactory(listView -> new MedecinListCell());
    }

    public static class MedecinItem {
        private final int id;
        private final String nom;
        private final String prenom;
        private final String specialite;
        private final int contact;
        private final String imageUrl;

        public MedecinItem(int id, String nom, String prenom, String specialite, int contact, String imageUrl) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.specialite = specialite;
            this.contact = contact;
            this.imageUrl = imageUrl;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getSpecialite() { return specialite; }
        public int getContact() { return contact; }
        public String getImageUrl() { return imageUrl; }
    }

    private static class MedecinListCell extends ListCell<MedecinItem> {
        private final HBox content;
        private final ImageView imageView;
        private final VBox textContainer;
        private final Text nom;
        private final Text specialite;
        private final Text contact;
        private final Button rdvButton;

        public MedecinListCell() {
            imageView = new ImageView();
            imageView.setFitWidth(60);
            imageView.setFitHeight(60);
            imageView.setStyle("-fx-border-radius: 30px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 2, 2);");

            nom = new Text();
            nom.setFont(Font.font("Roboto", FontWeight.BOLD, 16));
            nom.setFill(Color.web("#333333"));

            specialite = new Text();
            specialite.setFont(Font.font("Roboto", 14));
            specialite.setFill(Color.web("#555555"));

            contact = new Text();
            contact.setFont(Font.font("Roboto", 14));
            contact.setFill(Color.web("#777777"));

            textContainer = new VBox(nom, specialite, contact);
            textContainer.setSpacing(5);

            rdvButton = new Button("Prendre Rendez-vous");
            rdvButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px;");
            rdvButton.setOnMouseEntered(e -> rdvButton.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px;"));
            rdvButton.setOnMouseExited(e -> rdvButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px;"));
            rdvButton.setOnAction(event -> openRendezVousForm(getItem()));

            content = new HBox(imageView, textContainer, rdvButton);
            content.setSpacing(20);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(10));
            content.setStyle("-fx-background-color: white; -fx-border-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 2, 2);");
        }

        @Override
        protected void updateItem(MedecinItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                try {
                    java.net.URL imageUrl = getClass().getResource(item.getImageUrl());
                    if (imageUrl != null) {
                        Image image = new Image(imageUrl.toExternalForm());
                        imageView.setImage(image);
                    } else {
                        // Fallback to default image if not found
                        java.net.URL defaultUrl = getClass().getResource("/ines/images/user.jpg");
                        if (defaultUrl != null) {
                            imageView.setImage(new Image(defaultUrl.toExternalForm()));
                        } else {
                            imageView.setImage(null);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur de chargement de l'image : " + item.getImageUrl());
                    e.printStackTrace();
                    imageView.setImage(null);
                }

                nom.setText(item.getNom() + " " + item.getPrenom());
                specialite.setText("Spécialité : " + item.getSpecialite());
                contact.setText("Contact : " + item.getContact());

                setGraphic(content);
            }
        }

        private void openRendezVousForm(MedecinItem medecin) {
            System.out.println("ID du médecin sélectionné : " + medecin.getId());
            RendezVousController controller = new RendezVousController();
            controller.showRendezVousForm(medecin);
        }
    }
}