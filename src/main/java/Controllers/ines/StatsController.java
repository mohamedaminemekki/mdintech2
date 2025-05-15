package Controllers.ines;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import services.ines.RendezVousServices;

import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

public class StatsController {

    @FXML
    private BarChart<String, Number> barChart;

    private final RendezVousServices rendezVousServices = new RendezVousServices();

    @FXML
    public void initialize() {
        try {
            // Récupère les statistiques en pourcentage (entier)
            Map<String, Integer> stats = rendezVousServices.getRendezVousStats();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Pourcentage de Rendez-vous (%)");

            Random rand = new Random();

            // Pour chaque service, ajoute la donnée (en pourcentage) dans la série
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
                series.getData().add(data);

                // Génération d'une couleur aléatoire pour la barre
                String randomColor = String.format("#%02X%02X%02X", rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

                // Appliquer le style dès que le nœud de la barre est créé
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        Platform.runLater(() -> newNode.setStyle("-fx-bar-fill: " + randomColor + ";"));
                    }
                });
            }

            barChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
