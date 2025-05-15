package Controllers.mariem;

import Singleton.dbConnection;
import com.mysql.cj.jdbc.JdbcConnection;
import entities.mariem.ChatManager;
import entities.mariem.Ville;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import utils.DatabaseConnection;
import java.util.List;
import java.util.Objects;

public class ChatController {
    // Le ListView contiendra des HBox représentant chacune une bulle de message
    @FXML private ListView<HBox> chatHistory;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private VBox chatContainer;
    @FXML private ProgressIndicator loadingIndicator;

    private List<Ville> villes;
    private ChatManager chatManager;

    public void initialize() {
        // Chargement des villes (mais elles ne sont pas affichées dès le démarrage)
        villes = dbConnection.loadCities();
        chatManager = new ChatManager(villes);

        // Styles généraux
        chatContainer.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 10;");
        chatHistory.setStyle("-fx-background-color: transparent;");
        messageInput.setStyle("-fx-background-radius: 20; -fx-padding: 10; -fx-font-size: 14px;");

        // Affichage d'un message de bienvenue personnalisé
        addBotMessage("Bonjour ! Je suis MdinaBot , votre guide virtuel. Posez-moi vos questions.");
    }

    @FXML
    private void envoyerMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) return;

        // Affichage du message de l'utilisateur
        addUserMessage(message);
        messageInput.clear();

        // Affichage de l'indicateur de chargement pendant le traitement
        loadingIndicator.setVisible(true);

        // Simulation d'un traitement asynchrone (vous pouvez utiliser Task/Service pour un vrai appel réseau)
        new Thread(() -> {
            ChatManager.ChatResponse response = chatManager.processMessage(message);
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                addBotMessage(response.getMessage());
                if (response.getOptions() != null && !response.getOptions().isEmpty()) {
                    // Chaque option est affichée comme un message séparé
                    for (String option : response.getOptions()) {
                        addBotMessage("Option : " + option);
                    }
                }
            });
        }).start();
    }

    // Ajoute une bulle de message pour l'utilisateur (alignée à droite)
    private void addUserMessage(String message) {
        HBox messageBubble = createMessageBubble(message, true);
        chatHistory.getItems().add(messageBubble);
        chatHistory.scrollTo(chatHistory.getItems().size() - 1);
    }

    // Ajoute une bulle de message pour le bot (alignée à gauche)
    private void addBotMessage(String message) {
        HBox messageBubble = createMessageBubble(message, false);
        chatHistory.getItems().add(messageBubble);
        chatHistory.scrollTo(chatHistory.getItems().size() - 1);
    }

    // Crée une bulle de message avec avatar et style adapté selon l'origine du message
    private HBox createMessageBubble(String message, boolean isUser) {
        HBox bubble = new HBox();
        bubble.setPadding(new Insets(5));
        bubble.setSpacing(10);

        // Label pour le texte du message avec un style de bulle
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setStyle("-fx-background-color: " + (isUser ? "#DCF8C6" : "#FFFFFF") + ";"
                + "-fx-padding: 10;"
                + "-fx-background-radius: 10;"
                + "-fx-border-radius: 10;");

        // Avatar (assurez-vous que les images se trouvent dans src/main/resources/images/)
        ImageView avatar = new ImageView();
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);
        if (isUser) {
            // Avatar pour l'utilisateur
            avatar.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/user.png"))));
            bubble.setAlignment(Pos.CENTER_RIGHT);
            bubble.getChildren().addAll(messageLabel, avatar);
        } else {
            // Avatar pour le chatbot
            avatar.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/bot.jpg"))));
            bubble.setAlignment(Pos.CENTER_LEFT);
            bubble.getChildren().addAll(avatar, messageLabel);
        }
        return bubble;
    }
}