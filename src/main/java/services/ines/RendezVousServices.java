package services.ines;

import entities.ines.RendezVous;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RendezVousServices implements IService<RendezVous> {

    Connection con;


    public RendezVousServices() {
        con = MyDataBase.getInstance().getCon();

    }

    // Fonction readList pour récupérer tous les rendez-vous
    public List<RendezVous> readList() throws SQLException {
        String query = "SELECT * FROM `rendezvous`";
        List<RendezVous> rendezVousList = new ArrayList<>();
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery(query);
        while (rs.next()) {
            // Récupérer la date et l'heure séparées
            LocalDate date = rs.getDate("dateRendezVous").toLocalDate();
            LocalTime time = rs.getTime("timeRendezVous").toLocalTime();

            // Créer un objet RendezVous avec la date et l'heure séparées
            RendezVous r = new RendezVous(
                    rs.getInt("idRendezVous"),
                    date,
                    time,
                    rs.getString("lieu"),
                    rs.getString("status"),
                    rs.getInt("idMedecin")
            );
            rendezVousList.add(r);
        }
        return rendezVousList;
    }

    // Fonction pour ajouter un rendez-vous
    public void add(RendezVous rendezVous) throws SQLException {
        String query = "INSERT INTO `rendezvous` (`dateRendezVous`, `timeRendezVous`, `lieu`, `status`, `idMedecin`) VALUES (?, ?, ?, ?, ?)";

        // Préparer la requête avec des paramètres
        PreparedStatement ps = con.prepareStatement(query);
        ps.setDate(1, Date.valueOf(rendezVous.getDateRendezVous())); // Conversion LocalDate en Date
        ps.setTime(2, Time.valueOf(rendezVous.getTimeRendezVous())); // Conversion LocalTime en Time
        ps.setString(3, rendezVous.getLieu());
        ps.setString(4, rendezVous.getStatus());
        ps.setInt(5, rendezVous.getIdMedecin());

        // Exécuter la requête pour insérer le rendez-vous
        ps.executeUpdate();
        System.out.println("Rendez-vous ajouté !");
        int idService = getServiceIdByMedecin(rendezVous.getIdMedecin());
        ServiceHospitalierServices serviceService = new ServiceHospitalierServices();
        serviceService.deleteServiceIfLowAppointments(idService);

        scheduleEmailReminder(rendezVous);



    }

    // Planifier l'envoi du mail de rappel
    private void scheduleEmailReminder(RendezVous rendezVous) {
        LocalDateTime rendezVousDateTime = LocalDateTime.of(rendezVous.getDateRendezVous(), rendezVous.getTimeRendezVous());
        LocalDateTime emailSendTime = rendezVousDateTime.minusHours(1); // Une heure avant le rendez-vous

        // Vérifier si l'heure d'envoi est dans le futur
        if (emailSendTime.isAfter(LocalDateTime.now())) {
            long delay = ChronoUnit.SECONDS.between(LocalDateTime.now(), emailSendTime); // Calculer le délai en secondes

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> sendEmail(rendezVous), delay, TimeUnit.SECONDS);

            System.out.println("Mail de rappel planifié pour " + emailSendTime);
        } else {
            System.out.println("Le rendez-vous est dans moins d'une heure. Aucun rappel ne sera envoyé.");
        }
    }

    // Envoyer le mail de rappel
    private void sendEmail(RendezVous rendezVous) {
        String recipient = "ines.rahrah@esprit.tn"; // Remplacer par l'email du patient
        String subject = "Rappel de votre rendez-vous médical";
        String content = "Bonjour,\n\nNous vous rappelons que votre rendez-vous est prévu pour le " +
                rendezVous.getDateRendezVous() + " à " + rendezVous.getTimeRendezVous() + ".\n\nCordialement,\nL'équipe médicale.";

        EmailService.sendEmail(recipient, subject, content);
        System.out.println("Mail de rappel envoyé à " + recipient);
    }





    private int getServiceIdByMedecin(int idMedecin) throws SQLException {
        String query = "SELECT idService FROM medecin WHERE idMedecin = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idMedecin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idService");
                }
            }
        }
        return -1;
    }


    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM `rendezvous` WHERE `idRendezVous` = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);  // Utilisation de l'ID passé en paramètre
        ps.executeUpdate();
    }

    // Fonction pour mettre à jour un rendez-vous
    public void update(RendezVous rendezVous) throws SQLException {
        String query = "UPDATE rendezvous SET dateRendezVous = ?, timeRendezVous = ?, lieu = ?, status = ?, idMedecin = ? WHERE idRendezVous = ?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setDate(1, Date.valueOf(rendezVous.getDateRendezVous()));  // Conversion LocalDate en Date
        ps.setTime(2, Time.valueOf(rendezVous.getTimeRendezVous()));  // Conversion LocalTime en Time
        ps.setString(3, rendezVous.getLieu());
        ps.setString(4, rendezVous.getStatus());
        ps.setInt(5, rendezVous.getIdMedecin());
        ps.setInt(6, rendezVous.getIdRendezVous());  // ID du rendez-vous à modifier

        int rowsAffected = ps.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Rendez-vous modifié avec succès.");
        } else {
            System.out.println("Aucun rendez-vous trouvé avec l'ID : " + rendezVous.getIdRendezVous());
        }
    }





    public boolean isTimeSlotTaken(LocalDate date, LocalTime time) throws SQLException {
        String query = "SELECT COUNT(*) FROM rendezvous WHERE dateRendezVous = ? AND timeRendezVous = ?";
        try (PreparedStatement statement = con.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(date)); // Convertir LocalDate en java.sql.Date
            statement.setTime(2, Time.valueOf(time)); // Convertir LocalTime en java.sql.Time
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Retourne true si un rendez-vous existe déjà à ce créneau
            }
        }
        return false; // Retourne false si le créneau est disponible
    }


    // Ajouter la méthode pour compter les rendez-vous pris par médecin
    public int countRendezVousByService(int idMedecin) throws SQLException {
        String query = "SELECT COUNT(*) FROM rendezvous r " +
                "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                "WHERE m.idService = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idMedecin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Retourne le nombre de rendez-vous pris
                }
            }
        }
        return 0; // Si aucun rendez-vous trouvé
    }

    public Map<String, Integer> getRendezVousStats() throws SQLException {
        // Récupération des comptes par service
        Map<String, Integer> rawStats = new HashMap<>();
        String query = "SELECT s.nomService, COUNT(r.idRendezVous) AS total FROM rendezvous r " +
                "JOIN medecin m ON r.idMedecin = m.idMedecin " +
                "JOIN servicehospitalier s ON m.idService = s.idService " +
                "GROUP BY s.nomService";

        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rawStats.put(rs.getString("nomService"), rs.getInt("total"));
            }
        }
        // Calcul du total général
        int total = rawStats.values().stream().mapToInt(Integer::intValue).sum();
        // Calcul du pourcentage en tronquant la partie décimale
        Map<String, Integer> percentageStats = new HashMap<>();
        for (Map.Entry<String, Integer> entry : rawStats.entrySet()) {
            int percentage = total == 0 ? 0 : (int)(((double) entry.getValue() / total) * 100);
            percentageStats.put(entry.getKey(), percentage);
        }
        return percentageStats;
    }


    // Affiche simplement dans la console le pourcentage de rendez-vous par service (pour test)
    public void afficherRendezVousStats() throws SQLException {
        Map<String, Integer> stats = getRendezVousStats();
        stats.forEach((service, percentage) ->
                System.out.println("Service " + service + " : " + percentage + "%")
        );
    }

    // Récupérer les rendez-vous pour un médecin spécifique
    public List<RendezVous> getRendezVousByMedecin(int idMedecin) throws SQLException {
        String query = "SELECT * FROM `rendezvous` WHERE idMedecin = ?";
        List<RendezVous> rendezVousList = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, idMedecin);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            LocalDate date = rs.getDate("dateRendezVous").toLocalDate();
            LocalTime time = rs.getTime("timeRendezVous").toLocalTime();
            RendezVous r = new RendezVous(
                    rs.getInt("idRendezVous"),
                    date,
                    time,
                    rs.getString("lieu"),
                    rs.getString("status"),
                    rs.getInt("idMedecin")
            );
            rendezVousList.add(r);
        }
        return rendezVousList;
    }













}
