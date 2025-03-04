package controllers.mariem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import services.mariem.ReservationService;
import services.mariem.TripService;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class TripStatsController {

    // Déclarations FXML
    @FXML private Label totalTripsLabel;
    @FXML private Label revenueLabel;
    @FXML private Label occupancyLabel;
    @FXML private LineChart<String, Number> tripsChart;
    @FXML private PieChart destinationsChart;
    @FXML private PieChart statusChart;
    @FXML private BarChart<String, Number> profitableTripsChart;
    @FXML private BarChart<String, Number> unprofitableTripsChart;
    @FXML private LineChart<String, Number> cancellationHeatMap;
    @FXML private PieChart destinationCancellationChart;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private TripService tripService;
    private ReservationService reservationService;

    public void initialize() {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            tripService = new TripService(connection);
            reservationService = new ReservationService(connection);

            loadGeneralStats();
            setupCharts();
            setupTimeFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGeneralStats() throws SQLException {
        totalTripsLabel.setText(String.valueOf(tripService.getTotalTrips()));
        revenueLabel.setText(String.format("%.2f DT", reservationService.getMonthlyRevenue()));
        occupancyLabel.setText(String.format("%.1f%%", reservationService.getAverageOccupancy()));
    }

    private void setupCharts() throws SQLException {
        setupTripsChart();
        setupDestinationsChart();
        setupStatusChart();
        setupProfitabilityCharts();
    }

    private void setupTripsChart() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Trajets par mois");
        Map<String, Integer> tripsData = tripService.getTripsPerMonth();
        tripsData.forEach((month, count) ->
                series.getData().add(new XYChart.Data<>(month, count)));
        tripsChart.getData().add(series);
    }

    private void setupDestinationsChart() throws SQLException {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        Map<String, Integer> destinationsData = tripService.getPopularDestinations(5);
        destinationsData.forEach((dest, count) ->
                pieData.add(new PieChart.Data(dest + " (" + count + ")", count)));
        destinationsChart.setData(pieData);
    }

    private void setupStatusChart() throws SQLException {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        Map<String, Integer> statusData = reservationService.getReservationStatusStats();
        statusData.forEach((status, count) ->
                pieData.add(new PieChart.Data(status + " (" + count + ")", count)));
        statusChart.setData(pieData);
    }

    private void setupProfitabilityCharts() throws SQLException {
        setupProfitableTripsChart();
        setupUnprofitableTripsChart();
    }

    private void setupProfitableTripsChart() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> data = reservationService.getMostProfitableTrips(5);
        data.forEach((trip, revenue) ->
                series.getData().add(new XYChart.Data<>(trip, revenue)));
        profitableTripsChart.getData().add(series);
    }

    private void setupUnprofitableTripsChart() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> data = reservationService.getLeastProfitableTrips(5);
        data.forEach((trip, revenue) ->
                series.getData().add(new XYChart.Data<>(trip, revenue)));
        unprofitableTripsChart.getData().add(series);
    }

    private void setupTimeFilters() {
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupCancellationAnalysis() throws SQLException {
        // Pour le LineChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Taux d'annulation");

        Map<String, Double> data = reservationService.getCancellationByHour(
                startDatePicker.getValue(),
                endDatePicker.getValue()
        );

        data.forEach((hour, rate) ->
                series.getData().add(new XYChart.Data<>(hour, rate)));

        cancellationHeatMap.getData().clear();
        cancellationHeatMap.getData().add(series);

        // Pour le PieChart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        Map<String, Integer> destinationData = reservationService.getCancellationByDestination();

        destinationData.forEach((dest, count) ->
                pieData.add(new PieChart.Data(dest + " (" + count + ")", count)));

        destinationCancellationChart.setData(pieData);
    }

    @FXML
    private void updateTimeAnalysis() {

    }

    private void showAlert(String erreur, String impossibleDeChargerLesDonnées) {
    }

    private void refreshCancellationData(LocalDate start, LocalDate end) throws SQLException {
        // Implémentation de la mise à jour des données
    }
}