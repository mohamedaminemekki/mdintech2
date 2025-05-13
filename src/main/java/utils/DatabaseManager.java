package utils;

import entities.mariem.Ville;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/city_transport";
    private static final String USER = "root";
    private static final String PASS = "";

    public static List<Ville> loadCities() {
        List<Ville> villes = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM villes");

            while(rs.next()) {
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