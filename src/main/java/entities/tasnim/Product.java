package entities.tasnim;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty reference = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final IntegerProperty stockLimit = new SimpleIntegerProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty();
    private final StringProperty imagePath = new SimpleStringProperty();
    private final IntegerProperty sold = new SimpleIntegerProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final ObjectProperty<java.time.LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public Product(int id, String name, String reference, double price, int stockLimit, int stock, String imagePath, int sold) {
        this.createdAt.set(java.time.LocalDateTime.now());
    }

    public Product(int id, String name, String reference, double price, int stockLimit, int stock, String imagePath, int sold, String description, String category, java.time.LocalDateTime createdAt) {
        this.id.set(id);
        this.name.set(name);
        this.reference.set(reference);
        this.price.set(price);
        this.stockLimit.set(stockLimit);
        this.stock.set(stock);
        this.imagePath.set(imagePath);
        this.sold.set(sold);
        this.description.set(description);
        this.category.set(category);
        this.createdAt.set(createdAt);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty referenceProperty() { return reference; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty stockLimitProperty() { return stockLimit; }
    public IntegerProperty stockProperty() { return stock; }
    public StringProperty imagePathProperty() { return imagePath; }
    public IntegerProperty soldProperty() { return sold; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty categoryProperty() { return category; }
    public ObjectProperty<java.time.LocalDateTime> createdAtProperty() { return createdAt; }

    // Regular getters and setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public String getReference() { return reference.get(); }
    public void setReference(String reference) { this.reference.set(reference); }

    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }

    public int getStockLimit() { return stockLimit.get(); }
    public void setStockLimit(int stockLimit) { this.stockLimit.set(stockLimit); }

    public int getStock() { return stock.get(); }
    public void setStock(int stock) { this.stock.set(stock); }

    public String getImagePath() { return imagePath.get(); }
    public void setImagePath(String imagePath) { this.imagePath.set(imagePath); }

    public int getSold() { return sold.get(); }
    public void setSold(int sold) { this.sold.set(sold); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    public java.time.LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    /**
     * Set createdAt to now (utility for persistence)
     */
    public void setCreatedAtNow() {
        this.createdAt.set(java.time.LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", reference='" + getReference() + '\'' +
                ", price=" + getPrice() +
                ", stockLimit=" + getStockLimit() +
                ", stock=" + getStock() +
                ", imagePath='" + getImagePath() + '\'' +
                ", sold=" + getSold() +
                ", description='" + getDescription() + '\'' +
                ", category='" + getCategory() + '\'' +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
