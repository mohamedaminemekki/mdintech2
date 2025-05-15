package utils.amine;

import Singleton.loggedInUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import entities.amine.User;
import io.github.cdimascio.dotenv.Dotenv;
import com.sun.net.httpserver.HttpServer;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.json.JSONObject;
import services.amine.userService;
import javafx.application.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;

public class GoogleCallbackServer {
    static Dotenv dotenv = Dotenv.load();
    private static final String REDIRECT_URI = "http://localhost:8082/callback";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    static userService us = new userService();
    private static navigation nav = new navigation(); // Instance of the navigation class
    private static ActionEvent event; // Store the ActionEvent
    private static HttpServer server; // Store the HttpServer instance

    public static void setEvent(ActionEvent event) {
        GoogleCallbackServer.event = event;
    }

    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8082), 0); // Initialize the server

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String response = "Google authentication successful! You can close this tab.";

            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];

                try {
                    // Exchange authorization code for access token
                    TokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                            HTTP_TRANSPORT,
                            JSON_FACTORY,
                            "https://oauth2.googleapis.com/token",
                            dotenv.get("CLIENT_ID"),
                            dotenv.get("CLIENT_SECRET"),
                            code,
                            REDIRECT_URI
                    ).execute();

                    String accessToken = tokenResponse.getAccessToken();
                    System.out.println("Access Token: " + accessToken);

                    // Fetch user info using access token
                    String rawResponse = getUserInfo(accessToken);
                    String userInfo = rawResponse.trim(); // Trim the response

                    // Parse the user info JSON
                    JSONObject userInfoJson = new JSONObject(userInfo); // Use the correct variable
                    System.out.println("Parsed User Info: " + userInfoJson.toString(2));
                    String email = userInfoJson.getString("email");
                    userService userService = new userService();
                    User user = userService.findByEmail(email);

                    if (user != null) {
                        loggedInUser.initializeSession(user);
                        Platform.runLater(() -> {
                            try {
                                stopServer(); // Stop the server before switching scenes
                                nav.switchScene(event, "/main-user-view.fxml");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        // User does not exist, show a pop-up to collect additional information
                        showUserRegistrationPopup(userInfoJson);
                    }

                    response = "User Info: " + userInfo;

                } catch (Exception e) {
                    response = "Error retrieving user info: " + e.getMessage();
                    e.printStackTrace();
                }
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(5));
        server.start();
        System.out.println("Server started at http://localhost:8082/callback");
    }

    private static void stopServer() {
        if (server != null) {
            server.stop(0); // Stop the server with a delay of 0 seconds
            System.out.println("Server stopped.");
        }
    }

    private static String getUserInfo(String accessToken) throws IOException {
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        HttpRequest request = requestFactory.buildGetRequest(new com.google.api.client.http.GenericUrl(
                "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken));
        HttpResponse response = request.execute();
        return response.parseAsString();
    }

    private static void showUserRegistrationPopup(JSONObject userInfo) {
        Platform.runLater(() -> {
            // Create a pop-up dialog with fields for CIN, phone, and address
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Complete Registration");
            dialog.setHeaderText("Please provide additional information to complete your registration.");

            // Set the button types
            ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

            // Create the form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField cinField = new TextField();
            cinField.setPromptText("CIN");
            TextField phoneField = new TextField();
            phoneField.setPromptText("Phone");
            TextField addressField = new TextField();
            addressField.setPromptText("Address");
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Password");
            TextArea bioField = new TextArea();
            bioField.setPromptText("Tell us a bit about yourself...");
            bioField.setWrapText(true);
            bioField.setPrefRowCount(3);


            grid.add(new Label("CIN:"), 0, 0);
            grid.add(cinField, 1, 0);
            grid.add(new Label("Phone:"), 0, 1);
            grid.add(phoneField, 1, 1);
            grid.add(new Label("Address:"), 0, 2);
            grid.add(addressField, 1, 2);
            grid.add(new Label("Password:"), 0, 3);
            grid.add(passwordField, 1, 3);
            grid.add(new Label("Bio:"), 0, 4);
            grid.add(bioField, 1, 4);



            dialog.getDialogPane().setContent(grid);

            // Convert the result to a User object when the register button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == registerButtonType) {
                    return new Pair<>(cinField.getText(), phoneField.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();

            result.ifPresent(cinPhonePair -> {
                // Create a new user with the information from the OAuth2 response and the pop-up
                String name = userInfo.getString("given_name") + " " + userInfo.getString("family_name");
                String email = userInfo.getString("email");
                int cin = Integer.parseInt(cinPhonePair.getKey());
                String phone = cinPhonePair.getValue();
                String address = addressField.getText();
                String password = passwordField.getText();
                String bio = bioField.getText();


                // Create a new User object
                User newUser = new User(
                        name,
                        Integer.toString(cin),
                        email,
                        password.isEmpty() ? "defaultPassword" : password,
                        "ROLE_USER", // Default role
                        phone,
                        address,
                        userInfo.getString("picture"),
                        new Date(),
                        bio
                );
                String googleId = userInfo.optString("sub"); // 'sub' is the unique Google ID field
                newUser.setGoogleId(googleId); // Set the Google ID manually

                // Save the new user to the database
                try {
                    us.saveGoogle(newUser);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                // Stop the server and redirect to the desired page
                try {
                    stopServer(); // Stop the server before switching scenes
                    loggedInUser.initializeSession(newUser);
                    nav.switchScene(event, "/main-user-view.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}