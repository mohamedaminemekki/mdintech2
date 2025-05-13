package entities.tasnim;

public class OrderItem {
    private int id;
    private Product product;
    private int quantity;
    private double priceTotal;
    private Order order;
    private java.time.LocalDateTime createdAt;

    // Constructors, Getters, and Setters
    public OrderItem() {}

    public OrderItem(int id, Product product, int quantity, double priceTotal, Order order, java.time.LocalDateTime createdAt) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.priceTotal = priceTotal;
        this.order = order;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceTotal() { return priceTotal; }
    public void setPriceTotal(double priceTotal) { this.priceTotal = priceTotal; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : null) +
                ", quantity=" + quantity +
                ", priceTotal=" + priceTotal +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
}