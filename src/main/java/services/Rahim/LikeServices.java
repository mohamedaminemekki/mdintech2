package services.Rahim;

import entities.Rahim.PostLike;
import entities.amine.User;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LikeServices {
    private final Connection con = MyDataBase.getInstance().getCon();

    public void toggleLike(int userId, int postId) throws SQLException {
        if (hasLiked(userId, postId)) {
            String deleteQuery = "DELETE FROM post_like WHERE user_id = ? AND blog_post_id = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteQuery)) {
                ps.setInt(1, userId);
                ps.setInt(2, postId);
                ps.executeUpdate();
            }
        } else {
            String insertQuery = "INSERT INTO post_like (user_id, blog_post_id, created_at) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertQuery)) {
                ps.setInt(1, userId);
                ps.setInt(2, postId);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
        }
    }

    public List<PostLike> getLikesForPost(int postId) throws SQLException {
        List<PostLike> likes = new ArrayList<>();
        String query = "SELECT * FROM post_like WHERE blog_post_id = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PostLike like = new PostLike();
                    like.setId(rs.getInt("id"));
                    like.setBlogPostId(rs.getInt("blog_post_id"));
                    like.setUserId(rs.getInt("user_id"));
                    like.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    likes.add(like);
                }
            }
        }
        return likes;
    }

    private boolean hasLiked(int userId, int postId) throws SQLException {
        String query = "SELECT COUNT(*) FROM post_like WHERE user_id = ? AND blog_post_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<User> getUsersWhoLikedPost(int postId) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.* FROM post_like l JOIN user u ON l.user_id = u.id WHERE l.blog_post_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    // Add other user fields as needed
                    users.add(user);
                }
            }
        }
        return users;
    }
}
