package entities.Rahim;

import entities.amine.User;
import java.time.LocalDateTime;

public class Comments {
    private int id;
    private int blog_post_id;  // Added to match database
    private int user_id;       // Added to match database
    private String content;
    private LocalDateTime created_at;  // Changed to match database naming
    private BlogPost blogPost;
    private User user;

    public Comments() {
        this.created_at = LocalDateTime.now();
    }

    public Comments(String content, User user, BlogPost blogPost) {
        this();
        this.content = content;
        this.user = user;
        this.blogPost = blogPost;
        if (user != null) {
            this.user_id = user.getId();
        }
        if (blogPost != null) {
            this.blog_post_id = blogPost.getId();
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBlogPostId() { return blog_post_id; }
    public void setBlogPostId(int blog_post_id) { 
        this.blog_post_id = blog_post_id; 
    }

    public int getUserId() { return user_id; }
    public void setUserId(int user_id) { 
        this.user_id = user_id; 
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return created_at; }
    public void setCreatedAt(LocalDateTime created_at) { 
        this.created_at = created_at; 
    }

    public BlogPost getBlogPost() { return blogPost; }
    public void setBlogPost(BlogPost blogPost) {
        this.blogPost = blogPost;
        if (blogPost != null) {
            this.blog_post_id = blogPost.getId();
        }
    }

    // Alias method for getBlogPostId to maintain compatibility
    public int getPostId() { 
        return getBlogPostId(); 
    }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.user_id = user.getId();
        }
    }
}
