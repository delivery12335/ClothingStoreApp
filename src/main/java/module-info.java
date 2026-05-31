module com.clothingstore.clothingstoreapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.clothingstore.clothingstoreapp to javafx.fxml;
    opens com.clothingstore.clothingstoreapp.controller to javafx.fxml;
    opens com.clothingstore.clothingstoreapp.db to javafx.fxml;
    opens com.clothingstore.clothingstoreapp.model to javafx.base;
    opens com.clothingstore.clothingstoreapp.dao to javafx.fxml;

    exports com.clothingstore.clothingstoreapp;
    exports com.clothingstore.clothingstoreapp.controller;
    exports com.clothingstore.clothingstoreapp.model;
    exports com.clothingstore.clothingstoreapp.dao;
}