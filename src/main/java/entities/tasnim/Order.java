package entities.tasnim;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import entities.amine.User;

public class Order {
    private int id;
    private Date date;
    private String status;
    private List<OrderItem> orderItems;
    private Product product; // Assuming Product entity exists
    private Date createdAt;
    private Date confirmedAt;
    private User user; // Use entities.amine.User

    // Constructors
    public Order() {
        this.date = new Date();
        this.createdAt = new Date();
        this.orderItems = new ArrayList<>();
    }

    public Order(User user, Date date, String status, List<OrderItem> orderItems, Product product, Date createdAt, Date confirmedAt) {
        this.user = user;
        this.date = date;
        this.status = status;
        this.orderItems = orderItems != null ? orderItems : new ArrayList<>();
        this.product = product;
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.confirmedAt = confirmedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Date confirmedAt) { this.confirmedAt = confirmedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // Utility Methods
    public void addOrderItem(OrderItem orderItem) {
        if (!orderItems.contains(orderItem)) {
            orderItems.add(orderItem);
            orderItem.setOrder(this);
        }
    }

    public void removeOrderItem(OrderItem orderItem) {
        if (orderItems.remove(orderItem)) {
            if (orderItem.getOrder() == this) {
                orderItem.setOrder(null);
            }
        }
    }


    @Override
    public String toString() {
        return String.format("Order #%d - %s", id, status);
    }

    /**
     * Custom validation: Orders can only be cancelled within 24 hours of creation.
     * Throws IllegalStateException if invalid.
     */
    public void validateCancellation() {
        if ("cancelled".equals(status) && date != null) {
            long diffMs = new Date().getTime() - date.getTime();
            long hours = diffMs / (1000 * 60 * 60);
            if (hours > 24) {
                throw new IllegalStateException("Orders can only be cancelled within 24 hours of creation.");
            }
        }
    }
}