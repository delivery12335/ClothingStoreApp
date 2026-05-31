package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

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
            closeWindow();
        } else {
            showMessage("Не удалось зарегистрировать пользователя");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().setAll("message-error");
    }
}
