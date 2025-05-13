package services.Rahim;

import entities.Rahim.Comments;
import entities.amine.User;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentServices {
    private final Connection con = MyDataBase.getInstance().getCon();

    public void addComment(Comments comment) throws SQLException {
        String query = "INSERT INTO comments (content, blog_post_id, user_id, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, comment.getContent());
            ps.setInt(2, comment.getBlogPostId());
            ps.setInt(3, comment.getUserId());
            ps.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
            }
        }
    }    public List<Comments> getCommentsForPost(int postId) throws SQLException {
        List<Comments> comments = new ArrayList<>();
        String query = "SELECT c.*, u.Name as user_name, u.Email as user_email, u.pathtopic as user_pathtopic " +
                      "FROM comments c " +
                      "LEFT JOIN user u ON c.user_id = u.id " +
                      "WHERE c.blog_post_id = ? " +
                      "ORDER BY c.created_at DESC";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comments comment = new Comments();
                    comment.setId(rs.getInt("id"));
                    comment.setContent(rs.getString("content"));
                    comment.setBlogPostId(rs.getInt("blog_post_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    
                    // Set user information
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("user_name"));
                    user.setEmail(rs.getString("user_email"));  
                    user.setPathtopic(rs.getString("user_pathtopic"));
                    comment.setUser(user);
                    
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    public void deleteComment(int commentId) throws SQLException {
        String query = "DELETE FROM comments WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, commentId);
            ps.executeUpdate();
        }
    }

    public void updateComment(Comments comment) throws SQLException {
        String query = "UPDATE comments SET content = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, comment.getContent());
            ps.setInt(2, comment.getId());
            ps.executeUpdate();
        }
    }
}