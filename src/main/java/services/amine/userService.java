package services.amine;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.amine.User;
import Singleton.dbConnection;
import utils.amine.PasswordVerification;
import utils.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class userService implements Iservice<User> {

    private final Connection conn;

    public userService() {
        this.conn = dbConnection.getInstance().getConn();
    }

    @Override
    public boolean save(User obj) throws JsonProcessingException {
        if (!PasswordVerification.isStrongPassword(obj.getPassword())) {
            throw new IllegalArgumentException("Password is not strong enough.");
        }
        ObjectMapper objectMapper = new ObjectMapper();

        String rolesJson = objectMapper.writeValueAsString(List.of("ROLE_USER"));
        String hashedPassword = PasswordVerification.hashPassword(obj.getPassword());

        String query = "INSERT INTO user (CIN, Name, Email, Password, roles, Phone, Address, is_active, pathtopic, birthday, is_verified, account_creation_date, last_login_date, failed_login_attempts, bio, created_at, updated_at, google_id, avatar, google_authenticator_secret, is_google_authenticator_enabled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, obj.getCIN());
            stmt.setString(2, obj.getName());
            stmt.setString(3, obj.getEmail());
            stmt.setString(4, hashedPassword);
            stmt.setString(5, rolesJson); // Store roles as CSV
            stmt.setString(6, obj.getPhone());
            stmt.setString(7, obj.getAddress());
            stmt.setBoolean(8, obj.isActive());
            stmt.setString(9, obj.getPathtopic());
            stmt.setDate(10, new java.sql.Date(obj.getBirthday().getTime()));
            stmt.setBoolean(11, obj.isVerified());
            stmt.setDate(12, new java.sql.Date(obj.getAccountCreationDate().getTime()));
            stmt.setDate(13, new java.sql.Date(obj.getLastLoginDate().getTime()));
            stmt.setInt(14, obj.getFailedLoginAttempts());
            stmt.setString(15, obj.getBio());
            stmt.setDate(16, new java.sql.Date(obj.getCreatedAt().getTime()));
            stmt.setDate(17, new java.sql.Date(obj.getCreatedAt().getTime()));
            stmt.setString(18, obj.getGoogleId());
            stmt.setString(19, obj.getAvatar());
            stmt.setString(20, obj.getGoogleAuthenticatorSecret());
            stmt.setBoolean(21, obj.isGoogleAuthenticatorEnabled());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    public boolean saveGoogle(User obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String hashedPassword = PasswordVerification.hashPassword(obj.getPassword());
        String rolesJson = objectMapper.writeValueAsString(List.of("ROLE_USER"));


        String query = "INSERT INTO user (CIN, Name, Email, Password,roles, Phone, Address, is_active, pathtopic, birthday, is_verified, account_creation_date, last_login_date, failed_login_attempts, bio, created_at, updated_at, google_id, avatar, google_authenticator_secret, is_google_authenticator_enabled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, obj.getCIN());
            stmt.setString(2, obj.getName());
            stmt.setString(3, obj.getEmail());
            stmt.setString(4, hashedPassword);
            stmt.setString(5, rolesJson); // Store roles as CSV
            stmt.setString(6, obj.getPhone());
            stmt.setString(7, obj.getAddress());
            stmt.setBoolean(8, obj.isActive());
            stmt.setString(9, obj.getPathtopic());
            stmt.setDate(10, new java.sql.Date(obj.getBirthday().getTime()));
            stmt.setBoolean(11, obj.isVerified());
            stmt.setDate(12, new java.sql.Date(obj.getAccountCreationDate().getTime()));
            stmt.setDate(13, new java.sql.Date(obj.getLastLoginDate().getTime()));
            stmt.setInt(14, obj.getFailedLoginAttempts());
            stmt.setString(15, obj.getBio());
            stmt.setDate(16, new java.sql.Date(obj.getCreatedAt().getTime()));
            stmt.setDate(17, new java.sql.Date(obj.getUpdatedAt().getTime()));
            stmt.setString(18, obj.getGoogleId());
            stmt.setString(19, obj.getAvatar());
            stmt.setString(20, obj.getGoogleAuthenticatorSecret());
            stmt.setBoolean(21, obj.isGoogleAuthenticatorEnabled());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }


    @Override
    public void update(User obj) {
        String hashedPassword = PasswordVerification.hashPassword(obj.getPassword());

        String query = "UPDATE user SET Name=?, Email=?,Password=?, roles=?, Phone=?, Address=?, is_active=?, pathtopic=?, birthday=?, is_verified=?, account_creation_date=?, last_login_date=?, failed_login_attempts=?, bio=?,created_at=?, updated_at=?, google_id=?, avatar=?, google_authenticator_secret=?, is_google_authenticator_enabled=? WHERE id=?";

        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, obj.getName());
            stmt.setString(2, obj.getEmail());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, String.join(",", obj.getRoles()));
            stmt.setString(5, obj.getPhone());
            stmt.setString(6, obj.getAddress());
            stmt.setBoolean(7, obj.isActive());
            stmt.setString(8, obj.getPathtopic());
            stmt.setDate(9, new java.sql.Date(obj.getBirthday().getTime()));
            stmt.setBoolean(10, obj.isVerified());
            stmt.setDate(11, new java.sql.Date(obj.getAccountCreationDate().getTime()));
            stmt.setDate(12, new java.sql.Date(obj.getLastLoginDate().getTime()));
            stmt.setInt(13, obj.getFailedLoginAttempts());
            stmt.setString(14, obj.getBio());
            stmt.setDate(15, new java.sql.Date(obj.getUpdatedAt().getTime()));
            stmt.setString(16, obj.getGoogleId());
            stmt.setString(17, obj.getAvatar());
            stmt.setString(18, obj.getGoogleAuthenticatorSecret());
            stmt.setBoolean(19, obj.isGoogleAuthenticatorEnabled());
            stmt.setInt(20, obj.getId());

            stmt.executeUpdate();
            System.out.println("User updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateUserWithoutPassword(User obj) {
        String query = "UPDATE user SET Name=?, Email=?, roles=?, Phone=?, Address=?, is_active=?, pathtopic=?, birthday=?, is_verified=?, account_creation_date=?, last_login_date=?, failed_login_attempts=?, bio=?,created_at=?, updated_at=?, google_id=?, avatar=?, google_authenticator_secret=?, is_google_authenticator_enabled=? WHERE id=?";

        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, obj.getName());
            stmt.setString(2, obj.getEmail());
            stmt.setString(3, String.join(",", obj.getRoles()));
            stmt.setString(4, obj.getPhone());
            stmt.setString(5, obj.getAddress());
            stmt.setBoolean(6, obj.isActive());
            stmt.setString(7, obj.getPathtopic());
            stmt.setDate(8, new java.sql.Date(obj.getBirthday().getTime()));
            stmt.setBoolean(9, obj.isVerified());
            stmt.setDate(10, new java.sql.Date(obj.getAccountCreationDate().getTime()));
            stmt.setDate(11, new java.sql.Date(obj.getLastLoginDate().getTime()));
            stmt.setInt(12, obj.getFailedLoginAttempts());
            stmt.setString(13, obj.getBio());
            stmt.setDate(15, new java.sql.Date(obj.getCreatedAt().getTime()));
            stmt.setDate(14, new java.sql.Date(obj.getUpdatedAt().getTime()));
            stmt.setString(16, obj.getGoogleId());
            stmt.setString(17, obj.getAvatar());
            stmt.setString(18, obj.getGoogleAuthenticatorSecret());
            stmt.setBoolean(19, obj.isGoogleAuthenticatorEnabled());
            stmt.setInt(20, obj.getId());

            stmt.executeUpdate();
            System.out.println("User updated successfully (without password).");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void delete(User obj) {
        String query = "DELETE FROM user WHERE id=?";
        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, obj.getId());
            stmt.executeUpdate();
            System.out.println("User deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public User findById(int id) {
        String query = "SELECT * FROM user WHERE id=?";
        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (Connection conn = dbConnection.getInstance().getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }


    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return PasswordVerification.verifyPassword(rawPassword, hashedPassword);
    }

    public User login(String email, String password) {
        String query = "SELECT * FROM user WHERE Email=?";
        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("Password");

                if (PasswordVerification.verifyPassword(password, storedHashedPassword)) {
                    return extractUserFromResultSet(rs);
                } else {
                    System.out.println("Incorrect password.");
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void updateUserStatus(String cin, boolean newStatus) {
        String query = "UPDATE user SET is_active=? WHERE CIN=?";
        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, newStatus);
            stmt.setString(2, cin);
            stmt.executeUpdate();
            System.out.println("User status updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<User> searchUsers(Integer minAge, Integer maxAge, String name, String cin, String address) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE 1=1";

        if (minAge != null) query += " AND TIMESTAMPDIFF(YEAR, birthday, CURDATE()) >= ?";
        if (maxAge != null) query += " AND TIMESTAMPDIFF(YEAR, birthday, CURDATE()) <= ?";
        if (name != null && !name.isEmpty()) query += " AND Name LIKE ?";
        if (cin != null) query += " AND CIN = ?";
        if (address != null && !address.isEmpty()) query += " AND Address LIKE ?";

        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int index = 1;
            if (minAge != null) stmt.setInt(index++, minAge);
            if (maxAge != null) stmt.setInt(index++, maxAge);
            if (name != null && !name.isEmpty()) stmt.setString(index++, "%" + name + "%");
            if (cin != null) stmt.setString(index++, cin);
            if (address != null && !address.isEmpty()) stmt.setString(index++, "%" + address + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }


    public boolean doesEmailExist(String email) {
        String query = "SELECT * FROM user WHERE Email=?";
        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void updatePassword(String email, String newPassword) {
        String query = "UPDATE user SET Password=? WHERE Email=?";
        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String hashedPassword = PasswordVerification.hashPassword(newPassword);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public User findByEmail(String email) {
        String query = "SELECT * FROM user WHERE Email=?";
        try (Connection conn = dbConnection.getInstance().getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("CIN"),
                rs.getString("Name"),
                rs.getString("Email"),
                rs.getString("Password"),
                List.of(rs.getString("roles").split(",")),
                rs.getString("Phone"),
                rs.getString("Address"),
                rs.getBoolean("is_active"),
                rs.getString("pathtopic"),
                rs.getDate("birthday"),
                rs.getBoolean("is_verified"),
                rs.getDate("account_creation_date"),
                rs.getDate("last_login_date"),
                rs.getInt("failed_login_attempts"),
                rs.getString("bio"),
                rs.getDate("created_at"),
                rs.getDate("updated_at"),
                rs.getString("google_id"),
                rs.getString("avatar"),
                rs.getString("google_authenticator_secret"),
                rs.getBoolean("is_google_authenticator_enabled")
        );
    }

}
