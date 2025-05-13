package services;

import entities.mariem.Badge;
import utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BadgeLoader {
    private final Connection connection;

    public BadgeLoader() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public Badge loadBadgeById(String badgeId) throws SQLException {
        String query = "SELECT id, description, image_path FROM badges WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, badgeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Badge(
                        rs.getString("id"),
                        rs.getString("description"),
                        rs.getString("image_path")
                );
            } else {
                throw new SQLException("Badge non trouvé : " + badgeId);
            }
        }
    }

}