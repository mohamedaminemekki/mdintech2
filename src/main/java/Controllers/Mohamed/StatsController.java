package Controllers.Mohamed;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Mohamed.Reclamation;
import services.Mohamed.ReclamationServices;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;

public class StatsController {

    @FXML
    private PieChart reclamationPieChart;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private DatePicker datePicker; // For selecting a specific day

    @FXML
    private ComboBox<String> monthComboBox; // For selecting a specific month

    @FXML
    private Label totalReclamationsLabel;

    @FXML
    private Label highestReclamationLabel;

    @FXML
    private Button exportPdfButton;

    @FXML
    private VBox statsPage; // Root container for the entire page

    private final ReclamationServices reclamationService = new ReclamationServices();

    @FXML
    private void initialize() {
        // Set up filter options
        filterComboBox.getItems().addAll("Day", "Month", "Year");
        filterComboBox.setValue("Day"); // Default filter

        // Set up month options
        monthComboBox.getItems().addAll(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
        monthComboBox.setValue("January"); // Default month

        // Set up date picker
        datePicker.setValue(LocalDate.now()); // Default to today's date

        // Load initial data
        loadChartData();

        // Update chart when filter, date, or month changes
        filterComboBox.setOnAction(event -> loadChartData());
        datePicker.setOnAction(event -> loadChartData());
        monthComboBox.setOnAction(event -> loadChartData());

        // Export to PDF button action
        exportPdfButton.setOnAction(event -> exportToPdf());
    }

    private void loadChartData() {
        try {
            // Fetch all reclamations
            List<Reclamation> reclamations = reclamationService.readList();

            // Group reclamations by type and filter
            Map<String, Integer> reclamationCounts = new HashMap<>();
            String filter = filterComboBox.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Reclamation rec : reclamations) {
                LocalDate reclamationDate = LocalDate.parse(rec.getDatee(), formatter);

                switch (filter) {
                    case "Day":
                        LocalDate selectedDate = datePicker.getValue();
                        if (reclamationDate.isEqual(selectedDate)) {
                            reclamationCounts.put(rec.getType(), reclamationCounts.getOrDefault(rec.getType(), 0) + 1);
                        }
                        break;
                    case "Month":
                        String selectedMonth = monthComboBox.getValue();
                        int selectedMonthNumber = getMonthNumber(selectedMonth);
                        if (reclamationDate.getMonthValue() == selectedMonthNumber && reclamationDate.getYear() == LocalDate.now().getYear()) {
                            reclamationCounts.put(rec.getType(), reclamationCounts.getOrDefault(rec.getType(), 0) + 1);
                        }
                        break;
                    case "Year":
                        if (reclamationDate.getYear() == LocalDate.now().getYear()) {
                            reclamationCounts.put(rec.getType(), reclamationCounts.getOrDefault(rec.getType(), 0) + 1);
                        }
                        break;
                }
            }

            // Update PieChart
            updatePieChart(reclamationCounts);

            // Update Total Reclamations and Highest Reclamation Type
            updateStats(reclamationCounts);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getMonthNumber(String monthName) {
        return switch (monthName) {
            case "January" -> 1;
            case "February" -> 2;
            case "March" -> 3;
            case "April" -> 4;
            case "May" -> 5;
            case "June" -> 6;
            case "July" -> 7;
            case "August" -> 8;
            case "September" -> 9;
            case "October" -> 10;
            case "November" -> 11;
            case "December" -> 12;
            default -> 0;
        };
    }

    private void updatePieChart(Map<String, Integer> reclamationCounts) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        int totalReclamations = reclamationCounts.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : reclamationCounts.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalReclamations;
            PieChart.Data data = new PieChart.Data(entry.getKey() + " (" + String.format("%.1f%%", percentage) + ")", entry.getValue());
            pieChartData.add(data);
        }

        // Update PieChart
        reclamationPieChart.setData(pieChartData);
    }

    private void updateStats(Map<String, Integer> reclamationCounts) {
        int totalReclamations = reclamationCounts.values().stream().mapToInt(Integer::intValue).sum();
        totalReclamationsLabel.setText("" + totalReclamations);

        // Find the highest reclamation type
        String highestType = "";
        int highestCount = 0;
        for (Map.Entry<String, Integer> entry : reclamationCounts.entrySet()) {
            if (entry.getValue() > highestCount) {
                highestType = entry.getKey();
                highestCount = entry.getValue();
            }
        }

        highestReclamationLabel.setText("" + highestType + " (" + highestCount + ")");
    }

    private void exportToPdf() {
        try {
            // Create a file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                Platform.runLater(() -> {
                    try {
                        // Capture the entire scene as an image
                        WritableImage snapshot = statsPage.snapshot(new SnapshotParameters(), null);

                        if (snapshot == null) {
                            showError("Failed to capture snapshot.");
                            return;
                        }

                        // Create a PDF document
                        Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(file));
                        document.open();

                        // Convert the snapshot to an image
                        byte[] imageData = snapshotToByteArray(snapshot);
                        if (imageData == null) {
                            showError("Failed to convert image.");
                            document.close();
                            return;
                        }

                        Image pdfImage = Image.getInstance(imageData);
                        pdfImage.scaleToFit(document.getPageSize().getWidth() - 50, document.getPageSize().getHeight() - 50);
                        pdfImage.setAlignment(Image.ALIGN_CENTER);

                        // Add the image to the PDF
                        document.add(pdfImage);

                        // Close the document
                        document.close();

                        // Show success message
                        showSuccess("PDF exported successfully!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Failed to export PDF: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred while exporting the PDF.");
        }
    }

    private byte[] snapshotToByteArray(WritableImage snapshot) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void backpage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Mohamed/AfficherReclamation.fxml")); // Replace with the correct FXML file
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu Principal");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}