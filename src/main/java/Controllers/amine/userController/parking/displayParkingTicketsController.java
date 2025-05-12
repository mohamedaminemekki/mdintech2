package controllers.amine.userController.parking;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import services.amine.ParkingModule.ParkingService;
import services.amine.ParkingModule.ParkingTicketService;
import entities.amine.ParkingModule.ParkingTicket;
import entities.amine.ParkingModule.Parking;
import Singleton.loggedInUser;
import utils.amine.navigation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class displayParkingTicketsController {
    @FXML private ListView<String> activeTicketsListView;
    @FXML private ListView<String> allTicketsListView;
    private List<ParkingTicket> activeTickets; // Store active tickets
    private List<ParkingTicket> allTickets;   // Store all tickets

    private ParkingTicketService ticketService;
    private ParkingService parkingService;

    public displayParkingTicketsController() {
        ticketService = new ParkingTicketService();
        parkingService = new ParkingService();
    }

    @FXML
    private void initialize() {
        if (loggedInUser.getInstance().getLoggedUser() == null) {
            System.out.println("No user is logged in.");
            return;
        }

        int userId = Integer.parseInt(loggedInUser.getInstance().getLoggedUser().getCIN());

        List<ParkingTicket> tickets = ticketService.findByUserId(userId);

        activeTickets = tickets.stream()
                .filter(ParkingTicket::isStatus)
                .collect(Collectors.toList());

        List<String> activeTicketDetails = activeTickets.stream()
                .map(this::mapTicketToString)
                .collect(Collectors.toList());
        activeTicketsListView.getItems().setAll(activeTicketDetails);

        allTickets = tickets; // Store all tickets
        List<String> allTicketDetails = allTickets.stream()
                .map(this::mapTicketToString)
                .collect(Collectors.toList());
        allTicketsListView.getItems().setAll(allTicketDetails);
        System.out.println("Active ticket details: " + activeTicketDetails);
    }

    private String mapTicketToString(ParkingTicket ticket) {
        Parking parking = parkingService.findById(ticket.getParkingID());

        String parkingName = (parking != null) ? parking.getName() : "Unknown";

        return "Parking: " + parkingName +
                " | Issuing Date: " + ticket.getIssuingDate() +
                " | Expiring Date: " + ticket.getExpirationDate() +
                " | Slot ID: " + ticket.getParkingSlotID();
    }

    @FXML
    private void handleTicketClick(MouseEvent event) {
        int selectedIndex = activeTicketsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            ParkingTicket selectedTicket = activeTickets.get(selectedIndex);

            System.out.println("Selected Ticket ID: " + selectedTicket.getId());

            showUpdateExpiryDateDialog(selectedTicket);
        } else {
            System.out.println("No ticket selected.");
        }
    }

    private void updateTicketInLists(ParkingTicket updatedTicket) {
        // Update active tickets list
        for (int i = 0; i < activeTickets.size(); i++) {
            if (activeTickets.get(i).getId() == updatedTicket.getId()) {
                activeTickets.set(i, updatedTicket);
                activeTicketsListView.getItems().set(i, mapTicketToString(updatedTicket));
                break;
            }
        }

        for (int i = 0; i < allTickets.size(); i++) {
            if (allTickets.get(i).getId() == updatedTicket.getId()) {
                allTickets.set(i, updatedTicket);
                allTicketsListView.getItems().set(i, mapTicketToString(updatedTicket));
                break;
            }
        }
    }

    private void showUpdateExpiryDateDialog(ParkingTicket ticket) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Expiration Date");
        dialog.setHeaderText("Modify the expiration date and time for the selected ticket.");

        // Define buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Convert issuing & expiration dates
        LocalDateTime issuingDateTime = ticket.getIssuingDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime defaultExpirationDateTime = ticket.getExpirationDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

        // Create UI elements
        DatePicker expirationDatePicker = new DatePicker(defaultExpirationDateTime.toLocalDate());
        ComboBox<Integer> hourComboBox = new ComboBox<>();
        ComboBox<Integer> minuteComboBox = new ComboBox<>();

        // Populate time selectors
        for (int i = 0; i < 24; i++) hourComboBox.getItems().add(i);
        for (int i = 0; i < 60; i += 15) minuteComboBox.getItems().add(i);

        // Set default values
        hourComboBox.setValue(defaultExpirationDateTime.getHour());
        minuteComboBox.setValue((defaultExpirationDateTime.getMinute() / 15) * 15);

        // Layout setup
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(new Label("Expiration Date:"), 0, 0);
        grid.add(expirationDatePicker, 1, 0);
        grid.add(new Label("Hour:"), 0, 1);
        grid.add(hourComboBox, 1, 1);
        grid.add(new Label("Minutes:"), 0, 2);
        grid.add(minuteComboBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Handling user input
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                LocalDate selectedDate = expirationDatePicker.getValue();
                Integer selectedHour = hourComboBox.getValue();
                Integer selectedMinute = minuteComboBox.getValue();

                if (selectedDate == null || selectedHour == null || selectedMinute == null) {
                    showAlert("Please select a valid date and time.");
                    return null;
                }

                LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(selectedHour, selectedMinute));

                // Ensure expiration is at least 1 hour after issuing date
                if (selectedDateTime.isBefore(issuingDateTime.plusHours(1))) {
                    showAlert("Expiration time must be at least 1 hour after issuing time.");
                    return null;
                }

                // Update ticket expiration
                ticket.setExpirationDate(java.sql.Timestamp.valueOf(selectedDateTime));
                ticketService.update(ticket);
                updateTicketInLists(ticket);
            }
            return null;
        });

        dialog.showAndWait();
    }

    // Improved alert method
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleBackButton(ActionEvent event) throws IOException {
        navigation.switchScene(event, "/main-user-view.fxml");
    }

}
