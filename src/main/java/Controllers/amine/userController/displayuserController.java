package Controllers.amine.userController;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import entities.amine.User;
import services.amine.userService;
import utils.amine.navigation;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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

    private void  DetailsPopup(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User  Details");
        alert.setHeaderText("Information of " + user.getName());
        alert.setContentText(
                "Email: " + user.getEmail() + "\n" +
                        "Phone: " + user.getPhone() + "\n" +
                        "Address: " + user.getAddress() + "\n" +
                        "Role: " + user.getRoles().get(0) + "\n" +
                        "Status: " + (user.isActive() ? "Active" : "Inactive")
        );
        alert.showAndWait();
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-admin-view.fxml");
    }

    private void toggleUser(User user) {
        boolean newStatus = !user.isActive();
        user.setActive(newStatus);
        us.updateUserStatus(user .getCIN(), newStatus);
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