package controllers.tasnim;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import entities.tasnim.Order;
import entities.tasnim.OrderItem;
import entities.amine.User;
import services.tasnim.OrderService;
import services.tasnim.ProductService;
import services.tasnim.EmailService;
import services.tasnim.UserService;

import java.util.List;

public class OrderManagementController {

    @FXML
    private TextField searchField;

    @FXML
    private VBox ordersContainer;

    private OrderService orderService = new OrderService();
    private ProductService productService = new ProductService();
    private EmailService emailService = new EmailService();
    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadOrders();
    }

    @FXML
    private void handleRefresh() {
        ordersContainer.getChildren().clear();
        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = orderService.getAllOrders();
        for (Order order : orders) {
            if (!order.getStatus().equalsIgnoreCase("Confirmed") && !order.getStatus().equalsIgnoreCase("Rejected")) {
                ordersContainer.getChildren().add(createOrderCard(order));
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox orderCard = new VBox();
        orderCard.getStyleClass().add("order-card");

        // Order Header (Date)
        Label dateLabel = new Label("Date: " + order.getDate());
        dateLabel.getStyleClass().add("order-header");

        // Product List
        VBox productList = new VBox();
        productList.getStyleClass().add("product-list");
        for (OrderItem item : order.getOrderItems()) {
            String productName = item.getProduct() != null ? item.getProduct().getName() : "Unknown";
            Label productLabel = new Label(productName + " x " + item.getQuantity());
            productList.getChildren().add(productLabel);
        }

        // Total Price
        double totalPrice = order.getOrderItems().stream().mapToDouble(OrderItem::getPriceTotal).sum();
        Label totalLabel = new Label("Total: $" + totalPrice);
        totalLabel.getStyleClass().add("total-price");

        // Action Buttons
        HBox actionButtons = new HBox();
        actionButtons.getStyleClass().add("action-buttons");

        Button confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().add("confirm-button");
        confirmButton.setOnAction(e -> handleConfirm(order));

        Button rejectButton = new Button("Reject");
        rejectButton.getStyleClass().add("reject-button");
        rejectButton.setOnAction(e -> handleReject(order));

        actionButtons.getChildren().addAll(confirmButton, rejectButton);

        // Add all elements to the order card
        orderCard.getChildren().addAll(dateLabel, productList, totalLabel, actionButtons);

        return orderCard;
    }

    private void handleConfirm(Order order) {
        orderService.updateOrderStatus(order.getId(), "Confirmed");
        String userEmail = getUserEmail(order.getUser() != null ? order.getUser().getCIN() : null);
        if (userEmail != null) {
            emailService.sendEmail(userEmail, "Order Confirmed", "Your order has been confirmed.");
        }
        handleRefresh();
    }

    private void handleReject(Order order) {
        orderService.updateOrderStatus(order.getId(), "Rejected");
        String userEmail = getUserEmail(order.getUser() != null ? order.getUser().getCIN() : null);
        if (userEmail != null) {
            emailService.sendEmail(userEmail, "Order Rejected", "Your order has been rejected due to storage issues.");
        }
        handleRefresh();
    }

    private String getUserEmail(String userCIN) {
        // Fetch user email from the database using UserService
        // Assuming you have a method in UserService to get user by CIN (String)
        User user = userService.getUserByCIN(userCIN);
        return user != null ? user.getEmail() : null;
    }
}