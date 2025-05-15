package Controllers.tasnim;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.tasnim.Product;

import java.io.File;

public class ProductDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField referenceField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockLimitField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField imagePathField;
    @FXML
    private TextField soldField;
    @FXML
    private TextField descriptionField;
    @FXML
    private ComboBox<String> categoryComboBox;

    private Stage dialogStage;
    private Product product;
    private boolean okClicked = false;

    @FXML
    public void initialize() {
        // Populate categoryComboBox items programmatically
        categoryComboBox.getItems().setAll("Food", "Drinks", "Household products");
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProduct(Product product) {
        this.product = product;

        if (product != null) {
            nameField.setText(product.getName());
            referenceField.setText(product.getReference());
            priceField.setText(String.valueOf(product.getPrice()));
            stockLimitField.setText(String.valueOf(product.getStockLimit()));
            stockField.setText(String.valueOf(product.getStock()));
            imagePathField.setText(product.getImagePath());
            soldField.setText(String.valueOf(product.getSold()));
            descriptionField.setText(product.getDescription());
            categoryComboBox.setValue(product.getCategory());
        } else {
            categoryComboBox.getSelectionModel().clearSelection();
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public Product getProduct() {
        if (product == null) {
            // Gather all required fields from the dialog (with safe defaults)
            int id = 0;
            String name = nameField.getText();
            String reference = referenceField.getText();
            double price = parseDouble(priceField.getText());
            int stockLimit = parseInt(stockLimitField.getText());
            int stock = parseInt(stockField.getText());
            String imagePath = imagePathField.getText();
            int sold = parseInt(soldField.getText());
            String description = descriptionField.getText();
            String category = categoryComboBox.getValue();
            java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
            product = new Product(id, name, reference, price, stockLimit, stock, imagePath, sold, description, category, createdAt);
        }
        product.setName(nameField.getText());
        product.setReference(referenceField.getText());
        product.setPrice(parseDouble(priceField.getText()));
        product.setStockLimit(parseInt(stockLimitField.getText()));
        product.setStock(parseInt(stockField.getText()));
        product.setImagePath(imagePathField.getText());
        product.setSold(parseInt(soldField.getText()));
        product.setDescription(descriptionField.getText());
        product.setCategory(categoryComboBox.getValue());
        return product;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Ensure images/products directory exists (relative to resources)
                File uploadDir = new File("src/main/resources/images/products");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                // Copy file to images/products with a unique name
                String uniqueName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(uploadDir, uniqueName);
                java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                // Set the image path as images/products/filename (relative path)
                imagePathField.setText("images/products/" + uniqueName);
            } catch (Exception e) {
                showAlert("Error", "Failed to upload image: " + e.getMessage());
            }
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";

        // Validate nameField (only letters and spaces)
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            errorMessage += "Name is required!\n";
        } else if (!nameField.getText().matches("[a-zA-Z ]+")) {
            errorMessage += "Name can only contain letters and spaces!\n";
        }

        // Validate referenceField (no specific format, just not empty)
        if (referenceField.getText() == null || referenceField.getText().isEmpty()) {
            errorMessage += "Reference is required!\n";
        }

        // Validate priceField (only real numbers)
        if (priceField.getText() == null || priceField.getText().isEmpty()) {
            errorMessage += "Price is required!\n";
        } else if (!priceField.getText().matches("\\d*(\\.\\d*)?")) {
            errorMessage += "Price must be a valid number!\n";
        }

        // Validate stockLimitField (only integers)
        if (stockLimitField.getText() == null || stockLimitField.getText().isEmpty()) {
            errorMessage += "Stock Limit is required!\n";
        } else if (!stockLimitField.getText().matches("\\d*")) {
            errorMessage += "Stock Limit must be a valid integer!\n";
        }

        // Validate stockField (only integers)
        if (stockField.getText() == null || stockField.getText().isEmpty()) {
            errorMessage += "Stock is required!\n";
        } else if (!stockField.getText().matches("\\d*")) {
            errorMessage += "Stock must be a valid integer!\n";
        }

        // Validate imagePathField (optional, but can add checks if needed)
        // Validate soldField (only integers, optional)
        if (!soldField.getText().isEmpty() && !soldField.getText().matches("\\d*")) {
            errorMessage += "Sold must be a valid integer!\n";
        }

        // Validate descriptionField (optional)
        // Validate categoryComboBox (must be selected)
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            errorMessage += "Category is required!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Invalid Fields", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}