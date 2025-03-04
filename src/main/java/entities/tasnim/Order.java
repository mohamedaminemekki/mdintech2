package entities.tasnim;

import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private Date date;
    private String status;
    private int userId;
    private List<OrderItem> orderItems;

    // Constructors, Getters, and Setters
    public Order() {}

    public Order(int id, Date date, String status, int userId, List<OrderItem> orderItems) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.userId = userId;
        this.orderItems = orderItems;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}