package Controllers.mariem;

import entities.mariem.Trip;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.scene.layout.AnchorPane;
import javafx.concurrent.Worker;
import services.mariem.TripService;
import java.util.*;
import java.sql.SQLException;

public class MetroMapController {


    @FXML
    private WebView webView;
    @FXML
    private AnchorPane mapContainer;

    private TripService tripService;

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    @FXML
    public void initialize() {
        setupWebView();
        setupMapSizeBinding();
    }

    private void setupWebView() {
        String mapUrl = getClass().getResource("/views/train_map.html").toExternalForm();
        webView.getEngine().load(mapUrl);

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadAndDisplayMetroTrips();
            }
        });
    }

    private void setupMapSizeBinding() {
        webView.prefWidthProperty().bind(mapContainer.widthProperty());
        webView.prefHeightProperty().bind(mapContainer.heightProperty());
    }

    private void loadAndDisplayMetroTrips() {
        try {
            List<Trip> allTrips = tripService.readList();
            Set<String> metroCities = extractMetroCities(allTrips);
            addCitiesToMap(metroCities);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Set<String> extractMetroCities(List<Trip> trips) {
        Set<String> cities = new HashSet<>();
        for (Trip trip : trips) {
            if ("metro".equalsIgnoreCase(trip.getTransportName())) {
                cities.add(trip.getDeparture());
                cities.add(trip.getDestination());
            }
        }
        return cities;
    }

    private void addCitiesToMap(Set<String> cities) {
        cities.forEach(city -> {
            try {
                double[] coords = tripService.getCityCoordinates(city);
                if (coords != null) {
                    addMarkerToMap(city, coords[0], coords[1]);
                }
            } catch (SQLException e) {
                System.err.println("Erreur de coordonnées pour " + city + ": " + e.getMessage());
            }
        });
    }

    private void addMarkerToMap(String city, double lat, double lng) {
        String script = String.format(
                "addMetroMarker('%s', %f, %f);",
                city.replace("'", "\\'"),
                lat,
                lng
        );
        webView.getEngine().executeScript(script);
    }

    // Classe pour la communication Java-JavaScript
    public class JavaBridge {
        public void showCityTrips(String cityName) {
            try {
                List<Trip> departures = tripService.searchByDeparture(cityName);
                List<Trip> arrivals = tripService.searchByDestination(cityName);
                displayTrips(cityName, departures, arrivals);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayTrips(String cityName, List<Trip> departures, List<Trip> arrivals) {
        // Implémentez l'affichage dans votre UI
        System.out.println("Départs de " + cityName + ": " + departures);
        System.out.println("Arrivées à " + cityName + ": " + arrivals);
    }
}