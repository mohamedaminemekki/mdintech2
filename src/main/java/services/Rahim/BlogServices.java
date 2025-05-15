package services.Rahim;

import entities.Rahim.BlogPost;
import entities.Rahim.Comments;
import entities.Rahim.PostLike;
import entities.amine.User;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogServices {
    private final Connection con = MyDataBase.getInstance().getCon();

    public void createPost(BlogPost post) throws SQLException {
        String query = "INSERT INTO blog_post (title, content, user_id, created_at, post_date, approved, image_url, category, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println("[DEBUG] Creating post with values:");
        System.out.println("Title: " + post.getTitle());
        System.out.println("Content: " + post.getContent());
        System.out.println("UserId: " + post.getUserId());
        System.out.println("CreatedAt: " + post.getCreatedAt());
        System.out.println("ImageUrl: " + post.getImageUrl());
        System.out.println("Category: " + post.getCategory());
        try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setInt(3, post.getUserId());
            Timestamp now = Timestamp.valueOf(post.getCreatedAt());
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now); // post_date defaults to created_at
            ps.setBoolean(6, true); // post is approved by default
            ps.setString(7, post.getImageUrl());
            ps.setString(8, post.getCategory());
            ps.setTimestamp(9, now); // updated_at initially same as created_at
            try {
                ps.executeUpdate();
            } catch (SQLException ex) {
                System.err.println("[ERROR] SQL Exception during post insert: " + ex.getMessage());
                ex.printStackTrace();
                throw ex;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    post.setId(rs.getInt(1));
                }
            }
        }
    }    public List<BlogPost> getAllPosts() throws SQLException {
        List<BlogPost> posts = new ArrayList<>();
        String query = "SELECT b.*, u.Name as user_name, u.Email as user_email, u.pathtopic as user_pathtopic " +
                      "FROM blog_post b " +
                      "LEFT JOIN user u ON b.user_id = u.id " +
                      "ORDER BY b.created_at DESC";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                posts.add(mapResultSetToBlogPost(rs));
            }
        }
        return posts;
    }

    public List<BlogPost> getApprovedPosts() throws SQLException {
        List<BlogPost> posts = new ArrayList<>();
        String query = "SELECT b.*, u.Name as user_name, u.Email as user_email, u.pathtopic as user_pathtopic " +
                      "FROM blog_post b " +
                      "LEFT JOIN user u ON b.user_id = u.id " +
                      "WHERE b.approved = true " +
                      "ORDER BY b.created_at DESC";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                posts.add(mapResultSetToBlogPost(rs));
            }
        }
        return posts;
    }    private BlogPost mapResultSetToBlogPost(ResultSet rs) throws SQLException {
        BlogPost post = new BlogPost();
        post.setId(rs.getInt("id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setUserId(rs.getInt("user_id"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        post.setPostDate(rs.getTimestamp("post_date").toLocalDateTime());
        post.setApproved(rs.getBoolean("approved"));
        post.setImageUrl(rs.getString("image_url"));
        post.setCategory(rs.getString("category"));
        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        // Map user information
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setName(rs.getString("user_name"));
        user.setEmail(rs.getString("user_email"));
        user.setPathtopic(rs.getString("user_pathtopic"));
        post.setUser(user);

        // Load comments
        CommentServices commentServices = new CommentServices();
        List<Comments> comments = commentServices.getCommentsForPost(post.getId());
        post.setComments(comments);

        // Load likes
        LikeServices likeServices = new LikeServices();
        List<PostLike> likes = likeServices.getLikesForPost(post.getId());
        post.setLikes(likes);
        
        return post;
    }public BlogPost getPostById(int id) throws SQLException {
        String query = "SELECT b.*, u.Name as user_name, u.Email as user_email, u.pathtopic as user_pathtopic " +
                      "FROM blog_post b " +
                      "LEFT JOIN user u ON b.user_id = u.id " +
                      "WHERE b.id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBlogPost(rs);
                }
            }
        }
        return null;
    }public void deletePost(int postId) throws SQLException {
        String query = "DELETE FROM blog_post WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, postId);
            ps.executeUpdate();
        }
    }

    public void updatePost(BlogPost post) throws SQLException {
        String query = "UPDATE blog_post SET title = ?, content = ?, image_url = ?, category = ?, " +
                "approved = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getImageUrl());
            ps.setString(4, post.getCategory());
            ps.setBoolean(5, post.isApproved());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(7, post.getId());
            ps.executeUpdate();
        }
    }
}
