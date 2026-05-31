package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.HelloApplication;
import com.clothingstore.clothingstoreapp.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class RegisterController {

    @FXML
    private StackPane authRoot;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox agreementCheckBox;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();


    @FXML
    private void initialize() {
        URL bgUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/images/Gemini_Generated_Image_ydvwhzydvwhzydvw.png");
        if (authRoot != null && bgUrl != null) {
            authRoot.setStyle(
                    "-fx-background-image: " +
                            "linear-gradient(to bottom right, rgba(0,18,51,0.44), rgba(3,83,164,0.22), rgba(245,247,250,0.06)), " +
                            "url('" + bgUrl.toExternalForm() + "'); " +
                            "-fx-background-size: cover, cover; " +
                            "-fx-background-repeat: no-repeat, no-repeat; " +
                            "-fx-background-position: center center, center center;"
            );
        }
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showMessage("Заполните все поля");
            return;
        }

        if (!isValidEmail(email)) {
            showMessage("Введите корректный email");
            return;
        }

        if (!agreementCheckBox.isSelected()) {
            showMessage("Необходимо согласиться с пользовательским соглашением");
            return;
        }

        if (userDAO.usernameExists(username)) {
            showMessage("Такое имя пользователя уже существует");
            return;
        }

        if (userDAO.emailExists(email)) {
            showMessage("Такой email уже зарегистрирован");
            return;
        }

        boolean ok = userDAO.register(email, username, password);
        if (ok) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успешно");
            alert.setHeaderText(null);
            alert.setContentText("Регистрация прошла успешно. Теперь вы можете войти в систему.");
            alert.showAndWait();
            showLoginView();
        } else {
            showMessage("Не удалось зарегистрировать пользователя");
        }
    }

    @FXML
    private void handleCancel() {
        showLoginView();
    }

    private void showLoginView() {
        try {
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Scene currentScene = messageLabel.getScene();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/login-view.fxml"));
            Scene scene = new Scene(loader.load(), currentScene.getWidth(), currentScene.getHeight());

            java.net.URL cssUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            showMessage("Failed to open login screen: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
    }
}
