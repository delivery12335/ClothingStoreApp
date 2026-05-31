package com.clothingstore.clothingstoreapp.dao;

import com.clothingstore.clothingstoreapp.db.DatabaseConnection;
import com.clothingstore.clothingstoreapp.model.User;
import com.clothingstore.clothingstoreapp.enums.UserRole;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public UserDAO() {
        ensureEmailColumnExists();
    }

    public User login(String username, String password) {

        String sql = "SELECT UserID, Email, Username, Password, Role FROM Users WHERE Username = ? AND Password = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("UserID"),
                        resultSet.getString("Email"),
                        resultSet.getString("Username"),
                        resultSet.getString("Password"),
                        resultSet.getString("Role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean register(String email, String username, String password) {
        ensureEmailColumnExists();

        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        String normalizedUsername = username == null ? null : username.trim();
        String normalizedPassword = password == null ? null : password.trim();

        if (normalizedEmail == null || normalizedEmail.isEmpty() || normalizedUsername == null || normalizedUsername.isEmpty() || normalizedPassword == null || normalizedPassword.isEmpty()) {
            return false;
        }

        if (usernameExists(normalizedUsername) || emailExists(normalizedEmail)) {
            return false;
        }

        String sql = "INSERT INTO Users (Email, Username, Password, Role) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalizedEmail);
            statement.setString(2, normalizedUsername);
            statement.setString(3, normalizedPassword);
            statement.setString(4, UserRole.USER);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean usernameExists(String username) {
        return valueExists("Username", username == null ? null : username.trim());
    }

    public boolean emailExists(String email) {
        return valueExists("Email", email == null ? null : email.trim().toLowerCase());
    }

    private boolean valueExists(String column, String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM Users WHERE " + column + " = ? LIMIT 1";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void ensureEmailColumnExists() {
        try (Connection connection = DatabaseConnection.connect()) {
            if (connection == null) return;

            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(null, null, "Users", "Email")) {
                if (!columns.next()) {
                    try (PreparedStatement alter = connection.prepareStatement("ALTER TABLE Users ADD COLUMN Email TEXT")) {
                        alter.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

