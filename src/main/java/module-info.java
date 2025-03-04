module mdintech {
    requires javafx.fxml;
    requires java.sql;
    requires spring.security.crypto;
    requires jakarta.mail;
    requires java.dotenv;
    requires commons.validator;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.auth;

    requires javafx.controls;
    requires java.desktop;
    requires org.apache.httpcomponents.httpcore;
    requires jdk.httpserver;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.javafx;
    requires org.json;
    // requires javafx.web;

    // Open the Controller package to javafx.fxml for reflection
    opens controllers to javafx.fxml;
    opens controllers.amine.userController to javafx.fxml;
    opens  controllers.amine.parkingController to javafx.fxml;
    opens controllers.amine.userController.parking  to javafx.fxml;



    // Export the main package
    opens main to javafx.fxml;

    // Export required packages
    exports main;
    exports controllers.amine.userController;
}