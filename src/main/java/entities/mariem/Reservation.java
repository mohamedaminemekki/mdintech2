package entities.mariem;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private int userId;
    private int tripId;
    private int transportId;
    private Timestamp reservationTime;
    private String status;
    private int seatNumber;
    private String seatType;
    private String paymentStatus;
    private String departure;
    private String destination;
    private Timestamp departureTime;
    private double price;

    // Constructeurs
    public Reservation(int id, int userId, int tripId, int transportId, Timestamp reservationTime, String status, int seatNumber, String seatType, String paymentStatus, String departure, String destination, Timestamp departureTime, double price) {
        this.id = id;
        this.userId = userId;
        this.tripId = tripId;
        this.transportId = transportId;
        this.reservationTime = reservationTime;
        this.status = status;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.paymentStatus = paymentStatus;
        this.departure = departure;
        this.destination = destination;
        this.departureTime = departureTime;
        this.price = price;
    }

    public Reservation(int userId, int tripId, int transportId, Timestamp reservationTime, String status, int seatNumber, String seatType, String paymentStatus, String departure, String destination, Timestamp departureTime, double price) {
        this.userId = userId;
        this.tripId = tripId;
        this.transportId = transportId;
        this.reservationTime = reservationTime;
        this.status = status;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.paymentStatus = paymentStatus;
        this.departure = departure;
        this.destination = destination;
        this.departureTime = departureTime;
        this.price = price;
    }

    public Reservation() {}

    public Reservation(int userId, int tripId, int transportId, Timestamp reservationTime, String status, int seatNumber, String seatType, String paymentStatus) {
    }

    public Reservation(int resId, int i, int i1, int i2, Timestamp newReservationTime, String newStatus, int newSeatNumber, String newSeatType, String newPaymentStatus) {
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getTransportId() { return transportId; }
    public void setTransportId(int transportId) { this.transportId = transportId; }

    public Timestamp getReservationTime() { return reservationTime; }
    public void setReservationTime(Timestamp reservationTime) { this.reservationTime = reservationTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Timestamp getDepartureTime() { return departureTime; }
    public void setDepartureTime(Timestamp departureTime) { this.departureTime = departureTime; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", userId=" + userId +
                ", tripId=" + tripId +
                ", transportId=" + transportId +
                ", reservationTime=" + reservationTime +
                ", status='" + status + '\'' +
                ", seatNumber=" + seatNumber +
                ", seatType='" + seatType + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", departure='" + departure + '\'' +
                ", destination='" + destination + '\'' +
                ", departureTime=" + departureTime +
                ", price=" + price +
                '}';
    }

    public void setReservationId(int reservationId) {
        this.id = reservationId;
    }

    public int getReservationId() {
        return this.id;
    }
}