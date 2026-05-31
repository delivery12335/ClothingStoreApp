package com.clothingstore.clothingstoreapp.dao;

import com.clothingstore.clothingstoreapp.db.DatabaseConnection;
import com.clothingstore.clothingstoreapp.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        String sql = "SELECT CategoryID, Name FROM Categories";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("CategoryID"),
                        rs.getString("Name")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }
}

