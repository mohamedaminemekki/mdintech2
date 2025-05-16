package entities.Rahim;

import entities.amine.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogPost {
    private int id;
    private int userId;  // Added to match database field
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime postDate;
    private String imageUrl;
    private boolean approved;
    private List<Comments> comments;
    private List<PostLike> likes;
    private String category;
    private User user;  // Transient reference to User object

    public static final List<String> VALID_CATEGORIES = List.of(
        "Technology", "Travel", "Food", "Lifestyle", 
        "Fashion", "Health", "Sports", "Business"
    );

    public BlogPost() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.postDate = LocalDateTime.now();
        this.approved = false;
        this.comments = new ArrayList<>();
        this.likes = new ArrayList<>();
    }

    public BlogPost(String title, String content, String category, User user) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
        this.user = user;
        this.userId = user.getId();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPostDate() { return postDate; }
    public void setPostDate(LocalDateTime postDate) { this.postDate = postDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public List<Comments> getComments() { return comments; }
    public void setComments(List<Comments> comments) { this.comments = comments; }

    public List<PostLike> getLikes() { return likes; }
    public void setLikes(List<PostLike> likes) { this.likes = likes; }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        if (category == null || category.isEmpty() || VALID_CATEGORIES == null || !VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Invalid category. Must be one of: " + VALID_CATEGORIES);
        }
        this.category = category;
    }

    public User getUser() { return user; }
    public void setUser(User user) { 
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    // Helper methods for managing relationships
    public void addComment(Comments comment) {
        if (!this.comments.contains(comment)) {
            this.comments.add(comment);
            comment.setBlogPost(this);
        }
    }

    public void removeComment(Comments comment) {
        if (this.comments.remove(comment)) {
            comment.setBlogPost(null);
        }
    }

    public void addLike(PostLike like) {
        if (!this.likes.contains(like)) {
            this.likes.add(like);
            like.setBlogPost(this);
        }
    }

    public void removeLike(PostLike like) {
        if (this.likes.remove(like)) {
            like.setBlogPost(null);
        }
    }

    // Update timestamps before saving
    public void updateTimestamps() {
        this.updatedAt = LocalDateTime.now();
    }

    public int getLikeCount() { return likes.size(); }
    public int getCommentCount() { return comments.size(); }
}