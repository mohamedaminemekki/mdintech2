package services.mariem;

import entities.mariem.Trip;
import org.json.JSONArray;
import org.json.JSONObject;
import services.Services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class TripService implements Services<Trip> {

    private Connection connection;

    public TripService(Connection connection) {
        this.connection = connection;
    }

    public TripService() {

    }

    // Vérifie si un Transport ID existe dans la table transport_types
    public boolean transportTypeExists(int transportId) throws SQLException {
        String query = "SELECT COUNT(*) FROM transport_types WHERE transport_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, transportId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }

    // Affiche les types de transport disponibles
    public void displayTransportTypes() throws SQLException {
        String query = "SELECT transport_id, name FROM transport_types";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            System.out.println("=== Types de transport disponibles ===");
            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("transport_id") + ", Nom: " + resultSet.getString("name"));
            }
        }
    }

    @Override
    public List<Trip> readList() throws SQLException {
        List<Trip> trips = new ArrayList<>();
        String query = "SELECT t.id, t.transport_id, t.departure_time, t.arrival_time, t.price, t.departure, t.destination, " +
                "tt.name AS transport_name, t.date " +
                "FROM trips t " +
                "JOIN transport_types tt ON t.transport_id = tt.transport_id";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("date") != null ? resultSet.getDate("date").toLocalDate() : LocalDate.now();
                Trip trip = new Trip(
                        resultSet.getInt("id"),
                        resultSet.getInt("transport_id"),
                        resultSet.getTimestamp("departure_time"),
                        resultSet.getTimestamp("arrival_time"),
                        resultSet.getDouble("price"),
                        resultSet.getString("departure"),
                        resultSet.getString("destination"),
                        resultSet.getString("transport_name")
                );
                trips.add(trip);

                // Log pour vérifier les données récupérées
                System.out.println("Voyage récupéré : Départ = " + trip.getDeparture() + ", Destination = " + trip.getDestination());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des voyages : " + e.getMessage());
            throw e;
        }

        return trips;
    }

    @Override
    public void add(Trip trip) throws SQLException {
        // Vérifier si le Transport ID existe
        if (!transportTypeExists(trip.getTransportId())) {
            throw new SQLException("Le Transport ID " + trip.getTransportId() + " n'existe pas dans la table transport_types.");
        }

        String query = "INSERT INTO trips (transport_id, departure_time, arrival_time, price, departure, destination, transport_name, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, trip.getTransportId());
            statement.setTimestamp(2, trip.getDepartureTime());
            statement.setTimestamp(3, trip.getArrivalTime());
            statement.setDouble(4, trip.getPrice());
            statement.setString(5, trip.getDeparture());
            statement.setString(6, trip.getDestination());
            statement.setString(7, trip.getTransportName());
            statement.setDate(8, Date.valueOf(trip.getDate())); // Ajout de la date

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Erreur lors de l'ajout du voyage");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    trip.setTripId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du voyage : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Trip trip) throws SQLException {
        // Vérifier si le Transport ID existe
        if (!transportTypeExists(trip.getTransportId())) {
            throw new SQLException("Le Transport ID " + trip.getTransportId() + " n'existe pas dans la table transport_types.");
        }

        String query = "UPDATE trips SET transport_id = ?, departure_time = ?, arrival_time = ?, price = ?, departure = ?, destination = ?, transport_name = ?, date = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, trip.getTransportId());
            statement.setTimestamp(2, trip.getDepartureTime());
            statement.setTimestamp(3, trip.getArrivalTime());
            statement.setDouble(4, trip.getPrice());
            statement.setString(5, trip.getDeparture());
            statement.setString(6, trip.getDestination());
            statement.setString(7, trip.getTransportName());
            statement.setDate(8, Date.valueOf(trip.getDate())); // Ajout de la date
            statement.setInt(9, trip.getTripId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Aucun voyage trouvé avec cet ID pour la mise à jour");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du voyage : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addP(Trip trip) throws SQLException {
        // Cette méthode semble non utilisée, elle peut être supprimée ou implémentée
    }

    @Override
    public void delete(int tripId) throws SQLException {
        String query = "DELETE FROM trips WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, tripId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Aucun voyage trouvé avec cet ID pour la suppression");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du voyage : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Trip getById(int id) throws SQLException {
        String query = "SELECT t.id, t.transport_id, t.departure_time, t.arrival_time, t.price, t.departure, t.destination, " +
                "tt.name AS transport_name, t.date " + // Ajout du champ date
                "FROM trips t " +
                "JOIN transport_types tt ON t.transport_id = tt.transport_id " +
                "WHERE t.id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalDate date = resultSet.getDate("date") != null ? resultSet.getDate("date").toLocalDate() : LocalDate.now(); // Récupération de la date
                return new Trip(
                        resultSet.getInt("id"),
                        resultSet.getInt("transport_id"),
                        resultSet.getTimestamp("departure_time"),
                        resultSet.getTimestamp("arrival_time"),
                        resultSet.getDouble("price"),
                        resultSet.getString("departure"),
                        resultSet.getString("destination"),
                        resultSet.getString("transport_name")
                );
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du voyage par ID : " + e.getMessage());
            throw e;
        }
    }

    // Recherche générale (par mot-clé)
    public List<Trip> searchTrips(String keyword) throws SQLException {
        String query = "SELECT t.id, t.transport_id, t.departure_time, t.arrival_time, t.price, t.departure, t.destination, " +
                "tt.name AS transport_name, t.date " +
                "FROM trips t " +
                "JOIN transport_types tt ON t.transport_id = tt.transport_id " +
                "WHERE t.departure LIKE ? OR t.destination LIKE ? OR tt.name LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + keyword + "%");
            statement.setString(2, "%" + keyword + "%");
            statement.setString(3, "%" + keyword + "%");
            return executeQuery(statement);
        }
    }

    // Recherche par lieu de départ
    public List<Trip> searchByDeparture(String keyword) throws SQLException {
        String query = "SELECT t.id, t.transport_id, t.departure_time, t.arrival_time, t.price, t.departure, t.destination, " +
                "tt.name AS transport_name, t.date " +
                "FROM trips t " +
                "JOIN transport_types tt ON t.transport_id = tt.transport_id " +
                "WHERE t.departure LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + keyword + "%");
            return executeQuery(statement);
        }
    }

    // Recherche par lieu de destination
    public List<Trip> searchByDestination(String keyword) throws SQLException {
        String query = "SELECT t.id, t.transport_id, t.departure_time, t.arrival_time, t.price, t.departure, t.destination, " +
                "tt.name AS transport_name, t.date " +
                "FROM trips t " +
                "JOIN transport_types tt ON t.transport_id = tt.transport_id " +
                "WHERE t.destination LIKE ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + keyword + "%");
            return executeQuery(statement);
        }
    }

    // Recherche par Transport ID
    public List<Trip> searchByTransportId(String keyword) throws SQLException {
        String query = "SELECT t.id, t.transport_id, t.departure_time, t.arrival_time, t.price, t.departure, t.destination, " +
                "tt.name AS transport_name, t.date " +
                "FROM trips t " +
                "JOIN transport_types tt ON t.transport_id = tt.transport_id " +
                "WHERE t.transport_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, Integer.parseInt(keyword));
            return executeQuery(statement);
        }
    }

    // Méthode utilitaire pour exécuter une requête et retourner une liste de trajets
    private List<Trip> executeQuery(PreparedStatement statement) throws SQLException {
        List<Trip> trips = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("date") != null ? resultSet.getDate("date").toLocalDate() : LocalDate.now();
                Trip trip = new Trip(
                        resultSet.getInt("id"),
                        resultSet.getInt("transport_id"),
                        resultSet.getTimestamp("departure_time"),
                        resultSet.getTimestamp("arrival_time"),
                        resultSet.getDouble("price"),
                        resultSet.getString("departure"),
                        resultSet.getString("destination"),
                        resultSet.getString("transport_name")
                );
                trips.add(trip);
            }
        }
        return trips;
    }
    public String getTransportType(int tripId) {
        String query = "SELECT transport_name FROM trips WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("transport_name") : "unknown";
        } catch (SQLException e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    public LocalDate getTripDate(int tripId) {
        String query = "SELECT date FROM trips WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDate("date").toLocalDate() : LocalDate.now();
        } catch (SQLException e) {
            e.printStackTrace();
            return LocalDate.now();
        }
    }

    public double getTripDistance(int tripId) throws SQLException {
        String query = "SELECT distance FROM trips WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, tripId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble("distance") : 0;
        }
    }
    public int getTripHour(int tripId) throws SQLException {
        String query = "SELECT HOUR(departure_time) as hour FROM trips WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, tripId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("hour") : -1;
        }
    }
    public int getTripIdByDate(LocalDate date) throws SQLException {
        String query = "SELECT id FROM trips WHERE date = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        }
    }
    // Dans TripService.java

    public int getTotalTrips() throws SQLException {
        String query = "SELECT COUNT(*) FROM trips";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Map<String, Integer> getTripsPerMonth() throws SQLException {
        Map<String, Integer> monthlyStats = new LinkedHashMap<>();
        String query = "SELECT DATE_FORMAT(departure_time, '%Y-%m') as month, COUNT(*) " +
                "FROM trips GROUP BY month ORDER BY month";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                monthlyStats.put(rs.getString(1), rs.getInt(2));
            }
        }
        return monthlyStats;
    }

    public Map<String, Integer> getPopularDestinations(int limit) throws SQLException {
        Map<String, Integer> destinations = new LinkedHashMap<>();
        String query = "SELECT destination, COUNT(*) as count " +
                "FROM trips GROUP BY destination ORDER BY count DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                destinations.put(rs.getString("destination"), rs.getInt("count"));
            }
        }
        return destinations;
    }

    public List<Trip> getTripsByTransportName(String transportName) {
        // Implémentez cette méthode pour récupérer les voyages depuis une base de données
        // Par exemple, en utilisant JDBC ou un ORM comme Hibernate
        return List.of(
                new Trip(1, 1, Timestamp.valueOf("2023-10-01 08:00:00"), Timestamp.valueOf("2023-10-01 09:00:00"), 10.0, "Tunis", "Ariana", "metro"),
                new Trip(2, 1, Timestamp.valueOf("2023-10-01 09:00:00"), Timestamp.valueOf("2023-10-01 10:00:00"), 10.0, "Ariana", "Manouba", "metro")
        );
    }
    private final Map<String, double[]> geoCache = new HashMap<>();

    /**
     * Récupère les coordonnées [lat, lon] pour une ville via Nominatim.
     * @param city Nom de la ville (ex. "Tunis")
     * @return double[]{latitude, longitude} ou null si non trouvé
     */
    public double[] getCityCoordinates(String city) {
        String key = city.trim().toLowerCase();
        if (geoCache.containsKey(key)) {
            return geoCache.get(key);
        }

        try {
            String query = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url   = "https://nominatim.openstreetmap.org/search"
                    + "?format=json&limit=1&q=" + query;

            HttpRequest req  = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "JavaFX-App/1.0") // Nominatim exige un UA
                    .GET()
                    .build();

            HttpClient http = HttpClient.newHttpClient();
            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JSONArray arr = new JSONArray(resp.body());
                if (arr.length() > 0) {
                    JSONObject o = arr.getJSONObject(0);
                    double lat = o.getDouble("lat");
                    double lon = o.getDouble("lon");
                    double[] coords = { lat, lon };
                    geoCache.put(key, coords);
                    return coords;
                }
            } else {
                System.err.println("Géo-codage échoué (" + resp.statusCode() + ")");
            }
        } catch (Exception e) {
            System.err.println("Erreur géocodage pour \"" + city + "\": " + e.getMessage());
        }

        // si pas trouvé, on stocke un null pour ne pas retenter à chaque fois
        geoCache.put(key, null);
        return null;
    }

    public List<Trip> getTripsByCity(String cityName) throws SQLException {
        String query = "SELECT * FROM trips WHERE departure = ? OR destination = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, cityName);
            statement.setString(2, cityName);
            return executeQuery(statement);
        }
    }
    // Mapper un ResultSet à un objet Trip
    private Trip mapResultSetToTrip(ResultSet rs) throws SQLException {
        Trip trip = new Trip();
        trip.setId(rs.getInt("id"));
        trip.setDeparture(rs.getString("departure"));
        trip.setDestination(rs.getString("destination"));
        trip.setTransportName(rs.getString("transport_name"));
        // Ajoutez d'autres champs selon votre structure de base de données

        return trip;
    }
}