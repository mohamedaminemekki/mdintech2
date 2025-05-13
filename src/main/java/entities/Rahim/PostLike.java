package entities.Rahim;

import entities.amine.User;
import java.time.LocalDateTime;

public class PostLike {
    private int id;
    private int blog_post_id;
    private int user_id;       // Changed from String user_cin to int user_id
    private LocalDateTime created_at;
    private BlogPost blogPost;
    private User user;

    public PostLike() {
        this.created_at = LocalDateTime.now();
    }

    public PostLike(BlogPost blogPost, User user) {
        this();
        this.blogPost = blogPost;
        this.user = user;
        if (user != null) {
            this.user_id = user.getId();  // Changed to use ID instead of CIN
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

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.user_id = user.getId();  // Changed to use ID instead of CIN
        }
    }
}
