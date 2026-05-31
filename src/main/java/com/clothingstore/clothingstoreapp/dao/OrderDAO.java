package com.clothingstore.clothingstoreapp.dao;

import com.clothingstore.clothingstoreapp.db.DatabaseConnection;
import com.clothingstore.clothingstoreapp.model.Order;
import com.clothingstore.clothingstoreapp.model.OrderFilter;
import com.clothingstore.clothingstoreapp.enums.OrderStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAO implements CrudRepository<Order, Integer> {

    private static final String BASE_SELECT = "SELECT OrderID, UserID, OrderDate, TotalAmount, Status FROM Orders";

    @Override
    public Order create(Order order) {
        String sql = "INSERT INTO Orders (UserID, OrderDate, TotalAmount, Status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (conn == null) {
                return null;
            }

            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getOrderDate());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus().getDbValue());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                return null;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getInt(1));
                }
            }
            return order;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Order> findById(Integer id) {
        String sql = BASE_SELECT + " WHERE OrderID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn != null ? conn.prepareStatement(sql) : null) {
            if (ps == null) {
                return Optional.empty();
            }
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY OrderID";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn != null ? conn.createStatement() : null;
             ResultSet rs = stmt != null ? stmt.executeQuery(sql) : null) {

            if (rs == null) {
                return orders;
            }
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    @Override
    public boolean update(Order order) {
        String sql = "UPDATE Orders SET UserID = ?, OrderDate = ?, TotalAmount = ?, Status = ? WHERE OrderID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn != null ? conn.prepareStatement(sql) : null) {
            if (ps == null) {
                return false;
            }
            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getOrderDate());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus().getDbValue());
            ps.setInt(5, order.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM Orders WHERE OrderID = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn != null ? conn.prepareStatement(sql) : null) {
            if (ps == null) {
                return false;
            }
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> search(OrderFilter filter) {
        List<Order> orders = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (filter.getUserId() != null) {
            sql.append(" AND UserID = ?");
            params.add(filter.getUserId());
        }
        if (filter.getDateFrom() != null && !filter.getDateFrom().isBlank()) {
            sql.append(" AND OrderDate >= ?");
            params.add(filter.getDateFrom().trim());
        }
        if (filter.getDateTo() != null && !filter.getDateTo().isBlank()) {
            sql.append(" AND OrderDate <= ?");
            params.add(filter.getDateTo().trim());
        }
        if (filter.getStatus() != null) {
            sql.append(" AND Status = ?");
            params.add(filter.getStatus().getDbValue());
        }
        if (filter.getMinAmount() != null) {
            sql.append(" AND TotalAmount >= ?");
            params.add(filter.getMinAmount());
        }
        if (filter.getMaxAmount() != null) {
            sql.append(" AND TotalAmount <= ?");
            params.add(filter.getMaxAmount());
        }
        sql.append(" ORDER BY OrderID");

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn != null ? conn.prepareStatement(sql.toString()) : null) {
            if (ps == null) {
                return orders;
            }

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        return new Order(
                rs.getInt("OrderID"),
                rs.getInt("UserID"),
                rs.getString("OrderDate"),
                rs.getDouble("TotalAmount"),
                OrderStatus.fromDbValue(rs.getString("Status"))
        );
    }
}

