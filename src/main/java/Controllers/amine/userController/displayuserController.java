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
import javafx.util.Callback;
import entities.amine.User;
import services.amine.userService;
import utils.amine.navigation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private TextField minAgeField, maxAgeField, nameField, cinField, addressField, emailField;

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label inactiveUsersLabel;

    private userService us = new userService();
    private ObservableList<User> allUsers;

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        updateStatistics();
    }

    private void setupTable() {
        // Set up the columns with modern styling
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        ageColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user.getBirthday() != null) {
                // Convert java.sql.Date to LocalDate
                LocalDate birthday = ((java.sql.Date) user.getBirthday()).toLocalDate();
                LocalDate now = LocalDate.now();
                int age = now.getYear() - birthday.getYear();
                // Adjust age if birthday hasn't occurred this year
                if (birthday.getMonthValue() > now.getMonthValue() || 
                    (birthday.getMonthValue() == now.getMonthValue() && 
                     birthday.getDayOfMonth() > now.getDayOfMonth())) {
                    age--;
                }
                return new SimpleStringProperty(age + " years");
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        
        // Status column with colored badges
        statusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.isActive() ? "Active" : "Inactive");
        });
        statusColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label label = new Label(item);
                    label.setStyle(
                        item.equals("Active") 
                            ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;"
                            : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;"
                    );
                    setGraphic(label);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Action column with modern buttons
        actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionColumn.setCellFactory(col -> new TableCell<User, User>() {
            private final Button showButton = new Button("Details");
            private final HBox buttonBox = new HBox(5);

            {
                buttonBox.setAlignment(Pos.CENTER);
                showButton.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 3; -fx-padding: 5 15;"
                );
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    showButton.setOnAction(event -> DetailsPopup(user));
                    buttonBox.getChildren().setAll(showButton);
                    setGraphic(buttonBox);
                }
            }
        });

        // Add row hover effect
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent;");
            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #f8f9fa;");
                }
            });
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: transparent;");
                }
            });
            return row;
        });
    }

    @FXML
    public void loadUsers() {
        allUsers = FXCollections.observableArrayList(us.findAll());
        usersTable.setItems(allUsers);
        updateStatistics();
    }

    private void updateStatistics() {
        if (allUsers != null) {
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
            long activeCount = allUsers.stream().filter(User::isActive).count();
            activeUsersLabel.setText(String.valueOf(activeCount));
            inactiveUsersLabel.setText(String.valueOf(allUsers.size() - activeCount));
        }
    }

    @FXML
    public void searchUsers() {
        String name = nameField.getText().trim().toLowerCase();
        String cin = cinField.getText().trim().toLowerCase();
        String address = addressField.getText().trim().toLowerCase();
        String email = emailField.getText().trim().toLowerCase();
        Integer minAge = parseInteger(minAgeField.getText());
        Integer maxAge = parseInteger(maxAgeField.getText());

        if (allUsers == null) {
            return;
        }

        List<User> filteredUsers = allUsers.stream()
            .filter(user -> {
                // Check name
                if (!name.isEmpty() && !user.getName().toLowerCase().contains(name)) {
                    return false;
                }
                
                // Check CIN
                if (!cin.isEmpty() && !user.getCIN().toLowerCase().contains(cin)) {
                    return false;
                }
                
                // Check address
                if (!address.isEmpty() && !user.getAddress().toLowerCase().contains(address)) {
                    return false;
                }

                // Check email
                if (!email.isEmpty() && !user.getEmail().toLowerCase().contains(email)) {
                    return false;
                }
                
                // Check age range
                if (user.getBirthday() != null) {
                    LocalDate birthday = ((java.sql.Date) user.getBirthday()).toLocalDate();
                    LocalDate now = LocalDate.now();
                    int age = now.getYear() - birthday.getYear();
                    
                    // Adjust age if birthday hasn't occurred this year
                    if (birthday.getMonthValue() > now.getMonthValue() || 
                        (birthday.getMonthValue() == now.getMonthValue() && 
                         birthday.getDayOfMonth() > now.getDayOfMonth())) {
                        age--;
                    }
                    
                    if (minAge != null && age < minAge) {
                        return false;
                    }
                    if (maxAge != null && age > maxAge) {
                        return false;
                    }
                } else if (minAge != null || maxAge != null) {
                    return false; // Filter out users without birthday if age range is specified
                }
                
                return true;
            })
            .collect(Collectors.toList());

        usersTable.setItems(FXCollections.observableArrayList(filteredUsers));
        updateFilteredStatistics(filteredUsers);
    }

    private void updateFilteredStatistics(List<User> filteredUsers) {
        totalUsersLabel.setText(String.valueOf(filteredUsers.size()));
        long activeCount = filteredUsers.stream().filter(User::isActive).count();
        activeUsersLabel.setText(String.valueOf(activeCount));
        inactiveUsersLabel.setText(String.valueOf(filteredUsers.size() - activeCount));
    }

    private void DetailsPopup(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("User Details");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        // Create a modern container for the dialog
        VBox container = new VBox(20);
        container.setStyle("-fx-padding: 20;");
        container.setAlignment(Pos.TOP_CENTER);

        // Profile image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image("file:" + user.getPathtopic(), true);
            imageView.setImage(image);
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        } catch (Exception e) {
            System.out.println("Failed to load image: " + e.getMessage());
        }
        container.getChildren().add(imageView);

        // User details in a modern card
        VBox detailsCard = new VBox(10);
        detailsCard.setStyle(
            "-fx-background-color: #f8f9fa; -fx-padding: 20; " +
            "-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        addDetailRow(detailsCard, "CIN", user.getCIN());
        addDetailRow(detailsCard, "Name", user.getName());
        addDetailRow(detailsCard, "Email", user.getEmail());
        addDetailRow(detailsCard, "Phone", user.getPhone());
        addDetailRow(detailsCard, "Address", user.getAddress());
        addDetailRow(detailsCard, "Birthday", user.getBirthday().toString());

        // Status indicators
        HBox statusBox = new HBox(20);
        statusBox.setAlignment(Pos.CENTER);
        
        Label activeLabel = createStatusLabel("Active Status", user.isActive() ? "Active" : "Inactive", 
            user.isActive() ? "#27ae60" : "#95a5a6");
        Label verifiedLabel = createStatusLabel("Verification", user.isVerified() ? "Verified" : "Unverified",
            user.isVerified() ? "#f39c12" : "#95a5a6");
        
        statusBox.getChildren().addAll(activeLabel, verifiedLabel);
        detailsCard.getChildren().add(statusBox);
        
        container.getChildren().add(detailsCard);
        dialog.getDialogPane().setContent(container);

        // Modern styled buttons
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        ButtonType updateStatusButton = new ButtonType("Update Status", ButtonBar.ButtonData.OTHER);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(deleteButton, updateStatusButton, closeButton);

        // Style the buttons
        dialog.getDialogPane().lookupButton(deleteButton)
            .setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        dialog.getDialogPane().lookupButton(updateStatusButton)
            .setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        dialog.getDialogPane().lookupButton(closeButton)
            .setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                showConfirmation("Are you sure you want to delete this user?", () -> {
                    us.delete(user);
                    showInfo("User deleted successfully.");
                    loadUsers();
                });
            } else if (response == updateStatusButton) {
                showConfirmation("Are you sure you want to update the user's status?", () -> {
                    us.updateUserStatus(user);
                    showInfo("User status updated.");
                    loadUsers();
                });
            }
        });
    }

    private Label createStatusLabel(String title, String status, String color) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        Label statusLabel = new Label(status);
        statusLabel.setStyle(
            String.format("-fx-background-color: %s; -fx-text-fill: white; " +
                        "-fx-padding: 5 15; -fx-background-radius: 3; -fx-font-weight: bold;", color)
        );
        
        container.getChildren().addAll(titleLabel, statusLabel);
        return statusLabel;
    }

    private void addDetailRow(VBox container, String label, String value) {
        VBox row = new VBox(5);
        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        row.getChildren().addAll(titleLabel, valueLabel);
        container.getChildren().add(row);
    }

    private void showInfo(String message) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Information");
        info.setHeaderText(null);
        info.setContentText(message);
        info.getDialogPane().setStyle("-fx-background-color: white;");
        info.showAndWait();
    }

    private void showConfirmation(String message, Runnable onConfirm) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(message);
        confirmAlert.getDialogPane().setStyle("-fx-background-color: white;");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirm.run();
        }
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