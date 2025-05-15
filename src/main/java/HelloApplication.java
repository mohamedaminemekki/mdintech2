import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/amine/userModule/login-view.fxml"));
        
        // Get screen dimensions
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        // Load the root node
        Scene scene = new Scene(fxmlLoader.load());
        
        // Set the stage size to match screen size
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        
        // Set application title
        stage.setTitle("MDINTECH");
        stage.setScene(scene);
        
        // Make it maximized by default
        stage.setMaximized(true);
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}