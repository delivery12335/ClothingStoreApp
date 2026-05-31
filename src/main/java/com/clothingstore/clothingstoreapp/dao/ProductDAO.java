package com.clothingstore.clothingstoreapp.dao;

import com.clothingstore.clothingstoreapp.db.DatabaseConnection;
import com.clothingstore.clothingstoreapp.model.ProductFilter;
import com.clothingstore.clothingstoreapp.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO implements CrudRepository<Product, Integer> {

    private static final String BASE_SELECT = """
            SELECT ProductID, ProductName, CategoryID, Brand, Size, Color, Price, Quantity, ImagePath, Description
            FROM Products
            """;

    @Override
    public Product create(Product product) {
        String sql = """
                INSERT INTO Products (ProductName, CategoryID, Brand, Size, Color, Price, Quantity, ImagePath, Description)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindProduct(ps, product);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                return null;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
            return product;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Product> findById(Integer id) {
        String sql = BASE_SELECT + " WHERE ProductID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY ProductID";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    @Override
    public boolean update(Product product) {
        String sql = """
                UPDATE Products
                SET ProductName = ?, CategoryID = ?, Brand = ?, Size = ?, Color = ?,
                    Price = ?, Quantity = ?, ImagePath = ?, Description = ?
                WHERE ProductID = ?
                """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindProduct(ps, product);
            ps.setInt(10, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM Products WHERE ProductID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean decreaseQuantity(int productId, int quantity) {
        if (quantity <= 0) {
            return true;
        }

        String sql = """
                UPDATE Products
                SET Quantity = Quantity - ?
                WHERE ProductID = ? AND Quantity >= ?
                """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAllProducts() {
        return findAll();
    }

    public List<Product> search(ProductFilter filter) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filter.getNameContains() != null && !filter.getNameContains().isBlank()) {
            sql.append(" AND ProductName LIKE ?");
            params.add("%" + filter.getNameContains().trim() + "%");
        }
        if (filter.getBrand() != null && !filter.getBrand().isBlank()) {
            sql.append(" AND Brand LIKE ?");
            params.add("%" + filter.getBrand().trim() + "%");
        }
        if (filter.getColor() != null && !filter.getColor().isBlank()) {
            sql.append(" AND Color LIKE ?");
            params.add("%" + filter.getColor().trim() + "%");
        }
        if (filter.getCategoryId() != null) {
            sql.append(" AND CategoryID = ?");
            params.add(filter.getCategoryId());
        }
        if (filter.getMinPrice() != null) {
            sql.append(" AND Price >= ?");
            params.add(filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            sql.append(" AND Price <= ?");
            params.add(filter.getMaxPrice());
        }

        sql.append(" ORDER BY ProductID");

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("ProductID"),
                rs.getString("ProductName"),
                rs.getInt("CategoryID"),
                rs.getString("Brand"),
                rs.getString("Size"),
                rs.getString("Color"),
                rs.getDouble("Price"),
                rs.getInt("Quantity"),
                rs.getString("ImagePath"),
                rs.getString("Description")
        );
    }

    private void bindProduct(PreparedStatement ps, Product product) throws SQLException {
        ps.setString(1, product.getName());
        ps.setInt(2, product.getCategoryId());
        ps.setString(3, product.getBrand());
        ps.setString(4, product.getSize());
        ps.setString(5, product.getColor());
        ps.setDouble(6, product.getPrice());
        ps.setInt(7, product.getQuantity());
        ps.setString(8, product.getImagePath());
        ps.setString(9, product.getDescription());
    }
}

