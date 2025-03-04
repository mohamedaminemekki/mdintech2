module mdintech {
    requires javafx.fxml;
    requires spring.security.crypto;
    requires jakarta.mail;
    requires java.dotenv;
    requires commons.validator;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.auth;

    requires org.apache.httpcomponents.httpcore;
    requires jdk.httpserver;
    requires org.kordamp.ikonli.fontawesome;
    requires org.kordamp.ikonli.javafx;
    requires org.json;
    requires javafx.web;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.net.http;
    requires com.google.gson;
    requires java.sql;
    requires java.desktop;
    // requires javafx.web;

    // Open the Controller package to javafx.fxml for reflection
    opens controllers.amine to javafx.fxml;
    opens controllers.amine.userController to javafx.fxml;
    opens  controllers.amine.parkingController to javafx.fxml;
    opens controllers.amine.userController.parking  to javafx.fxml;
    opens entities.amine.ParkingModule to javafx.base;


    // Export the main package
    opens main to javafx.fxml;

    // Export required packages
    exports main;
    exports controllers.amine.userController;
}