package services.tasnim;

import entities.tasnim.User;
import utils.db;

import entities.tasnim.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    public User getUserById(int userId) {
        String query = "SELECT * FROM users WHERE CIN = ?"; // Adjust query as needed
        User user = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = db.getCon();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setCIN(rs.getInt("CIN"));
                user.setName(rs.getString("Name"));
                user.setEmail(rs.getString("Email"));
                // Set other fields as needed
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }

        return user;
    }

    // Method to get user role by CIN
    public UserRole getUserRole(int CIN) {
        String query = "SELECT Role FROM users WHERE CIN = ?"; // Adjust table and column names as needed
        UserRole role = UserRole.Citizen; // Default role if user not found

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Get the database connection instance
            con = db.getCon();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, CIN); // Set the CIN parameter
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Fetch the role from the database and convert it to UserRole enum
                String roleStr = rs.getString("Role");
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
}