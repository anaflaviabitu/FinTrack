module com.fintrack {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;


    opens app        to javafx.graphics, javafx.fxml;
    opens controller to javafx.fxml;
    opens repository to javafx.fxml;
    opens service    to javafx.fxml;


    opens model to javafx.base, javafx.fxml;
    opens utils to javafx.fxml;

    exports model;
    exports repository;
    exports utils;
    exports service;
    exports exceptions;

    exports app;
}