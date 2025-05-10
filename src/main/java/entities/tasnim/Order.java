package entities.tasnim;

import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private int userCIN;
    private Date date;
    private String status;
    private List<OrderItem> orderItems;

    // Constructors, Getters, and Setters
    public Order() {}

    public Order(int userCIN, Date date, String status, List<OrderItem> orderItems) {
        this.userCIN = userCIN;
        this.date = date;
        this.status = status;
        this.orderItems = orderItems;
    }

    public int getUserCIN() {
        return userCIN;
    }
    public void setUserCIN(int userCIN) {
        this.userCIN = userCIN;
    }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}