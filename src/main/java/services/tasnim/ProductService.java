package services.tasnim;


import entities.tasnim.Product;
import utils.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductService implements IService<Product> {
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());

    @Override
    public List<Product> readList() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, reference, price, stock_limit, stock, image_path, sold, description, category, created_at FROM product";
        try (Connection conn = db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String reference = rs.getString("reference");
                double price = rs.getDouble("price");
                int stockLimit = rs.getInt("stock_limit");
                int stock = rs.getInt("stock");
                int sold = rs.getInt("sold");
                String imagePath = rs.getString("image_path");
                String description = rs.getString("description");
                String category = rs.getString("category");
                java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : java.time.LocalDateTime.now();
                products.add(new Product(id, name, reference, price, stockLimit, stock, imagePath, sold, description, category, createdAt));
            }
        } catch (SQLException e) {
            throw e;
        }
        return products;
    }

    public void incrementSoldCount(int productId) {
        // Fix: Use the correct table name 'product' instead of 'products'
        String query = "UPDATE product SET sold = sold + 1 WHERE id = ?";
        try (Connection conn = db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void add(Product product) throws SQLException {
        String productQuery = "INSERT INTO product (name, reference, price, stock_limit, stock, image_path, sold, description, category, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getCon();
             PreparedStatement productStmt = conn.prepareStatement(productQuery, Statement.RETURN_GENERATED_KEYS)) {
            productStmt.setString(1, product.getName());
            productStmt.setString(2, product.getReference());
            productStmt.setDouble(3, product.getPrice());
            productStmt.setInt(4, product.getStockLimit());
            productStmt.setInt(5, product.getStock());
            productStmt.setString(6, product.getImagePath());
            productStmt.setInt(7, product.getSold());
            productStmt.setString(8, product.getDescription());
            productStmt.setString(9, product.getCategory());
            productStmt.setTimestamp(10, Timestamp.valueOf(product.getCreatedAt()));
            productStmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void update(Product product) throws SQLException {
        String productQuery = "UPDATE product SET name = ?, reference = ?, price = ?, stock_limit = ?, stock = ?, image_path = ?, sold = ?, description = ?, category = ?, created_at = ? WHERE id = ?";
        try (Connection conn = db.getCon();
             PreparedStatement productStmt = conn.prepareStatement(productQuery)) {
            productStmt.setString(1, product.getName());
            productStmt.setString(2, product.getReference());
            productStmt.setDouble(3, product.getPrice());
            productStmt.setInt(4, product.getStockLimit());
            productStmt.setInt(5, product.getStock());
            productStmt.setString(6, product.getImagePath());
            productStmt.setInt(7, product.getSold());
            productStmt.setString(8, product.getDescription());
            productStmt.setString(9, product.getCategory());
            productStmt.setTimestamp(10, Timestamp.valueOf(product.getCreatedAt()));
            productStmt.setInt(11, product.getId());
            productStmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    @Override
    public void delete(int productId) throws SQLException {
        String deleteProductQuery = "DELETE FROM product WHERE id = ?";
        try (Connection conn = db.getCon();
             PreparedStatement deleteProductStmt = conn.prepareStatement(deleteProductQuery)) {
            deleteProductStmt.setInt(1, productId);
            deleteProductStmt.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    public String getProductNameById(int productId) {
        // Fix: Use the correct table name 'product' instead of 'products'
        String sql = "SELECT name FROM product WHERE id = ?";
        try (Connection conn = db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Product";
    }

    public List<Product> getLowStockProducts() {
        List<Product> lowStockProducts = new ArrayList<>();
        String query = "SELECT id, name, reference, price, stock_limit, stock, image_path, sold, description, category, created_at FROM product WHERE stock < stock_limit";
        try (Connection conn = db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String reference = rs.getString("reference");
                double price = rs.getDouble("price");
                int stockLimit = rs.getInt("stock_limit");
                int stock = rs.getInt("stock");
                int sold = rs.getInt("sold");
                String imagePath = rs.getString("image_path");
                String description = rs.getString("description");
                String category = rs.getString("category");
                java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : java.time.LocalDateTime.now();
                Product product = new Product(id, name, reference, price, stockLimit, stock, imagePath, sold, description, category, createdAt);
                lowStockProducts.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lowStockProducts;
    }

    public void updateStock(int productId, int quantityAdded, String location) {
        // Update the stock directly in the product table, ignore location
        String sql = "UPDATE product SET stock = stock + ? WHERE id = ?";
        try (Connection conn = db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityAdded);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
            LOGGER.info("Stock updated successfully for product ID: " + productId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating stock", e);
        }
    }

    public int getTotalProducts() {
        String query = "SELECT COUNT(*) AS total FROM product";
        try (Connection conn = db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, reference, price, stock_limit, stock, image_path, sold, description, category, created_at FROM product";
        try (Connection conn = db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String reference = rs.getString("reference");
                double price = rs.getDouble("price");
                int stockLimit = rs.getInt("stock_limit");
                int stock = rs.getInt("stock");
                int sold = rs.getInt("sold");
                String imagePath = rs.getString("image_path");
                String description = rs.getString("description");
                String category = rs.getString("category");
                java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : java.time.LocalDateTime.now();
                products.add(new Product(id, name, reference, price, stockLimit, stock, imagePath, sold, description, category, createdAt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void addProduct(Product product) {
        // Use the same logic as add()
        try {
            add(product);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding product to database", e);
        }
    }

    public void updateProduct(Product product) {
        // Use the same logic as update()
        try {
            update(product);
            LOGGER.info("Product updated successfully: " + product.getName());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating product", e);
        }
    }

    public void deleteProduct(int productId) {
        // Use the same logic as delete()
        try {
            delete(productId);
            LOGGER.info("Product deleted successfully: " + productId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting product", e);
        }
    }

    public int getTotalOrders() {
        int totalOrders = 0;
        String query = "SELECT COUNT(*) AS total FROM `orders`";
        try (Connection conn =db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                totalOrders = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalOrders;
    }

    public double getTotalRevenue() {
        double totalRevenue = 0;
        // Fix: Use the correct column name 'price_total' instead of 'priceTotal'
        String query = "SELECT SUM(price_total) AS total FROM order_item";
        try (Connection conn =db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                totalRevenue = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalRevenue;
    }

    public int getLowStockProductsNumber() {
        int lowStockProducts = 0;
        String query = "SELECT COUNT(*) AS total FROM product WHERE stock < stock_limit";
        try (Connection conn = db.getCon();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                lowStockProducts = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lowStockProducts;
    }

    public entities.tasnim.Product getProductById(int productId) {
        // Fix: Use the correct table and column names for 'product'
        String sql = "SELECT * FROM product WHERE id = ?";
        try (Connection conn = utils.db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Fill in all required fields for Product constructor
                return new entities.tasnim.Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("reference"),
                        rs.getDouble("price"),
                        rs.getInt("stock_limit"),
                        rs.getInt("stock"),
                        rs.getString("image_path"),
                        rs.getInt("sold"),
                        rs.getString("description") != null ? rs.getString("description") : "",
                        rs.getString("category") != null ? rs.getString("category") : "",
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : java.time.LocalDateTime.now()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}