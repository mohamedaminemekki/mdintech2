module org.example.mdintech {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires commons.validator;
    requires spring.security.crypto;
    requires com.google.api.client.auth;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires java.dotenv;
    requires jakarta.mail;
    requires jdk.httpserver;
    requires java.desktop;

    opens org.example.mdintech to javafx.fxml;
    opens org.example.mdintech.Controllers.amine to javafx.fxml;
    opens org.example.mdintech.Controllers.amine.userController to javafx.fxml;
    opens org.example.mdintech.Controllers.amine.parkingController to javafx.fxml;
    opens org.example.mdintech.Controllers.amine.userController.parking  to javafx.fxml;


    exports org.example.mdintech;








}