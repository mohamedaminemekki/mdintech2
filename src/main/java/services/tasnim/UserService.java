package services.tasnim;

import entities.amine.User;
import utils.db;

import utils.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    public User getUserById(int userId) {
        String query = "SELECT * FROM user WHERE id = ?";
        entities.amine.User user = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = db.getCon();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new entities.amine.User();
                user.setCIN(rs.getString("cin"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                // Set other fields as needed
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }

        return user;
    }

    // Method to get user role by CIN
    public UserRole getUserRole(int userId) {
        String query = "SELECT roles FROM user WHERE id = ?";
        UserRole role = UserRole.USER; // Default role if user not found

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = db.getCon();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String roleStr = rs.getString("roles");
                try {
                    role = UserRole.valueOf(roleStr); // Convert the role string to UserRole enum
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid role value in database: " + roleStr);
                    // Default role (Citizen) will be returned
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user role: " + e.getMessage());
        }
        return role;
    }

    public entities.amine.User getUserByCIN(String cin) {
        String query = "SELECT * FROM user WHERE cin=?";
        entities.amine.User user = null;
        try (Connection con = db.getCon();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Defensive: Ensure no nulls for required fields
                int id = rs.getInt("id");
                String dbCin = rs.getString("cin");
                String name = rs.getString("name");
                if (name == null || name.trim().isEmpty()) name = "Unnamed User";
                String email = rs.getString("email");
                if (email == null) email = "";
                user = new entities.amine.User(
                        id,
                        dbCin,
                        name,
                        email
                        // ...other fields as needed
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}
