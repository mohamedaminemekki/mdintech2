package controllers.amine.parkingController;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import entities.amine.ParkingModule.Parking;
import entities.amine.ParkingModule.ParkingTicket;
import services.amine.ParkingModule.ParkingTicketService;
import utils.amine.navigation;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DisplayParkingDetailsViewController implements Initializable {
    @FXML
    private Button backButton;
    @FXML
    private Label parkingNameLabel;

    @FXML
    private Label parkingLocationLabel;

    @FXML
    private Label parkingCapacityLabel;

    @FXML
    private ListView<String> ticketListView;


    private ParkingTicketService parkingTicketService;
    private int parkingId;  // Set this from the previous screen when navigating

    public DisplayParkingDetailsViewController() {
        this.parkingTicketService = new ParkingTicketService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadParkingTickets();
    }


    public void setParking(Parking parking) {
        if (parking != null) {
            parkingNameLabel.setText("Name: " + parking.getName());
            parkingLocationLabel.setText("Location: " + parking.getLocalisation());
            parkingCapacityLabel.setText("Capacity: " + parking.getCapacity());
        }
    }
    private void loadParkingTickets() {
        List<ParkingTicket> tickets = parkingTicketService.findByParkingId(parkingId);
        ticketListView.getItems().clear();

        if (tickets.isEmpty()) {
            ticketListView.getItems().add("NO tickets for this parking");
        } else {
            for (ParkingTicket ticket : tickets) {
                String ticketDetails = "Ticket #" + ticket.getId() +
                        " | Slot: " + ticket.getParkingSlotID() +
                        " | User: " + ticket.getUserID() +
                        " | Issued: " + ticket.getIssuingDate() +
                        " | Expires: " + ticket.getExpirationDate();
                ticketListView.getItems().add(ticketDetails);
            }
        }
    }
    public void setParkingId(int parkingId) {
        this.parkingId = parkingId;
        loadParkingTickets(); // Refresh when setting the parking ID
    }


    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/amine/ParkingModule/display-parkings-view.fxml");
    }

    @FXML
    public void handleMouseEntered(javafx.scene.input.MouseEvent mouseEvent) {
        backButton.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 8px; -fx-cursor: hand;");
    }

    @FXML
    public void handleMouseExited(javafx.scene.input.MouseEvent event) {
        backButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-radius: 8px; -fx-cursor: hand;");
    }


}
