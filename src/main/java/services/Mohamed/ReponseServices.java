package services.Mohamed;

import entities.Mohamed.Reponse;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseServices implements IService<Reponse> {
    private Connection conn;

    public ReponseServices() {
        conn = MyDataBase.getInstance().getCon();
    }

    @Override
    public void add(Reponse r) throws SQLException {
        String query = "INSERT INTO reponse (reclamationId, message, datee) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, r.getReclamationId());
        pstmt.setString(2, r.getMessage());
        pstmt.setString(3, r.getDatee());
        pstmt.executeUpdate();
        System.out.println("Reponse added successfully!");
    }

    @Override
    public void update(Reponse r) throws SQLException {
        String query = "UPDATE reponse SET message = ?, datee = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, r.getMessage());
        pstmt.setString(2, r.getDatee());
        pstmt.setInt(3, r.getId());
        int rowsUpdated = pstmt.executeUpdate();
        if (rowsUpdated > 0) {
            System.out.println("Reponse updated successfully!");
        } else {
            System.out.println("Reponse not found.");
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM reponse WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, id);
        int rowsDeleted = pstmt.executeUpdate();
        if (rowsDeleted > 0) {
            System.out.println("Reponse deleted successfully!");
        } else {
            System.out.println("Reponse not found.");
        }
    }

    @Override
    public List<Reponse> readList() throws SQLException {
        List<Reponse> responses = new ArrayList<>();
        String query = "SELECT * FROM reponse";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            Reponse r = new Reponse(
                    rs.getInt("id"),
                    rs.getInt("reclamationId"),
                    rs.getString("message"),
                    rs.getString("datee")
            );
            responses.add(r);
        }
        return responses;
    }

    @Override
    public void addP(Reponse r) throws SQLException {
        add(r);
    }
}
