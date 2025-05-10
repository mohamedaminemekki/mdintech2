package entities.Mohamed;

public class Reclamation {
    private int id;
    private int client_id;
    private String datee;
    private String description;
    private Boolean state;
    private String type;
    private String photo;
    private String Priorite; // New field for sentiment analysis
    private String email; // New field for client email

    // Constructor with all fields
    public Reclamation(int id, int client_id, String datee, String description, Boolean state, String type, String photo, String Priorite, String email) {
        this.id = id;
        this.client_id = client_id;
        this.datee = datee;
        this.description = description;
        this.state = state;
        this.type = type;
        this.photo = photo;
        this.Priorite = Priorite;
        this.email = email;
    }

    // Constructor without ID (for creating new reclamations)
    public Reclamation(int client_id, String datee, String description, Boolean state, String type, String photo, String Priorite, String email) {
        this.client_id = client_id;
        this.datee = datee;
        this.description = description;
        this.state = state;
        this.type = type;
        this.photo = photo;
        this.Priorite = Priorite;
        this.email = email;
    }

    // Default constructor
    public Reclamation() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClient_id() { return client_id; }
    public void setClient_id(int client_id) { this.client_id = client_id; }

    public String getDatee() { return datee; }
    public void setDatee(String datee) { this.datee = datee; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getState() { return state; }
    public void setState(Boolean state) { this.state = state; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getPriorite() { return Priorite; }
    public void setPriorite(String Priorite) { this.Priorite = Priorite; }

    public String getEmail() { return email; } // Getter for email
    public void setEmail(String email) { this.email = email; } // Setter for email

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", client_id=" + client_id +
                ", datee='" + datee + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", type='" + type + '\'' +
                ", photo='" + photo + '\'' +
                ", Priorite='" + Priorite + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}