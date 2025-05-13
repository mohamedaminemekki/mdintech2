package services.tasnim;

import Singleton.loggedInUser;
import entities.tasnim.Order;
import entities.tasnim.OrderItem;
import utils.db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `orders`";

        try (Connection conn = db.getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                // Fetch and set the User object (assuming you have a UserService)
                String userCIN = rs.getString("user_id");
                entities.amine.User user = new services.tasnim.UserService().getUserByCIN(userCIN);
                order.setUser(user);
                order.setDate(rs.getDate("date"));
                order.setStatus(rs.getString("status"));
                // Set product from product_id column if present
                int productId = rs.getInt("product_id");
                if (!rs.wasNull() && productId > 0) {
                    entities.tasnim.Product product = new services.tasnim.ProductService().getProductById(productId);
                    order.setProduct(product);
                }
                // Fetch order items
                List<OrderItem> orderItems = getOrderItemsByOrderId(order.getId());
                order.setOrderItems(orderItems);
                orders.add(order);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching orders from database", e);
        }

        return orders;
    }

    private List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        String sql = "SELECT * FROM order_item WHERE order_id = ?";

        try (Connection conn = db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                // Fetch and set the Product object (assuming you have a ProductService)
                int productId = rs.getInt("product_id");
                entities.tasnim.Product product = new services.tasnim.ProductService().getProductById(productId);
                item.setProduct(product);
                item.setQuantity(rs.getInt("quantity"));
                item.setPriceTotal(rs.getDouble("price_total"));
                // Set parent order if needed
                // item.setOrder(order); // Only if you have the order object here
                orderItems.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching order items from database", e);
        }

        return orderItems;
    }

    public void updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE `orders` SET status = ? WHERE id = ?";

        try (Connection conn = db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
            LOGGER.info("Order status updated successfully: " + orderId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order status in database", e);
        }
    }

    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) AS total FROM `orders`";
        try (Connection conn = db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving total number of orders", e);
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(price_total) AS total FROM order_item";
        try (Connection conn = db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving total revenue", e);
        }
        return 0.0;
    }

    public int saveOrder(Order order, List<OrderItem> orderItems) {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        ResultSet generatedKeys = null;
        int orderId = -1;

        try {
            conn = db.getCon();
            conn.setAutoCommit(false); // Start a transaction

            // Insert the order
            String orderSql = "INSERT INTO `orders` (date, status, user_id, product_id, created_at) VALUES (?, ?, ?, ?, ?)";
            orderStmt = conn.prepareStatement(orderSql, PreparedStatement.RETURN_GENERATED_KEYS);
            orderStmt.setDate(1, new java.sql.Date(order.getDate().getTime()));
            orderStmt.setString(2, order.getStatus());
            // Use user.getId() for user_id foreign key
            orderStmt.setInt(3, order.getUser() != null ? order.getUser().getId() : java.sql.Types.NULL);
            // Set product_id to the first product in the order (if available), else null
            Integer productId = null;
            if (order.getProduct() != null) {
                productId = order.getProduct().getId();
            } else if (orderItems != null && !orderItems.isEmpty() && orderItems.get(0).getProduct() != null) {
                productId = orderItems.get(0).getProduct().getId();
            }
            if (productId != null && productId > 0) {
                orderStmt.setInt(4, productId);
            } else {
                orderStmt.setNull(4, java.sql.Types.INTEGER);
            }
            // Always set created_at for the order
            Timestamp orderCreatedAt = order.getCreatedAt() != null ? new Timestamp(order.getCreatedAt().getTime()) : new Timestamp(System.currentTimeMillis());
            orderStmt.setTimestamp(5, orderCreatedAt);
            orderStmt.executeUpdate();

            // Get the generated order ID
            generatedKeys = orderStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);

                // Insert each order item
                String itemSql = "INSERT INTO order_item (product_id, order_id, quantity, price_total) VALUES (?, ?, ?, ?)";
                itemStmt = conn.prepareStatement(itemSql);

                for (OrderItem item : orderItems) {
                    if (item.getProduct() == null || item.getProduct().getId() <= 0) {
                        throw new SQLException("OrderItem has null or invalid Product. Each order item must reference a valid product_id.");
                    }
                    itemStmt.setInt(1, item.getProduct().getId());
                    itemStmt.setInt(2, orderId);
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getPriceTotal());
                    itemStmt.addBatch(); // Add to batch for bulk insert
                }

                itemStmt.executeBatch(); // Execute all inserts in one batch
            }

            conn.commit(); // Commit the transaction
            LOGGER.info("Order and items saved successfully. Order ID: " + orderId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving order and items to database", e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            // Clean up resources
            try { if (generatedKeys != null) generatedKeys.close(); } catch (Exception ignored) {}
            try { if (orderStmt != null) orderStmt.close(); } catch (Exception ignored) {}
            try { if (itemStmt != null) itemStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
        }

        return orderId;
    }

    public void deleteOrder(int orderId) {
        Connection conn = null;
        PreparedStatement deleteOrderItemsStmt = null;
        PreparedStatement deleteOrderStmt = null;

        try {
            conn = db.getCon();
            conn.setAutoCommit(false); // Start a transaction

            // 1. Delete order items
            String deleteOrderItemsSql = "DELETE FROM order_item WHERE order_id = ?";
            deleteOrderItemsStmt = conn.prepareStatement(deleteOrderItemsSql);
            deleteOrderItemsStmt.setInt(1, orderId);
            deleteOrderItemsStmt.executeUpdate();

            // 2. Delete the order
            String deleteOrderSql = "DELETE FROM `orders` WHERE id = ?";
            deleteOrderStmt = conn.prepareStatement(deleteOrderSql);
            deleteOrderStmt.setInt(1, orderId);
            deleteOrderStmt.executeUpdate();

            conn.commit(); // Commit the transaction
            LOGGER.info("Order and associated items deleted successfully: " + orderId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting order and items from database", e);
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
        } finally {
            try { if (deleteOrderItemsStmt != null) deleteOrderItemsStmt.close(); } catch (Exception ignored) {}
            try { if (deleteOrderStmt != null) deleteOrderStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    private boolean userExists(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM user WHERE id = ?";
        try (Connection conn = db.getCon();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
        }
    }
}