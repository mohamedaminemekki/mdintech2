package Controllers.mariem;

import entities.mariem.Trip;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import services.mariem.TripService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MetroMapController {

    @FXML private WebView webView;
    @FXML private AnchorPane mapContainer;

    // Injecté par MainController
    private TripService tripService;
    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }

    private Map<String, double[]> cityCoordinatesCache = new HashMap<>();

    @FXML
    public void initialize() {
        // fallback si non injecté
        if (tripService == null) {
            try {
                tripService = new TripService(utils.DatabaseConnection.getInstance().getConnection());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        configureWebView();
        setupSizeBindings();
        setupMapResizeHandler();
    }

    private void configureWebView() {
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().setOnError(evt ->
                System.err.println("JS Error: " + evt.getMessage())
        );
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldS, newS) -> {
            if (newS == Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    initializeJavaScriptBridge();
                    webView.getEngine().executeScript("initMap();");
                    loadAndDisplayMetroTrips();
                });
            }
        });
        String url = getClass().getResource("/views/metro_map.html").toExternalForm();
        webView.getEngine().load(url);
    }

    private void initializeJavaScriptBridge() {
        try {
            JSObject window = (JSObject) webView.getEngine().executeScript("window");
            window.setMember("javaBridge", new JavaBridge());
            // route console.log vers Java pour debug
            webView.getEngine().executeScript(
                    "console.log = function(msg) { window.javaBridge.log(msg); };"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSizeBindings() {
        webView.prefWidthProperty().bind(mapContainer.widthProperty());
        webView.prefHeightProperty().bind(mapContainer.heightProperty());
    }

    private void setupMapResizeHandler() {
        mapContainer.widthProperty().addListener((o,ov,nv) -> invalidateMap());
        mapContainer.heightProperty().addListener((o,ov,nv) -> invalidateMap());
    }

    private void invalidateMap() {
        Platform.runLater(() ->
                webView.getEngine().executeScript(
                        "if(map){ map.invalidateSize(true); }"
                )
        );
    }

    private void loadAndDisplayMetroTrips() {
        try {
            List<Trip> trips = tripService.readList();
            for (Trip t : trips) {
                if (!"metro".equalsIgnoreCase(t.getTransportName())) continue;

                String dep = t.getDeparture().trim();
                String dest = t.getDestination().trim();

                double[] c1 = getCityCoordinates(dep);
                double[] c2 = getCityCoordinates(dest);

                if (c1 != null && c2 != null) {
                    callAddLine(dep, c1[0], c1[1], dest, c2[0], c2[1]);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private double[] getCityCoordinates(String city) throws Exception {
        String key = city.toLowerCase();
        if (cityCoordinatesCache.containsKey(key)) {
            return cityCoordinatesCache.get(key);
        }

        String query = java.net.URLEncoder.encode(city + ", Tunisia", "UTF-8");
        URL url = new URL("https://nominatim.openstreetmap.org/search?q=" +
                query + "&format=json&limit=1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "JavaFX-MetroMapApp");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) sb.append(line);
        rd.close();

        JSONArray arr = new JSONArray(sb.toString());
        if (arr.length() > 0) {
            JSONObject o = arr.getJSONObject(0);
            double lat = o.getDouble("lat"), lon = o.getDouble("lon");
            cityCoordinatesCache.put(key, new double[]{lat, lon});
            return new double[]{lat, lon};
        }
        return null;
    }

    private void callAddLine(String dCity, double dLat, double dLng,
                             String rCity, double rLat, double rLng) {
        String script = String.format(Locale.US,
                "addMetroLine('%s', %f, %f, '%s', %f, %f);",
                dCity.replace("'", "\\'"), dLat, dLng,
                rCity.replace("'", "\\'"), rLat, rLng
        );
        System.out.println("JS> " + script);
        Platform.runLater(() ->
                webView.getEngine().executeScript(script)
        );
    }

    /** Pont JavaScript → Java pour debug */
    public class JavaBridge {
        public void log(String msg) {
            System.out.println("[JS] " + msg);
        }
        public void showCityTrips(String city) {
            // implémentation si besoin
        }
    }
}