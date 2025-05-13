package controllers.mariem;

import entities.mariem.Trip;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import services.mariem.TripService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrainMapController {
    @FXML private WebView webView;
    @FXML private AnchorPane mapContainer;

    private TripService tripService;
    private Map<String, double[]> stationCoordinatesCache = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final int GEOCODING_DELAY = 500;

    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    @FXML
    public void initialize() {
        if (tripService == null) {
            try {
                tripService = new TripService(utils.DatabaseConnection.getInstance().getConnection());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        configureWebView();
        setupSizeBindings();
    }

    private void configureWebView() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(true);

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldS, newS) -> {
            if (newS == Worker.State.SUCCEEDED) {
                initializeJavaScriptBridge();
                loadAndDisplayTrainRoutes();
            }
        });

        webView.getEngine().load(getClass().getResource("/views/train_map.html").toExternalForm());
    }

    private void initializeJavaScriptBridge() {
        JSObject window = (JSObject) webView.getEngine().executeScript("window");
        window.setMember("javaBridge", new JavaBridge());
    }

    private void setupSizeBindings() {
        webView.prefWidthProperty().bind(mapContainer.widthProperty());
        webView.prefHeightProperty().bind(mapContainer.heightProperty());
        mapContainer.widthProperty().addListener((o, ov, nv) -> invalidateMap());
        mapContainer.heightProperty().addListener((o, ov, nv) -> invalidateMap());
    }

    private void invalidateMap() {
        Platform.runLater(() ->
                webView.getEngine().executeScript("if(window.map) window.map.invalidateSize(true);"));
    }

    private void loadAndDisplayTrainRoutes() {
        executor.execute(() -> {
            try {
                List<Trip> trips = tripService.readList();
                int delay = 0;

                for (Trip trip : trips) {
                    if (!"train".equalsIgnoreCase(trip.getTransportName())) continue;

                    final Trip currentTrip = trip;
                    int finalDelay = delay;
                    Platform.runLater(() -> {
                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.millis(finalDelay),
                                        e -> processTrainRoute(currentTrip))
                        );
                        timeline.play();
                    });
                    delay += GEOCODING_DELAY;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void processTrainRoute(Trip trip) {
        try {
            String departure = trip.getDeparture().trim();
            String destination = trip.getDestination().trim();

            double[] depCoords = getStationCoordinates(departure);
            double[] destCoords = getStationCoordinates(destination);

            if (depCoords != null && destCoords != null) {
                addTrainRouteToMap(departure, depCoords[0], depCoords[1],
                        destination, destCoords[0], destCoords[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double[] getStationCoordinates(String station) throws Exception {
        String key = station.toLowerCase();
        if (!stationCoordinatesCache.containsKey(key)) {
            stationCoordinatesCache.put(key, fetchStationCoordinates(station));
        }
        return stationCoordinatesCache.get(key);
    }

    private double[] fetchStationCoordinates(String station) throws Exception {
        String query = URLEncoder.encode(station + " station, Tunisia", "UTF-8");
        URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=1");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "JavaFX-TrainMapApp");

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            JSONArray arr = new JSONArray(sb.toString());
            if (arr.length() > 0) {
                JSONObject o = arr.getJSONObject(0);
                return new double[]{o.getDouble("lat"), o.getDouble("lon")};
            }
        }
        return null;
    }

    private void addTrainRouteToMap(String departure, double depLat, double depLng,
                                    String destination, double destLat, double destLng) {
        String script = String.format(Locale.US,
                "addTrainRoute('%s', %f, %f, '%s', %f, %f);",
                departure.replace("'", "\\'"), depLat, depLng,
                destination.replace("'", "\\'"), destLat, destLng
        );
        Platform.runLater(() -> webView.getEngine().executeScript(script));
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public WebView getWebView() {
        return webView;
    }

    public class JavaBridge {
        public void log(String msg) {
            System.out.println("[JS] " + msg);
        }
    }
}
