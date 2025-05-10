package controllers.amine.userController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import entities.amine.User;
import services.amine.userService;
import utils.amine.navigation;

import java.io.IOException;
import java.util.List;

public class displayuserController {

    @FXML
    private ListView<User> usersList;

    @FXML
    private TextField minAgeField, maxAgeField, nameField, cinField, addressField;

    private userService us = new userService();

    @FXML
    public void initialize() {
        loadUsers();
    }

    @FXML
    public void loadUsers() {
        List<User> users = us.findAll();
        ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
        usersList.setItems(observableUsers);

        // Custom ListView Cell Renderer
        usersList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<User> call(ListView<User> param) {
                return new ListCell<>() {
                    private final ImageView profileImageView = new ImageView();
                    private final Button blockButton = new Button();
                    private final HBox cellLayout = new HBox(10); // Spacing between elements

                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);

                        if (empty || user == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(user.getName() + " - " + user.getPhone() + " - " + user.getEmail());

                            // Profile Image Placeholder
                            profileImageView.setImage(new Image("file:src/main/resources/profile_placeholder.png"));
                            profileImageView.setFitWidth(40);
                            profileImageView.setFitHeight(40);

                            // Configure Block/Unblock Button
                            updateBlockButton(user);
                            blockButton.setOnAction(event -> toggleUserStatus(user));

                            // Layout: Image | Text | Button
                            cellLayout.getChildren().setAll(profileImageView, blockButton);
                            setGraphic(cellLayout);
                        }
                    }

                    // Updates button text and style
                    private void updateBlockButton(User user) {
                        if (user.isStatus()) {
                            blockButton.setText("Block");
                            blockButton.setStyle(
                                    "-fx-background-color: red; -fx-text-fill: white; " +
                                            "-fx-border-radius: 10; -fx-background-radius: 10; " +
                                            "-fx-padding: 5px 10px; -fx-font-size: 14px;"
                            );
                        } else {
                            blockButton.setText("Unblock");
                            blockButton.setStyle(
                                    "-fx-background-color: green; -fx-text-fill: white; " +
                                            "-fx-border-radius: 10; -fx-background-radius: 10; " +
                                            "-fx-padding: 5px 10px; -fx-font-size: 14px;"
                            );
                        }

                        // Hover effect
                        blockButton.setOnMouseEntered(e -> blockButton.setStyle("-fx-opacity: 0.8;"));
                        blockButton.setOnMouseExited(e -> blockButton.setStyle("-fx-opacity: 1;"));
                    }

                    // Toggle user status
                    private void toggleUserStatus(User user) {
                        boolean newStatus = !user.isStatus();
                        user.setStatus(newStatus);
                        us.updateUserStatus(user.getCIN(), newStatus);
                        updateBlockButton(user);
                    }
                };
            }
        });
    }

    @FXML
    public void searchUsers() {
        Integer minAge = parseInteger(minAgeField.getText());
        Integer maxAge = parseInteger(maxAgeField.getText());
        String name = nameField.getText().trim();
        Integer cin = parseInteger(cinField.getText());
        String address = addressField.getText().trim();

        List<User> filteredUsers = us.searchUsers(minAge, maxAge, name, cin, address);
        ObservableList<User> observableUsers = FXCollections.observableArrayList(filteredUsers);
        usersList.setItems(observableUsers);
    }

    private void showUserDetailsPopup(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Details");
        alert.setHeaderText("Information of " + user.getName());
        alert.setContentText(
                "Email: " + user.getEmail() + "\n" +
                        "Phone: " + user.getPhone() + "\n" +
                        "Address: " + user.getAddress() + "\n" +
                        "City: " + user.getCity() + "\n" +
                        "State: " + user.getState() + "\n" +
                        "Role: " + user.getRole() + "\n" +
                        "Status: " + (user.isStatus() ? "Active" : "Inactive")
        );
        alert.showAndWait();
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-admin-view.fxml");
    }

    private Integer parseInteger(String value) {
        try {
            return value.isEmpty() ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
