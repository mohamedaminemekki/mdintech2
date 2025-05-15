    package Controllers.Mohamed;
    import com.sendgrid.*;
    import com.sendgrid.helpers.mail.Mail;
    import com.sendgrid.helpers.mail.objects.Content;
    import com.sendgrid.helpers.mail.objects.Email;

    import java.util.*;

    import javafx.collections.FXCollections;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.geometry.Insets;
    import javafx.geometry.Pos;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.*;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.layout.*;
    import javafx.stage.Stage;
    import entities.Mohamed.Reclamation;
    import entities.Mohamed.Reponse;
    import services.Mohamed.ReclamationServices;
    import services.Mohamed.ReponseServices;

    import java.io.IOException;
    import java.sql.SQLException;
    import java.util.logging.Logger;
    import java.util.stream.Collectors;

    public class AfficherReclamation {
        @FXML
        private Button btnStats;
        @FXML
        private ListView<Reclamation> listViewReclamations;

        @FXML
        private Button btnActualiser, btnSearch;

        @FXML
        private TextArea descriptionArea;

        @FXML
        private ComboBox<String> chercherPar;

        @FXML
        private TextField searchField;

        private final ReclamationServices reclamationService = new ReclamationServices();
        private final ReponseServices reponseService = new ReponseServices();
        private List<Reclamation> allReclamations = new ArrayList<>(); // Store all reclamations
        private static final Logger logger = Logger.getLogger(AfficherReclamation.class.getName());

        @FXML
        private void initialize() {
            btnStats.setOnAction(this::goToStatsPage);

            VBox.setVgrow(descriptionArea, Priority.NEVER); // Prevent it from stretching
            chercherPar.setItems(FXCollections.observableArrayList("Client ID", "Type", "Date", "État"));
            chercherPar.setPromptText("Sélectionner le type de recherche");

            // Load and sort reclamations
            chargerReclamations();

            btnActualiser.setOnAction(event -> chargerReclamations());
            btnSearch.setOnAction(event -> rechercherReclamations());

            final boolean[] isReponseClicked = {false}; // Variable pour gérer l'état de clic

            listViewReclamations.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Reclamation rec, boolean empty) {
                    super.updateItem(rec, empty);
                    if (empty || rec == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10);
                        gridPane.setVgap(5);
                        gridPane.setStyle("-fx-padding: 10px; -fx-border-radius: 5px;");
                        gridPane.setAlignment(Pos.CENTER_LEFT);

                        // Set background color based on priority
                        String priorityColor = "";
                        switch (rec.getPriorite()) {
                            case "Haute":
                                priorityColor = "-fx-background-color: #fde2e2;"; // Light red
                                break;
                            case "Moyenne":
                                priorityColor = "-fx-background-color: #f8fde2;"; // Light orange
                                break;
                            case "Faible":
                                priorityColor = "-fx-background-color: #e2edfd;"; // Light green
                                break;
                            default:
                                priorityColor = "-fx-background-color: #ffffff;"; // White (default)
                        }
                        gridPane.setStyle(priorityColor + "-fx-padding: 10px; -fx-border-radius: 5px;");

                        // Define column constraints (match with header GridPane)
                        ColumnConstraints col1 = new ColumnConstraints();
                        col1.setPercentWidth(5); // ID
                        ColumnConstraints col2 = new ColumnConstraints();
                        col2.setPercentWidth(10); // Client ID
                        ColumnConstraints col3 = new ColumnConstraints();
                        col3.setPercentWidth(15); // Type
                        ColumnConstraints col4 = new ColumnConstraints();
                        col4.setPercentWidth(15); // Date
                        ColumnConstraints col5 = new ColumnConstraints();
                        col5.setPercentWidth(10); // État
                        ColumnConstraints col6 = new ColumnConstraints();
                        col6.setPercentWidth(30); // Réponse
                        ColumnConstraints col7 = new ColumnConstraints();
                        col7.setPercentWidth(10); // Action

                        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6, col7);

                        // Create Labels for each column
                        Label idLabel = createLabel(String.valueOf(rec.getId()), "-fx-font-weight: bold; -fx-text-fill: #333;");
                        Label clientLabel = createLabel(String.valueOf(rec.getClient_id()), "-fx-text-fill: #333;");
                        Label typeLabel = createLabel(rec.getType(), "-fx-font-style: italic; -fx-text-fill: #333;");
                        Label dateLabel = createLabel(rec.getDatee(), "-fx-text-fill: #777;");
                        Label etatLabel = createLabel(rec.getState() ? "Traité" : "Non traité", rec.getState() ? "-fx-text-fill: #195204; -fx-font-weight: bold;" : "-fx-text-fill: #154c79; -fx-font-weight: bold;");
                        // Inside the ListCell's updateItem method
                        etatLabel.setOnMouseClicked(event -> {
                            toggleState(rec, etatLabel); // Toggle the state
                            if (rec.getState()) { // If the reclamation is marked as resolved
                                sendEmail(rec.getEmail(), "Votre réclamation a été résolue",
                                        "Nous avons résolu votre réclamation. Merci de nous avoir contactés !");
                            }
                        });
                        // Load action icons
                        ImageView deleteIcon = new ImageView(new Image("/Mohamed/images/delete.png"));
                        deleteIcon.setFitWidth(20);
                        deleteIcon.setFitHeight(20);
                        deleteIcon.setOnMouseClicked(event -> supprimerReclamation(rec));

                        ImageView repondreIcon = new ImageView(new Image("/Mohamed/images/reply.png"));
                        repondreIcon.setFitWidth(20);
                        repondreIcon.setFitHeight(20);
                        repondreIcon.setOnMouseClicked(event -> ouvrirFenetreReponse(rec));

                        HBox actionsBox = new HBox(10);
                        actionsBox.getChildren().addAll(deleteIcon, repondreIcon);

                        // Fetch response for the reclamation
                        Reponse reponse = null;
                        try {
                            List<Reponse> reponses = reponseService.readList();
                            for (Reponse rep : reponses) {
                                if (rep.getReclamationId() == rec.getId()) {
                                    reponse = rep;
                                    break;
                                }
                            }
                        } catch (SQLException e) {
                            System.err.println("Erreur lors de la récupération des réponses : " + e.getMessage());
                        }

                        // Display response text
                        String reponseTexte = (reponse != null) ? reponse.getMessage() : "Pas de réponse";
                        if (reponseTexte.length() > 100) {
                            reponseTexte = reponseTexte.substring(0, 100) + "...";
                        }
                        Label reponseLabel = createLabel(reponseTexte, "-fx-text-fill: black; -fx-cursor: hand; -fx-underline: true;");

                        Reponse finalReponse = reponse;
                        reponseLabel.setOnMouseClicked(event -> {
                            if (finalReponse != null) {
                                descriptionArea.setText(finalReponse.getMessage());
                                isReponseClicked[0] = true;
                            } else {
                                descriptionArea.setText("Aucune réponse disponible.");
                                isReponseClicked[0] = true;
                            }
                            event.consume(); // Prevent click propagation
                        });
                        reponseLabel.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 1) {
                                // Un seul clic : afficher la réponse dans la zone de texte existante
                                if (finalReponse != null) {
                                    descriptionArea.setText(finalReponse.getMessage());
                                    isReponseClicked[0] = true;
                                } else {
                                    descriptionArea.setText("Aucune réponse disponible.");
                                    isReponseClicked[0] = true;
                                }
                                event.consume();
                            } else if (event.getClickCount() == 2) {
                                // Double clic : ouvrir une fenêtre de modification
                                if (finalReponse != null) {
                                    ouvrirFenetreModification(finalReponse);
                                }
                            }
                        });

                        // Add elements to the GridPane
                        gridPane.add(idLabel, 0, 0);
                        gridPane.add(clientLabel, 1, 0);
                        gridPane.add(typeLabel, 2, 0);
                        gridPane.add(dateLabel, 3, 0);
                        gridPane.add(etatLabel, 4, 0);
                        gridPane.add(reponseLabel, 5, 0);
                        gridPane.add(actionsBox, 6, 0);

                        // Bind GridPane width to ListView width
                        gridPane.prefWidthProperty().bind(listViewReclamations.widthProperty().subtract(20)); // Adjust for padding

                        setGraphic(gridPane);
                    }
                }
            });

            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteResponseItem = new MenuItem("Supprimer la réponse");
            deleteResponseItem.setOnAction(event -> {
                Reclamation selectedReclamation = listViewReclamations.getSelectionModel().getSelectedItem();
                if (selectedReclamation != null) {
                    try {
                        List<Reponse> reponses = reponseService.readList();
                        for (Reponse rep : reponses) {
                            if (rep.getReclamationId() == selectedReclamation.getId()) {
                                supprimerReponse(rep);
                                break;
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Erreur lors de la récupération des réponses : " + e.getMessage());
                    }
                }
            });
            contextMenu.getItems().add(deleteResponseItem);

            listViewReclamations.setContextMenu(contextMenu);

            listViewReclamations.setOnMouseClicked(event -> {
                if (!isReponseClicked[0]) { // Vérifier si un clic sur la réponse a été effectué
                    Reclamation selectedReclamation = listViewReclamations.getSelectionModel().getSelectedItem();
                    if (selectedReclamation != null) {
                        descriptionArea.setText(selectedReclamation.getDescription());
                    }
                }
                isReponseClicked[0] = false; // Réinitialiser après le clic
            });

        }
        @FXML
        private void goToStatsPage(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Mohamed/stats_page.fxml"));
                Parent statsPage = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(statsPage));
                stage.setTitle("Statistiques des Réclamations");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void ouvrirFenetreModification(Reponse finalReponse) {
            Stage stage = new Stage();
            stage.setTitle("Modifier la Réponse");

            VBox root = new VBox(10);
            root.setPadding(new Insets(10));

            Label label = new Label("Modifier la réponse:");
            TextArea textArea = new TextArea(finalReponse.getMessage());
            textArea.setWrapText(true);

            Button saveButton = new Button("Enregistrer");
            saveButton.setOnAction(e -> {
                try {
                    finalReponse.setMessage(textArea.getText()); // Update the response message
                    reponseService.update(finalReponse); // Update the response in the database

                    // Refresh the ListView to reflect the changes
                    listViewReclamations.refresh();

                    stage.close(); // Close the window
                } catch (SQLException ex) {
                    System.err.println("Erreur lors de la mise à jour : " + ex.getMessage());
                }
            });

            root.getChildren().addAll(label, textArea, saveButton);
            Scene scene = new Scene(root, 300, 200);
            stage.setScene(scene);
            stage.show();
        }

        private void ouvrirFenetreReponse(Reclamation rec) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Mohamed/AjouterReponse.fxml"));
                Parent root = loader.load();

                AjouterReponse controller = loader.getController();
                controller.setReclamationId(rec.getId());

                Stage stage = new Stage();
                stage.setTitle("Répondre à la Réclamation");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                afficherAlerte("Erreur", "Impossible d'ouvrir la fenêtre de réponse: " + e.getMessage());
            }
        }


        private Label createLabel(String text, String style) {
            Label label = new Label(text);
            label.setStyle(style);
            return label;
        }

        private void chargerReclamations() {
            try {
                // Récupérer toutes les réclamations
                allReclamations = reclamationService.readList();

                // Définir la priorité des réclamations (Haute > Moyenne > Faible)
                Map<String, Integer> priorityOrder = Map.of(
                        "Haute", 1,
                        "Moyenne", 2,
                        "Faible", 3
                );

                // Trier : Non traitées en haut, puis les traitées en bas
                Comparator<Reclamation> customComparator = Comparator
                        .comparing(Reclamation::getState) // 1. Non traité (false) avant Traité (true)
                        .thenComparing(rec -> priorityOrder.getOrDefault(rec.getPriorite(), 4)); // 2. Trier par priorité

                // Appliquer le tri
                allReclamations.sort(customComparator);

                // Mettre à jour la liste dans ListView
                listViewReclamations.setItems(FXCollections.observableArrayList(allReclamations));
            } catch (SQLException e) {
                System.err.println("Erreur lors du chargement des réclamations : " + e.getMessage());
            }
        }

        private void sendEmail(String toEmail, String subject, String body) {
            try {
                // Replace with your SendGrid API key
                String apiKey = "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"; // Replace with your actual API key

                // Set up the email
                Email from = new Email("dridi.mohammed01@gmail.com", "Smart City Support"); // Replace with your verified sender email
                Email to = new Email(toEmail);

                // Feedback form link
                String feedbackLink = "https://docs.google.com/forms/d/e/1FAIpQLSdGhjaGDYHK71IiBaPcTN5Bo0HGdEdKfpAZaEfcM5rAZkYwFg/viewform?usp=header";

                // Customize email content with feedback function
                String htmlBody = "<html><body>"
                        + "<p><strong>" + body + "</strong></p>"
                        + "<p>Nous vous remercions pour votre réclamation.</p>"
                        + "<p>Votre retour est important pour nous ! Veuillez partager votre expérience en remplissant ce court formulaire :</p>"
                        + "<p><a href='" + feedbackLink + "' style='color:blue; font-weight:bold;'>Donner mon avis</a></p>"
                        + "<p>Cordialement, <br><strong>Smart City Support</strong></p>"
                        + "</body></html>";

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
                    System.out.println("Email sent successfully.");
                } else {
                    System.out.println("Failed to send email: " + response.getStatusCode() + " - " + response.getBody());
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            }
        }





        private void rechercherReclamations() {
            String searchText = searchField.getText().toLowerCase();
            String searchType = chercherPar.getValue();

            List<Reclamation> filteredReclamations = allReclamations.stream()
                    .filter(rec -> {
                        switch (searchType) {
                            case "Client ID":
                                return String.valueOf(rec.getClient_id()).contains(searchText);
                            case "Type":
                                return rec.getType().toLowerCase().contains(searchText);
                            case "Date":
                                return rec.getDatee().toLowerCase().contains(searchText);
                            case "État":
                                return (rec.getState() ? "Traité" : "Non traité").toLowerCase().contains(searchText);
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toList());

            // Sort the filtered reclamations by priority
            Comparator<Reclamation> priorityComparator = (rec1, rec2) -> {
                Map<String, Integer> priorityOrder = Map.of(
                        "Haute", 1,
                        "Moyenne", 2,
                        "Faible", 3
                );
                return Integer.compare(priorityOrder.get(rec1.getPriorite()), priorityOrder.get(rec2.getPriorite()));
            };

            filteredReclamations.sort(priorityComparator);

            // Set the sorted and filtered reclamations in the ListView
            listViewReclamations.setItems(FXCollections.observableArrayList(filteredReclamations));
        }

        private void toggleState(Reclamation rec, Label etatLabel) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Changer l'état de la réclamation");
            alert.setContentText("Êtes-vous sûr de vouloir " + (rec.getState() ? "marquer cette réclamation comme Non traité ?" : "traiter cette réclamation ?"));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean newState = !rec.getState();
                rec.setState(newState);
                etatLabel.setText(newState ? "Traité" : "Non traité");
                etatLabel.setStyle(newState ? "-fx-text-fill: #195204; -fx-font-weight: bold;" : "-fx-text-fill: #154c79; -fx-font-weight: bold;");

                try {
                    reclamationService.update(rec);
                    afficherAlerte("Succès", "L'état de la réclamation a été mis à jour avec succès.");
                } catch (SQLException e) {
                    afficherAlerte("Erreur", "Impossible de mettre à jour l'état de la réclamation : " + e.getMessage());
                }
            } else {
                afficherAlerte("Annulé", "Aucune modification n'a été apportée.");
            }
        }


        private void supprimerReclamation(Reclamation rec) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmer la suppression");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        reclamationService.delete(rec.getId());
                        listViewReclamations.getItems().remove(rec);
                    } catch (SQLException e) {
                        afficherAlerte("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                    }
                }
            });
        }
        private void supprimerReponse(Reponse reponse) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmer la suppression");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        reponseService.delete(reponse.getId()); // Delete the response from the database
                        chargerReclamations(); // Refresh the ListView to reflect the changes
                    } catch (SQLException e) {
                        afficherAlerte("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                    }
                }
            });
        }

        private void afficherAlerte(String titre, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
        public void retournerPagePrecedente(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-admin-view.fxml")); // Replace with the correct FXML file
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Menu Principal");
                stage.show();
            } catch (IOException e) {
                System.out.println("mamchetech");
            }
        }

    }