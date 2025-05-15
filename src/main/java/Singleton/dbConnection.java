package Singleton;
import entities.mariem.Ville;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static utils.UserRole.USER;
import static utils.VilleBase.PASS;

public class dbConnection {
    private static dbConnection instance;
    private Connection connection;
    private final String dbPort = "3306";
    private final String host = "localhost";
    private final String dbName = "pidev_symfony";
    private final String url = "jdbc:mysql://" + host + ":" + dbPort + "/" + dbName +
            "?autoReconnect=true&useSSL=false"; // Added auto-reconnect
    private final String user = "root";
    private final String password = "";

    private dbConnection() {} // Private constructor

    public static synchronized dbConnection getInstance() {
        if (instance == null) {
            instance = new dbConnection();
        }
        return instance;
    }

    // Get a valid connection (reconnects if closed)
    public Connection getConn() {
        try {
            // Reconnect if the connection is closed or invalid
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                reconnect();
            }
        } catch (SQLException e) {
            System.err.println("Connection validation failed: " + e.getMessage());
            reconnect();
        }
        return connection;
    }

    // Reconnect explicitly
    private void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Reconnected to database!");
        } catch (SQLException e) {
            System.err.println("Reconnection failed: " + e.getMessage());
        }
    }
    public static List<Ville> loadCities() {
        List<Ville> villes = new ArrayList<>();

        // Récupère la connexion via votre singleton
        try (Connection conn = dbConnection.getInstance().getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM villes")) {

            while (rs.next()) {
                Ville ville = new Ville(
                        rs.getString("nom"),
                        rs.getString("histoire"),
                        rs.getString("anecdotes"),
                        rs.getString("activites"),
                        rs.getString("gastronomie"),
                        rs.getString("nature"),
                        rs.getString("histoire_interactive")
                );
                villes.add(ville);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return villes;
    }

}