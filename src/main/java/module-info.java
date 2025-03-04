module tn.esprit.market_3a33 {
    requires javafx.controls;
    requires javafx.fxml;
    requires mysql.connector.j;
    requires javafx.base;
    requires javafx.graphics;
    requires kernel;
    requires layout;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires stripe.java;
    requires java.mail;


    exports test;
    opens test to javafx.graphics, javafx.fxml;
    opens utils to javafx.fxml;
    exports utils;
    opens Controllers.tasnim to javafx.fxml;
    opens entities.tasnim to javafx.base;
    exports services.tasnim;
    opens services.tasnim to javafx.fxml;
    exports entities.tasnim;

}