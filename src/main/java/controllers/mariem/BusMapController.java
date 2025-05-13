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

public class BusMapController {
    @FXML private WebView webView;
    @FXML private AnchorPane mapContainer;

    private TripService tripService;
    private Map<String, double[]> cityCoordinatesCache = new HashMap<>();
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
                loadAndDisplayBusTrips();
            }
        });

        webView.getEngine().load(getClass().getResource("/views/bus_map.html").toExternalForm());
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

    void invalidateMap() {
        Platform.runLater(() ->
                webView.getEngine().executeScript("if(map) map.invalidateSize(true);")
        );
    }

    private void loadAndDisplayBusTrips() {
        executor.execute(() -> {
            try {
                List<Trip> trips = tripService.readList();
                int delay = 0;

                for (Trip t : trips) {
                    if (!"bus".equalsIgnoreCase(t.getTransportName())) continue;

                    final Trip trip = t;
                    int finalDelay = delay;
                    Platform.runLater(() -> {
                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.millis(finalDelay), e -> processTrip(trip))
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

    private void processTrip(Trip trip) {
        try {
            String dep = trip.getDeparture().trim();
            String dest = trip.getDestination().trim();

            double[] depCoords = getCachedCoordinates(dep);
            double[] destCoords = getCachedCoordinates(dest);

            if (depCoords != null && destCoords != null) {
                callAddBusLine(dep, depCoords[0], depCoords[1],
                        dest, destCoords[0], destCoords[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double[] getCachedCoordinates(String city) throws Exception {
        String key = city.toLowerCase();
        if (!cityCoordinatesCache.containsKey(key)) {
            cityCoordinatesCache.put(key, fetchCoordinates(city));
        }
        return cityCoordinatesCache.get(key);
    }

    private double[] fetchCoordinates(String city) throws Exception {
        String query = URLEncoder.encode(city + ", Tunisia", "UTF-8");
        URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=1");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "JavaFX-BusMapApp");

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

    private void callAddBusLine(String dCity, double dLat, double dLng,
                                String rCity, double rLat, double rLng) {
        String script = String.format(Locale.US,
                "addBusLine('%s', %f, %f, '%s', %f, %f);",
                dCity.replace("'", "\\'"), dLat, dLng,
                rCity.replace("'", "\\'"), rLat, rLng
        );
        Platform.runLater(() -> webView.getEngine().executeScript(script));
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public class JavaBridge {
        public void log(String msg) {
            System.out.println("[JS] " + msg);
        }
    }
}
