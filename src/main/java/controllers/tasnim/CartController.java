package controllers.tasnim;

import Singleton.loggedInUser;
import entities.amine.User;
import entities.tasnim.Order;
import entities.tasnim.OrderItem;
import entities.tasnim.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import services.tasnim.OrderService;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CartController {
    @FXML
    private ListView<String> cartListView;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button confirmButton;
    @FXML
    private Button deleteButton;

    private ObservableList<String> cartItems = FXCollections.observableArrayList();
    private double totalPrice = 0.0;
    private List<Product> cartProducts = new ArrayList<>();
    private OrderService orderService = new OrderService();

    @FXML
    public void initialize() {
        cartListView.setItems(cartItems);
    }

    // This method is called from MainController to populate the cart page.
    public void setCartData(List<Product> cartProducts, double totalPrice) {
        this.cartProducts = cartProducts;
        this.totalPrice = totalPrice;
        cartItems.clear();
        for (Product product : cartProducts) {
            cartItems.add(product.getName() + " - " + String.format("%.2f dt", product.getPrice()));
        }
        totalPriceLabel.setText(String.format("Total: %.2f dt", totalPrice));
    }

    @FXML
    private void handleDeleteOrder() {
        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int selectedIndex = cartListView.getSelectionModel().getSelectedIndex();
            cartItems.remove(selectedIndex);
            cartProducts.remove(selectedIndex);
            // Recalculate total price
            totalPrice = cartProducts.stream().mapToDouble(Product::getPrice).sum();
            totalPriceLabel.setText(String.format("Total: %.2f dt", totalPrice));
        }
    }

    @FXML
    private void handleConfirmOrder() {
        if (cartProducts.isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty. Add items before confirming.");
            return;
        }

        // Get logged-in user from singleton
        User currentUser = loggedInUser.getInstance().getLoggedUser();
        if (currentUser == null) {
            showAlert("Error", "No user logged in!");
            return;
        }

        // Create and populate Order
        Order order = new Order();
        order.setDate(new Date());
        order.setStatus("Pending");
        order.setUser(currentUser); // Link to entities.amine.User
        order.setOrderItems(new ArrayList<>());

        // Create OrderItems with Product references
        List<OrderItem> orderItems = new ArrayList<>();
        for (Product product : cartProducts) {
            OrderItem item = new OrderItem();
            item.setProduct(product); // Set Product object
            item.setQuantity(1); // Default quantity 1, adjust if needed
            item.setPriceTotal(product.getPrice());
            item.setOrder(order); // Set parent Order
            orderItems.add(item);
            order.addOrderItem(item); // Maintain bidirectional link
        }

        // Save to database
        int orderId = orderService.saveOrder(order, orderItems);
        if (orderId != -1) {
            showAlert("Success", "Order #" + orderId + " confirmed!");
            clearCart();
        } else {
            showAlert("Error", "Failed to confirm order.");
        }
    }

    private void clearCart() {
        cartProducts.clear();
        cartItems.clear();
        totalPrice = 0.0;
        totalPriceLabel.setText("Total: 0.00 dt");
    }

    private void showAlert(String title, String message) {
        new Alert(Alert.AlertType.INFORMATION, message)
                .setTitle(title);
    }
}