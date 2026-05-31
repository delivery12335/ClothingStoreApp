package com.clothingstore.clothingstoreapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/login-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 1150, 700);

        // Load CSS styles
        String css = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Магазин одежды - Вход");
        stage.setScene(scene);
        applyApplicationIcon(stage);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void applyApplicationIcon(Stage stage) {
        if (stage == null) {
            return;
        }

        URL iconUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/images/app-icon.png");
        if (iconUrl != null) {
            stage.getIcons().setAll(new Image(iconUrl.toExternalForm()));
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
