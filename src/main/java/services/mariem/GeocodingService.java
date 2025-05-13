package services.mariem;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GeocodingService {
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search?format=json&q=";
    private final Map<String, double[]> cache = new HashMap<>();
    private long lastRequestTime = 0;

    public double[] getCoordinates(String city) throws IOException, InterruptedException {
        String normalizedCity = city.toLowerCase().trim();

        // Vérifier le cache
        if (cache.containsKey(normalizedCity)) {
            return cache.get(normalizedCity);
        }

        // Respecter le taux de requêtes (1 req/s)
        long delay = System.currentTimeMillis() - lastRequestTime;
        if (delay < 1000) {
            Thread.sleep(1000 - delay);
        }

        HttpClient client = HttpClient.newHttpClient();
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(NOMINATIM_API + encodedCity))
                .header("User-Agent", "TransportApp/1.0")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        lastRequestTime = System.currentTimeMillis();

        if (response.statusCode() == 200) {
            JsonArray results = JsonParser.parseString(response.body()).getAsJsonArray();
            if (!results.isEmpty()) {
                JsonObject firstResult = results.get(0).getAsJsonObject();
                double lat = firstResult.get("lat").getAsDouble();
                double lon = firstResult.get("lon").getAsDouble();
                double[] coords = {lat, lon};
                cache.put(normalizedCity, coords);
                return coords;
            }
        }
        return null;
    }
}