package com.clothingstore.clothingstoreapp.controller;

import com.clothingstore.clothingstoreapp.HelloApplication;
import com.clothingstore.clothingstoreapp.dao.UserDAO;
import com.clothingstore.clothingstoreapp.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {

    @FXML
    private StackPane authRoot;

    @FXML
    private VBox loginCard;

    @FXML
    private ImageView backgroundImageView;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox loginAgreementCheckBox;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        URL bgUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/images/12.jpg");
        if (authRoot != null && backgroundImageView != null && bgUrl != null) {
            Image backgroundImage = new Image(bgUrl.toExternalForm());
            if (backgroundImage.isError()) {
                System.err.println("Login background image failed to load: " + backgroundImage.getException());
                return;
            }

            backgroundImageView.setImage(backgroundImage);
            backgroundImageView.setEffect(new GaussianBlur(6));
            backgroundImageView.fitWidthProperty().bind(authRoot.widthProperty());
            backgroundImageView.fitHeightProperty().bind(authRoot.heightProperty());
            backgroundImageView.toBack();
        } else {
            System.err.println("Login background image resource was not found.");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter username and password");
            messageLabel.getStyleClass().setAll("message-error");
            return;
        }

        if (loginAgreementCheckBox == null || !loginAgreementCheckBox.isSelected()) {
            messageLabel.setText("Agree to the terms first");
            messageLabel.getStyleClass().setAll("message-error");
            return;
        }

        User user = userDAO.login(username, password);

        if (user == null) {
            messageLabel.setText("Wrong username or password");
            messageLabel.getStyleClass().setAll("message-error");
            return;
        }

        try {
            FXMLLoader loader;
            int width;
            int height;
            String title;

            if (user.getRole().equals("ADMIN")) {
                loader = new FXMLLoader(HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/admin-view.fxml"));
                title = "Clothing Store - Панель администратора";
                width = 900;
                height = 700;
            } else {
                loader = new FXMLLoader(HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/fxml/shop-view.fxml"));
                title = "Clothing Store - Каталог";
                width = 1000;
                height = 700;
            }

            Scene scene = new Scene(loader.load(), width, height);

            java.net.URL cssUrl = HelloApplication.class.getResource("/com/clothingstore/clothingstoreapp/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.show();
            stage.setMaximized(true);
        } catch (Exception e) {
            System.err.println("Error opening user window: " + e.getMessage());
            e.printStackTrace(System.err);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(e.getClass().getSimpleName() + ": " + e.getMessage());

            StringBuilder sb = new StringBuilder();
            for (StackTraceElement el : e.getStackTrace()) {
                sb.append(el).append("\n");
            }

            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();

            messageLabel.setText("Error opening window: " + e.getMessage());
            messageLabel.getStyleClass().setAll("message-error");
        }
    }

    @FXML
    private void openRegisterWindow() {
        showRegisterCard();
    }

    private void showRegisterCard() {
        loginCard.setVisible(false);
        loginCard.setManaged(false);

        VBox registerCard = new VBox(18);
        registerCard.setAlignment(javafx.geometry.Pos.CENTER);
        registerCard.getStyleClass().add("auth-card");
        registerCard.setMaxWidth(380);
        registerCard.setMinWidth(380);
        registerCard.setMaxHeight(520);
        registerCard.setMinHeight(520);
        StackPane.setAlignment(registerCard, Pos.CENTER);

        Label title = new Label("SIGN UP");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("Create your account");
        subtitle.getStyleClass().add("auth-subtitle");

        TextField email = new TextField();
        email.setPromptText("Enter your email");
        email.getStyleClass().add("input-field");

        TextField username = new TextField();
        username.setPromptText("Choose a username");
        username.getStyleClass().add("input-field");

        PasswordField password = new PasswordField();
        password.setPromptText("Create a password");
        password.getStyleClass().add("input-field");

        CheckBox agreement = new CheckBox("I agree to terms and conditions");
        agreement.getStyleClass().add("helper-text");

        Label registerMessage = new Label();
        registerMessage.getStyleClass().add("message-error");

        Button registerButton = new Button("REGISTER");
        registerButton.getStyleClass().add("button-primary");
        registerButton.setPrefWidth(280);
        registerButton.setPrefHeight(44);

        Button backButton = new Button("BACK TO LOGIN");
        backButton.getStyleClass().add("button-outline");
        backButton.setPrefWidth(280);
        backButton.setPrefHeight(42);

        registerButton.setOnAction(e -> handleRegister(email, username, password, agreement, registerMessage));
        backButton.setOnAction(e -> showLoginCard(registerCard));

        registerCard.getChildren().addAll(
                title,
                subtitle,
                labeledField("Email ID", email),
                labeledField("Username", username),
                labeledField("Password", password),
                agreement,
                registerButton,
                backButton,
                registerMessage
        );

        authRoot.getChildren().add(registerCard);
    }

    private VBox labeledField(String labelText, Control field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("helper-text");
        VBox box = new VBox(5, label, field);
        box.setMinWidth(280);
        box.setMaxWidth(280);
        return box;
    }

    private void handleRegister(
            TextField emailField,
            TextField usernameField,
            PasswordField passwordField,
            CheckBox agreementCheckBox,
            Label registerMessage
    ) {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        registerMessage.getStyleClass().setAll("message-error");

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            registerMessage.setText("Fill in all fields");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            registerMessage.setText("Enter a valid email");
            return;
        }
        if (!agreementCheckBox.isSelected()) {
            registerMessage.setText("Agree to the terms first");
            return;
        }
        if (userDAO.usernameExists(username)) {
            registerMessage.setText("Username already exists");
            return;
        }
        if (userDAO.emailExists(email)) {
            registerMessage.setText("Email is already registered");
            return;
        }

        if (userDAO.register(email, username, password)) {
            registerMessage.getStyleClass().setAll("message-success");
            registerMessage.setText("Registration complete. Go back and sign in.");
            this.usernameField.setText(username);
        } else {
            registerMessage.setText("Could not register user");
        }
    }

    private void showLoginCard(VBox registerCard) {
        authRoot.getChildren().remove(registerCard);
        loginCard.setManaged(true);
        loginCard.setVisible(true);
    }
}
