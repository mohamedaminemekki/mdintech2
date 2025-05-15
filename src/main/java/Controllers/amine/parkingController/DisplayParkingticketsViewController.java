package Controllers.amine.parkingController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import entities.amine.ParkingModule.ParkingTicket;
import javafx.stage.Stage;
import services.amine.ParkingModule.ParkingTicketService;
import services.amine.userService;
import entities.amine.User;
import utils.amine.navigation;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DisplayParkingticketsViewController {

    @FXML
    private ListView<String> ticketListView;
    @FXML
    private AnchorPane ticketDetailPane;
    @FXML
    private Label detailTitle;
    @FXML
    private Text detailContent;
    @FXML
    private Button closeButton;

    private final ParkingTicketService ticketService = new ParkingTicketService();
    private final userService userService = new userService();

    @FXML
    public void initialize() {
        loadParkingTickets();
    }

    private void loadParkingTickets() {
        List<ParkingTicket> tickets = ticketService.findAll();
        List<String> ticketDescriptions = tickets.stream()
                .map(ticket -> {
                    User user = userService.findById(ticket.getUserID());
                    String userName = (user != null) ? user.getName() : "Unknown User";
                    return "Parking ID: " + ticket.getParkingID() +
                            " | Slot: " + ticket.getParkingSlotID() +
                            " | User: " + userName +
                            " | Date: " + ticket.getIssuingDate();
                })
                .collect(Collectors.toList());

        ticketListView.getItems().addAll(ticketDescriptions);
        ticketListView.setOnMouseClicked(this::handleTicketClick);
    }

    private void handleTicketClick(MouseEvent event) {
        String selectedItem = ticketListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int selectedIndex = ticketListView.getSelectionModel().getSelectedIndex();
            ParkingTicket selectedTicket = ticketService.findAll().get(selectedIndex);
            showTicketDetails(selectedTicket);
        }
    }

    private void showTicketDetails(ParkingTicket ticket) {
        User user = userService.findById(ticket.getUserID());
        String userName = (user != null) ? user.getName() : "Unknown User";

        detailTitle.setText("🚗 Ticket ID: " + ticket.getId());

        String statusStyle = ticket.isStatus() ? "-fx-fill: #28a745; -fx-font-weight: bold;" : "-fx-fill: #dc3545; -fx-font-weight: bold;";
        String statusText = ticket.isStatus() ? "Active 🟢" : "Expired 🔴";

        detailContent.setText(
                "🔒 Slot ID: " + ticket.getParkingSlotID() + "\n" +
                        "👤 User: " + userName + "\n" +
                        "📅 Issuing Date: " + ticket.getIssuingDate() + "\n" +
                        "📆 Expiration Date: " + ticket.getExpirationDate() + "\n" +
                        statusText
        );
        detailContent.setStyle(statusStyle);

        // Center the popup on the screen
        Stage stage = (Stage) ticketDetailPane.getScene().getWindow();
        stage.centerOnScreen();

        ticketDetailPane.setStyle("-fx-background-color: white; -fx-border-color: #007bff; -fx-border-radius: 10; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 10);");
        ticketDetailPane.setVisible(true);
    }

    public void closeTicketDetail(ActionEvent event) {
        ticketDetailPane.setVisible(false);
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-admin-view.fxml");
    }
}
