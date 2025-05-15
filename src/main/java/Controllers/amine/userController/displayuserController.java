package Controllers.amine.userController;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import entities.amine.User;
import services.amine.userService;
import utils.amine.navigation;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class displayuserController {

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> ageColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TableColumn<User, User> actionColumn;

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
        usersTable.setItems(observableUsers);

        // Set up the columns
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        ageColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user.getBirthday() != null) {
                int age = LocalDate.now().getYear() - user.getBirthday().getYear();
                return new SimpleStringProperty(age + " years");
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        statusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.isActive() ? "Active" : "Inactive");
        });
        actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));

        // Set up the action column with Block/Unblock buttons
        actionColumn.setCellFactory(col -> new TableCell<User, User>() {
            private final Button showButton = new Button("Show User");
            private final HBox buttonBox = new HBox(5); // spacing between buttons

            {
                buttonBox.setAlignment(Pos.CENTER);
                showButton.setStyle("-fx-background-color: #0275d8; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    showButton.setOnAction(event -> DetailsPopup(user));

                    showButton.setStyle("-fx-background-color: #0275d8; -fx-text-fill: white; -fx-font-weight: bold;");
                    buttonBox.getChildren().setAll(showButton);
                    setGraphic(buttonBox);
                }
            }


        });

    }

    @FXML
    public void searchUsers() {
        Integer minAge = parseInteger(minAgeField.getText());
        Integer maxAge = parseInteger(maxAgeField.getText());
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();

        List<User> filteredUsers = us.searchUsers(minAge, maxAge, name, cinField.getText(), address);
        ObservableList<User> observableUsers = FXCollections.observableArrayList(filteredUsers);
        usersTable.setItems(observableUsers);
    }

    private void DetailsPopup(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("User Details");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setPrefWidth(450);

        // Load the image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image("file:" + user.getPathtopic(), true);
            imageView.setImage(image);
            imageView.setFitWidth(120);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }

        VBox container = new VBox(10);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        // Add image
        container.getChildren().add(imageView);

        // User details grid
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        int row = 0;
        grid.addRow(row++, new Label("CIN:"), new Label(user.getCIN()));
        grid.addRow(row++, new Label("Name:"), new Label(user.getName()));
        grid.addRow(row++, new Label("Email:"), new Label(user.getEmail()));
        grid.addRow(row++, new Label("Phone:"), new Label(user.getPhone()));
        grid.addRow(row++, new Label("Address:"), new Label(user.getAddress()));
        grid.addRow(row++, new Label("Birthday:"), new Label(user.getBirthday().toString()));

        // Status (Active / Verified)
        Label activeLabel = new Label(user.isActive() ? "Active" : "Inactive");
        activeLabel.setTextFill(user.isActive() ? Color.GREEN : Color.GRAY);

        Label verifiedLabel = new Label(user.isVerified() ? "Verified" : "Unverified");
        verifiedLabel.setTextFill(user.isVerified() ? Color.DARKORANGE : Color.GRAY);

        grid.addRow(row++, new Label("Active Status:"), activeLabel);
        grid.addRow(row++, new Label("Verified:"), verifiedLabel);

        container.getChildren().add(grid);

        dialog.getDialogPane().setContent(container);

        // Add Edit and Close buttons
        ButtonType DeleteUserButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType BlockUserButton = new ButtonType("Update Status", ButtonBar.ButtonData.OTHER);
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(DeleteUserButton,BlockUserButton, closeButtonType);

        // Handle button actions
        dialog.showAndWait().ifPresent(response -> {
            if (response == DeleteUserButton) {
                showConfirmation("Are you sure you want to delete this user?", () -> {
                    us.delete(user);
                    showInfo("User deleted successfully.");
                });

            } else if (response == BlockUserButton) {
                showConfirmation("Are you sure you want to update the user’s status?", () -> {
                    us.updateUserStatus(user);
                    showInfo("User status updated.");
                });

            }
        });
    }

    private void showInfo(String message) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Information");
        info.setHeaderText(null);
        info.setContentText(message);
        info.showAndWait();
        initialize();
    }

    private void showConfirmation(String message, Runnable onConfirm) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(message);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirm.run();
        }
    }


    // Utility method to add a row to the GridPane
    private void addRow(GridPane grid, int rowIndex, String label, String value) {
        Label lbl = new Label(label);
        TextField field = new TextField(value);
        field.setEditable(false);
        grid.add(lbl, 0, rowIndex);
        grid.add(field, 1, rowIndex);
    }


    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-admin-view.fxml");
    }

    private void toggleUser(User user) {
        boolean newStatus = !user.isActive();
        user.setActive(newStatus);
        us.updateUserStatus(user);
        usersTable.refresh(); // Refresh the table to update the button state
    }

    private Integer parseInteger(String value) {
        try {
            return value.isEmpty() ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}